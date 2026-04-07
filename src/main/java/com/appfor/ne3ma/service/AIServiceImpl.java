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
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final UserRepository userRepository;
    private final AIConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIProvider aiProvider;

    @Override
    public MessageResponse processMessage(ChatRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        AI_Conversations conversation = resolveConversation(request, user);

        Message userMessage = new Message();
        userMessage.setAI_conv(conversation);
        userMessage.setRole(Role.USER);
        userMessage.setContent(request.getMessage());
        messageRepository.save(userMessage);

        String reply = aiProvider.generateReply(request.getMessage(), username);

        Message aiMessage = new Message();
        aiMessage.setAI_conv(conversation);
        aiMessage.setRole(Role.ASSISTANT);
        aiMessage.setContent(reply);
        messageRepository.save(aiMessage);

        return new MessageResponse(reply, conversation.getId(), LocalDateTime.now());
    }

    private AI_Conversations resolveConversation(ChatRequest request, User user) {
        if (request.getConversationId() != null) {
            return conversationRepository.findByIdAndUser(request.getConversationId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        }

        AI_Conversations conversation = new AI_Conversations();
        conversation.setUser(user);
        conversation.setConv_title(buildTitle(request.getMessage()));
        return conversationRepository.save(conversation);
    }

    private String buildTitle(String message) {
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isEmpty()) {
            return "New chat";
        }
        int max = 40;
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
