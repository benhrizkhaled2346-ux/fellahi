package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.ChatRequest;
import com.appfor.ne3ma.dto.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AIService {
    MessageResponse processMessage(ChatRequest request, String username);

    MessageResponse analyzeImage(
            MultipartFile image,
            String prompt,
            Long conversationId,
            String username
    );
}
