package com.appfor.ne3ma.provider;

import org.springframework.stereotype.Component;

@Component
public class MockAIProvider implements AIProvider {

    @Override
    public String generateReply(String prompt, String username) {
        return "Mock reply for " + username + ": " + prompt;
    }

    @Override
    public String analyzeImage(byte[] imageBytes, String contentType, String prompt, String username) {
        String effectivePrompt = (prompt == null || prompt.isBlank())
                ? "Describe this image."
                : prompt.trim();
        int size = imageBytes == null ? 0 : imageBytes.length;
        return "Mock image analysis for " + username
                + " (" + contentType + ", " + size + " bytes): "
                + effectivePrompt;
    }
}
