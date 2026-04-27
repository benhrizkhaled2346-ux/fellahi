package com.appfor.ne3ma.service;

import com.appfor.ne3ma.model.RefreshToken;
import com.appfor.ne3ma.model.User;
import com.appfor.ne3ma.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final  JWTService jwtService;

    private final MyuserdetailsServer userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

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
}
