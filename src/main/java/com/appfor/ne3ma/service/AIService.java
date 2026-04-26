package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.ChatRequest;
import com.appfor.ne3ma.dto.ImageResponse;
import com.appfor.ne3ma.dto.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AIService {
    MessageResponse processMessage(ChatRequest request, String email);

    ImageResponse analyzeImage(
            MultipartFile image,
            String email
    );
}
