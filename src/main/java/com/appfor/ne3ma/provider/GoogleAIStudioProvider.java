package com.appfor.ne3ma.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Primary
public class GoogleAIStudioProvider implements AIProvider {

    private static final String SCOPE_REJECTION =
            "I can only answer questions about farming, plants, animals, nature, and the environment.";

    private final RestTemplate restTemplate;

    @Value("${google.ai.api.key:}")
    private String apiKey;

    @Value("${google.ai.model:gemini-1.5-flash}")
    private String model;

    public GoogleAIStudioProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String generateReply(String prompt, String email) {
        if (prompt == null || prompt.isBlank()) {
            return "Please provide a question related to farming or nature.";
        }
        if (!isFarmOrNatureContext(prompt)) {
            return SCOPE_REJECTION;
        }
        if (apiKey == null || apiKey.isBlank()) {
            return "Google AI API key is not configured. Set google.ai.api.key.";
        }

        String configuredModel = normalizeModelName(model);
        try {
            return callGenerateContent(configuredModel, prompt);
        } catch (RestClientException ex) {
            return "AI provider request failed for model '" + configuredModel + "': " + ex.getMessage();
        }
    }

    @Override
    public String analyzeImage(byte[] imageBytes, String contentType, String prompt, String email) {
        return "Image analysis is not yet implemented with Google AI Studio provider.";
    }

    private boolean isFarmOrNatureContext(String prompt) {
        String lower = prompt.toLowerCase();
        String[] keywords = {
                "farm", "farming", "agri", "agriculture", "crop", "soil", "irrigation", "fertilizer",
                "plant", "tree", "leaf", "flower", "seed", "garden", "forest", "nature",
                "animal", "livestock", "cow", "goat", "chicken", "poultry", "fish", "bee",
                "climate", "weather", "rain", "drought", "environment", "sustainab","Disease","olive","palm","tree"
        };

        for (String keyword : keywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildScopedPrompt(String userPrompt) {
        return """
                You are an assistant specialized in farming and nature only.
                Allowed topics: agriculture, crops, livestock, soils, irrigation, pests, biodiversity, weather for farming, and environmental sustainability.
                If user asks outside these topics, politely refuse in one sentence.
                Answer clearly and practically.

                User question:
                """ + userPrompt;
    }

    private String callGenerateContent(String modelName, String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + modelName
                + ":generateContent?key="
                + apiKey;

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> userPart = Map.of("text", buildScopedPrompt(prompt));
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "parts", List.of(userPart)
        );
        payload.put("contents", List.of(userMessage));
        payload.put("generationConfig", Map.of(
                "temperature", 0.4,
                "maxOutputTokens", 512
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return extractText(response.getBody());
    }

    private String normalizeModelName(String rawModelName) {
        if (rawModelName == null) {
            return "";
        }
        String trimmed = rawModelName.trim();
        if (trimmed.startsWith("models/")) {
            return trimmed.substring("models/".length());
        }
        return trimmed;
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map responseBody) {
        if (responseBody == null) {
            return "Empty response from AI provider.";
        }

        Object candidatesObj = responseBody.get("candidates");
        if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
            return "No answer generated by AI provider.";
        }

        Object firstCandidateObj = candidates.get(0);
        if (!(firstCandidateObj instanceof Map<?, ?> firstCandidate)) {
            return "Invalid response format from AI provider.";
        }

        Object contentObj = firstCandidate.get("content");
        if (!(contentObj instanceof Map<?, ?> content)) {
            return "Invalid response content from AI provider.";
        }

        Object partsObj = content.get("parts");
        if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) {
            return "No text parts found in AI response.";
        }

        Object firstPartObj = parts.get(0);
        if (!(firstPartObj instanceof Map<?, ?> firstPart)) {
            return "Invalid text part in AI response.";
        }

        Object text = firstPart.get("text");
        return Objects.toString(text, "AI response did not contain text.");
    }
}
