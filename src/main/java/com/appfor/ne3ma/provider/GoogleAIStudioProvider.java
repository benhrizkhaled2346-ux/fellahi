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

    @Value("${openrouter.api.key:${google.ai.api.key:}}")
    private String apiKey;

    @Value("${openrouter.model:${google.ai.model:google/gemini-2.5-flash}}")
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
            return "OpenRouter API key is not configured. Set openrouter.api.key.";
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
        return userPrompt == null ? "" : userPrompt.trim();
    }

    private String systemInstruction() {
        return """
                You are an assistant specialized in farming and nature only.
                Allowed topics: agriculture, crops, livestock, soils, irrigation, pests, biodiversity, weather for farming, and environmental sustainability.
                If user asks outside these topics, politely refuse in one sentence.
                Answer clearly and practically.
                """;
    }

    private String callGenerateContent(String modelName, String prompt) {
        String url = "https://openrouter.ai/api/v1/chat/completions";

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", modelName);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", systemInstruction()),
                Map.of("role", "user", "content", buildScopedPrompt(prompt))
        ));
        payload.put("temperature", 0.4);
        payload.put("max_tokens", 512);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
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

        Object choicesObj = responseBody.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return "No answer generated by AI provider.";
        }

        Object firstChoiceObj = choices.get(0);
        if (!(firstChoiceObj instanceof Map<?, ?> firstChoice)) {
            return "Invalid response format from AI provider.";
        }

        Object messageObj = firstChoice.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) {
            return "Invalid response content from AI provider.";
        }

        Object content = message.get("content");
        if (content instanceof String text && !text.isBlank()) {
            return text;
        }

        if (content instanceof List<?> parts && !parts.isEmpty()) {
            StringBuilder text = new StringBuilder();
            for (Object partObj : parts) {
                if (partObj instanceof Map<?, ?> part) {
                    Object partText = part.get("text");
                    if (partText != null) {
                        if (text.length() > 0) {
                            text.append('\n');
                        }
                        text.append(partText);
                    }
                }
            }
            if (text.length() > 0) {
                return text.toString();
            }
        }

        return Objects.toString(content, "AI response did not contain text.");
    }
}
