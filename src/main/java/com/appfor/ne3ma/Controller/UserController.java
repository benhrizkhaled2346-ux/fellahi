package com.appfor.ne3ma.Controller;

import com.appfor.ne3ma.dto.*;
import com.appfor.ne3ma.service.AuthService;
import com.appfor.ne3ma.service.JWTService;
import com.appfor.ne3ma.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private  final AuthService AuthService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        System.out.println(request.getUsername());
        return ResponseEntity.ok(userService.login(request));
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String header) {

        userService.logout(header);

        return ResponseEntity.ok("Logged out successfully");
    }
    @GetMapping("/logout")
    public String logout(){
        return "logout";
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestParam String username) {
        return ResponseEntity.ok(userService.getCurrentUser(username));
    }

//    @PutMapping("/me")
//    public ResponseEntity<UserResponse> updateCurrentUser(
//            @Valid @RequestBody UpdateUserRequest request) {
//        if (request.getUsername() == null || request.getUsername().isBlank()) {
//            return ResponseEntity.badRequest().build();
//        }
//        return ResponseEntity.ok(userService.updateCurrentUser(request.getUsername(), request));
//    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken").trim();
        String newAccessToken = AuthService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

}
