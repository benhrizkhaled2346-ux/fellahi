package com.appfor.ne3ma.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleTokenInfoResponse {
    private String aud;
    private String sub;
    private String email;
    private String name;

    @JsonProperty("email_verified")
    private String emailVerified;

    public boolean isEmailVerified() {
        return Boolean.parseBoolean(emailVerified);
    }
}
