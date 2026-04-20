package com.appfor.ne3ma.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Map<String, String> answers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
