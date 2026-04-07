package com.appfor.ne3ma.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String reply;
    private Long conversationId;
    private LocalDateTime timestamp;
}
