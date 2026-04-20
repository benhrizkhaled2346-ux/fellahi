package com.appfor.ne3ma.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private String refreshtoken;
    private String phone;
    private String fullname;

}
