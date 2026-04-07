package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.*;

import java.util.List;

public interface UserService {
    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser(String username);

//    UserResponse updateCurrentUser(String username, UpdateUserRequest request);

    List<UserResponse> listUsers();

    void deleteUser(Long id);

    void logout(String header);
}
