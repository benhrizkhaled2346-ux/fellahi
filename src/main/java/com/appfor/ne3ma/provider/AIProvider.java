package com.appfor.ne3ma.provider;

public interface AIProvider {
    String generateReply(String prompt, String email);

    String analyzeImage(byte[] imageBytes, String contentType, String prompt, String email);
}
