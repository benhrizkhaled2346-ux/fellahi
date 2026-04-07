package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.*;
import com.appfor.ne3ma.model.User;
import com.appfor.ne3ma.repository.UserRepository;
import java.util.List;
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
    @Override
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        UserPrincipal userPrincipal = new UserPrincipal(saved);
        String token = jwtservice.generateToken(userPrincipal);
        return new LoginResponse(
                token,
                saved.getUsername()
        );


    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserPrincipal userDetails =
                (UserPrincipal) authentication.getPrincipal();

        String token = jwtservice.generateToken(userDetails);


        return new LoginResponse(
                token,
                userDetails.getUsername()
        );
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(user);
    }

    @Override
    public UserResponse updateCurrentUser(String username, UpdateUserRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!request.getUsername().equals(user.getUsername())
                    && userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

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

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private boolean isBcryptHash(String password) {
        return password != null
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}
