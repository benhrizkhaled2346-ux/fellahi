package com.appfor.ne3ma.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @JsonAlias("userIdentity")
    private String email;

    @JsonAlias("tel")
    private String phone;

    private String type;

    @NotBlank
    private String password;

    public String resolveIdentity() {
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        if (phone != null && !phone.isBlank()) {
            return phone.trim();
        }
        throw new IllegalArgumentException("Email or phone is required");
    }
}
