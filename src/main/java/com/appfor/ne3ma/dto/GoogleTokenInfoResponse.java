package com.appfor.ne3ma.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTokenInfoResponse {
    private String email;

    @JsonProperty("name")
    private String fullname;
}
