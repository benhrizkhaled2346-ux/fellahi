package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.GoogleLoginRequest;
import com.appfor.ne3ma.dto.GoogleTokenInfoResponse;
import com.appfor.ne3ma.dto.LoginResponse;
import com.appfor.ne3ma.model.RefreshToken;
import com.appfor.ne3ma.model.User;
import com.appfor.ne3ma.repository.RefreshTokenRepository;
import com.appfor.ne3ma.repository.UserRepository;
import com.appfor.ne3ma.security.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String GOOGLE_TOKEN_INFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}";
    private static final String GOOGLE_PHONE_PREFIX = "google:";


    private final JWTService jwtService;
    private final MyuserdetailsServer userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${google.oauth.client-id}")
    private String googleClientId;

    public LoginResponse authenticateWithGoogle(GoogleLoginRequest request) {
        GoogleTokenInfoResponse tokenInfo = verifyGoogleIdToken(request.getIdToken());
        User user = userRepository.findByEmail(tokenInfo.getEmail())
                .orElseGet(() -> createGoogleUser(tokenInfo));

        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtService.generateToken(userPrincipal);
        String refreshTokenValue = null;

        if (request.isRememberMe()) {
            RefreshToken refreshToken = jwtService.generateRefreshToken(userPrincipal);
            refreshTokenValue = refreshToken.getToken();
        } else {
            deleteRefreshToken(user);
        }

        return new LoginResponse(
                token,
                user.getEmail(),
                refreshTokenValue,
                user.getPhone(),
                user.getFullname()
        );
    }

    public String refreshAccessToken(String refreshTokenUuid) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenUuid)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));


        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }


        User user = refreshToken.getUser();


        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return jwtService.generateToken(userDetails);
    }

    @Transactional
    public void deleteRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private GoogleTokenInfoResponse verifyGoogleIdToken(String idToken) {
        GoogleTokenInfoResponse tokenInfo;

        try {
            tokenInfo = restTemplate.getForObject(
                    GOOGLE_TOKEN_INFO_URL,
                    GoogleTokenInfoResponse.class,
                    idToken
            );
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Invalid Google ID token", ex);
        }

        if (tokenInfo == null || !StringUtils.hasText(tokenInfo.getEmail())) {
            throw new IllegalArgumentException("Google token validation failed");
        }

        if (!googleClientId.equals(tokenInfo.getAud())) {
            throw new IllegalArgumentException("Google token client ID mismatch");
        }

        if (!tokenInfo.isEmailVerified()) {
            throw new IllegalArgumentException("Google account email is not verified");
        }

        return tokenInfo;
    }

    private User createGoogleUser(GoogleTokenInfoResponse tokenInfo) {
        User user = new User();
        user.setEmail(tokenInfo.getEmail());
        user.setFullname(resolveFullName(tokenInfo));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setPhone(GOOGLE_PHONE_PREFIX + tokenInfo.getSub());
        return userRepository.save(user);
    }

    private String resolveFullName(GoogleTokenInfoResponse tokenInfo) {
        if (StringUtils.hasText(tokenInfo.getName())) {
            return tokenInfo.getName();
        }

        return tokenInfo.getEmail().split("@", 2)[0];
    }
}
