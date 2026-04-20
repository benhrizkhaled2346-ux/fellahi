package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.ChatRequest;
import com.appfor.ne3ma.dto.MessageResponse;
import com.appfor.ne3ma.model.AI_Conversations;
import com.appfor.ne3ma.model.Message;
import com.appfor.ne3ma.model.Role;
import com.appfor.ne3ma.model.User;
import com.appfor.ne3ma.provider.AIProvider;
import com.appfor.ne3ma.repository.AIConversationRepository;
import com.appfor.ne3ma.repository.MessageRepository;
import com.appfor.ne3ma.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final RestTemplate restTemplate;                    // ✅ injected from bean
    private final UserRepository userRepository;
    private final AIConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIProvider aiProvider;

    @Value("${pycont.url}")                                     // ✅ externalized, not hardcoded
    private String pydocUrl;

    @Override
    public MessageResponse processMessage(ChatRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        AI_Conversations conversation =
                resolveConversation(request.getConversationId(), request.getMessage(), user);

        Message userMessage = new Message();
        userMessage.setAI_conv(conversation);
        userMessage.setRole(Role.USER);
        userMessage.setContent(request.getMessage());
        messageRepository.save(userMessage);

        String reply = aiProvider.generateReply(request.getMessage(), email);

        Message aiMessage = new Message();
        aiMessage.setAI_conv(conversation);
        aiMessage.setRole(Role.ASSISTANT);
        aiMessage.setContent(reply);
        messageRepository.save(aiMessage);

        return new MessageResponse(reply, conversation.getId(), LocalDateTime.now());
    }

    @Override
    public MessageResponse analyzeImage(MultipartFile image, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        Message userMessage = new Message();
        userMessage.setAI_conv(null);
        userMessage.setRole(Role.USER);
        userMessage.setContent("[IMAGE]");
        messageRepository.save(userMessage);

        String reply;
        try {
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());

            Map<String, String> body = new HashMap<>();
            body.put("image", base64Image);

            ResponseEntity<Map> response = restTemplate.postForEntity(  // ✅ use injected bean
                    pydocUrl + "/analyze",
                    body,
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            if (result == null) {
                throw new IllegalArgumentException("Empty response from AI service");
            }

            if (Boolean.FALSE.equals(result.get("valid"))) {
                reply = (String) result.get("message");
            } else {
                reply = String.format(
                        "Disease: %s | Confidence: %s%% | Top 3: %s",
                        result.get("disease"),
                        result.get("confidence"),
                        result.get("top3")
                );
            }

        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to read image: " + ex.getMessage(), ex);
        }

        Message aiMessage = new Message();
        aiMessage.setAI_conv(null);
        aiMessage.setRole(Role.ASSISTANT);
        aiMessage.setContent(reply);
        messageRepository.save(aiMessage);

        return new MessageResponse(reply, null, LocalDateTime.now());
    }

    private AI_Conversations resolveConversation(Long conversationId, String seedMessage, User user) {
        if (conversationId != null) {
            return conversationRepository.findByIdAndUser(conversationId, user)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        }

        AI_Conversations conversation = new AI_Conversations();
        conversation.setUser(user);
        conversation.setConv_title(buildTitle(seedMessage));
        return conversationRepository.save(conversation);
    }

    private String buildTitle(String message) {
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isEmpty()) return "New chat";
        int max = 40;
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
