package com.appfor.ne3ma.provider;

import org.springframework.stereotype.Component;

@Component
public class MockAIProvider implements AIProvider {

    @Override
    public String generateReply(String prompt, String email) {
        return "Mock reply for " + email + ": " + prompt;
    }

    @Override
    public String analyzeImage(byte[] imageBytes, String contentType, String prompt, String email) {
        String effectivePrompt = (prompt == null || prompt.isBlank())
                ? "Describe this image."
                : prompt.trim();
        int size = imageBytes == null ? 0 : imageBytes.length;
        return "Mock image analysis for " + email
                + " (" + contentType + ", " + size + " bytes): "
                + effectivePrompt;
    }
}
