package com.appfor.ne3ma.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String fullname;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
    @NotBlank
    private String phone;

    private boolean rememberMe;
}
