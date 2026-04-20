package com.appfor.ne3ma.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String fullname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
