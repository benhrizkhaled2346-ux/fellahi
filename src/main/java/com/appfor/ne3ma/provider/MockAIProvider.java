package com.appfor.ne3ma.provider;

import org.springframework.stereotype.Component;

@Component
public class MockAIProvider implements AIProvider {

    @Override
    public String generateReply(String prompt, String username) {
        return "Mock reply for " + username + ": " + prompt;
    }
}
