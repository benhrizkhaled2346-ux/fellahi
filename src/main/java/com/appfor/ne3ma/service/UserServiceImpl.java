package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.*;
import com.appfor.ne3ma.model.InvalidToken;
import com.appfor.ne3ma.model.RefreshToken;
import com.appfor.ne3ma.model.User;
import com.appfor.ne3ma.repository.InvalidTokenRepository;
import com.appfor.ne3ma.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.appfor.ne3ma.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final JWTService jwtservice;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final InvalidTokenRepository invalidtokenrepo;
    private final AuthService authService;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{8,15}$");
    @Override
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullname(request.getFullname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        User saved = userRepository.save(user);
        UserPrincipal userPrincipal = new UserPrincipal(saved);
        String token = jwtservice.generateToken(userPrincipal);
        RefreshToken refreshToken =jwtservice.generateRefreshToken(userPrincipal);
        return new LoginResponse(
                token,
                saved.getEmail()
                ,refreshToken.getToken(),saved.getPhone(), saved.getFullname()
        );


    }


    @Override
    public LoginResponse login(LoginRequest request) {
         if (PHONE_PATTERN.matcher(request.getUserid()).matches()) {
            request.setUserid(userRepository.findByPhone(request.getUserid())
                     .orElseThrow(() -> new IllegalArgumentException("user not found by this phone")));
         }


            else if (!(EMAIL_PATTERN.matcher(request.getUserid()).matches())){
             throw new IllegalArgumentException("UserId must be a valid email or phone number");

        }


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserid(),
                        request.getPassword()
                )
        );

        UserPrincipal userDetails =
                (UserPrincipal) authentication.getPrincipal();

        String token = jwtservice.generateToken(userDetails);
        RefreshToken refreshToken =jwtservice.generateRefreshToken(userDetails);

        return new LoginResponse(
                token,
                userDetails.getUsername()
                ,refreshToken.getToken(),userDetails.getPhone(), userDetails.getFullname()
        );
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(user);
    }

//    @Override
//    public UserResponse updateCurrentUser(String email, UpdateUserRequest request) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        if (request.getEmail() != null && !request.getEmail().isBlank()) {
//            if (!request.getEmail().equals(user.getEmail())
//                    && userRepository.existsByEmail(request.getEmail())) {
//                throw new IllegalArgumentException("Email already exists");
//            }
//            user.setEmail(request.getEmail());
//        }
//
//        if (request.getPassword() != null && !request.getPassword().isBlank()) {
//            user.setPassword(passwordEncoder.encode(request.getPassword()));
//        }
//
//        User saved = userRepository.save(user);
//        return toResponse(saved);
//    }

    @Override
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token");
        }

        String token = authorizationHeader.substring(7);

        InvalidToken blacklistedToken = new InvalidToken();
        blacklistedToken.setToken(token);

        invalidtokenrepo.save(blacklistedToken);
    }




    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullname(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
