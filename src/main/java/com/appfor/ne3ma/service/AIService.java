package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.ChatRequest;
import com.appfor.ne3ma.dto.MessageResponse;

public interface AIService {
    MessageResponse processMessage(ChatRequest request, String username);
}
