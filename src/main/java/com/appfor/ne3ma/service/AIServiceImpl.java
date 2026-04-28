package com.appfor.ne3ma.service;

import com.appfor.ne3ma.dto.ChatRequest;
import com.appfor.ne3ma.dto.ImageResponse;
import com.appfor.ne3ma.dto.MessageResponse;
import com.appfor.ne3ma.model.AI_Conversations;
import com.appfor.ne3ma.model.Message;
import com.appfor.ne3ma.model.Role;
import com.appfor.ne3ma.model.User;
import com.appfor.ne3ma.provider.AIProvider;
import com.appfor.ne3ma.repository.AIConversationRepository;
import com.appfor.ne3ma.repository.MessageRepository;
import com.appfor.ne3ma.repository.UserRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final AIConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIProvider aiProvider;

    @Value("${pycont.url}")
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
    public ImageResponse analyzeImage(MultipartFile image, String email) {
        userRepository.findByEmail(email)
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
            EncodedImage encodedImage = normalizeImage(image);
            String base64Image = Base64.getEncoder().encodeToString(encodedImage.bytes());

            Map<String, Object> body = new HashMap<>();
            body.put("image", base64Image);
            body.put("contentType", encodedImage.contentType());
            body.put("filename", buildNormalizedFilename(image.getOriginalFilename(), encodedImage.format()));

            ResponseEntity<Map> response = restTemplate.postForEntity(
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
        } catch (HttpClientErrorException ex) {
            throw new IllegalArgumentException("AI image analysis failed: " + ex.getResponseBodyAsString(), ex);
        }

        Message aiMessage = new Message();
        aiMessage.setAI_conv(null);
        aiMessage.setRole(Role.ASSISTANT);
        aiMessage.setContent(reply);
        messageRepository.save(aiMessage);

        String aireply = aiProvider.generateReply(" Input will be a raw prediction like:\n" +
                "                - Disease name\n" +
                "                - Confidence score\n" +
                "                - Top predictions\n" +
                "                \n" +
                "                Your task:\n" +
                "                1. Extract the main disease.\n" +
                "                2. Format a clean JSON output with:\n" +
                "                   - disease (name, scientificName, confidence, type, short description)\n" +
                "                   - advice (list of actionable steps with title + description)\n" +
                "                \n" +
                "                3. Then generate a UI-friendly structured response including:\n" +
                "                - Title: \"Treatment\"\n" +
                "                - Disease name + confidence %\n" +
                "                - Quick Summary (short explanation + severity label)\n" +
                "                - Immediate Actions (2 urgent steps)\n" +
                "                - Comprehensive Plan (3–4 detailed treatments with frequency if possible)\n" +
                "                - Prevention & Monitoring (bullet points)\n" +
                "                - Recommended Products (generic placeholders)\n" +
                "                - Progress Checklist (simple progress indicator like \"2 of 6 complete\")\n" +
                "                \n" +
                "                Rules:\n" +
                "                - Keep text short, clear, and practical\n" +
                "                - Use simple English\n" +
                "                - Infer missing info if needed (e.g. fungal → fungicide)\n" +
                "                - Severity: HIGH if confidence > 90%\n" +
                "                \n" +
                "                Input:\n" +
                "                {input_here}\n" +
                "                \n" +
                "                Output:\n" +
                "                Return clean JSON + structured UI sections exactly as described.\n"+reply, email);
        return new ImageResponse(reply, null, LocalDateTime.now(),aireply);
    }

    private AI_Conversations resolveConversation(Long conversationId, String seedMessage, User user) {
        if (conversationId != null && conversationId > 0) {
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
        if (trimmed.isEmpty()) {
            return "New chat";
        }
        int max = 40;
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private EncodedImage normalizeImage(MultipartFile image) throws IOException {
        ImageIO.setUseCache(false);

        try (InputStream inputStream = image.getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            if (bufferedImage == null) {
                throw new IllegalArgumentException("Unsupported or corrupted image file");
            }

            String format = resolveTargetFormat(image.getContentType(), bufferedImage);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                boolean written = ImageIO.write(bufferedImage, format, outputStream);
                if (!written) {
                    throw new IllegalArgumentException("Unsupported image format: " + image.getContentType());
                }
                return new EncodedImage(outputStream.toByteArray(), "image/" + format, format);
            }
        }
    }

    private String resolveTargetFormat(String contentType, BufferedImage image) {
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/jpeg".equalsIgnoreCase(contentType) || "image/jpg".equalsIgnoreCase(contentType)) {
            return "jpg";
        }
        return image.getColorModel().hasAlpha() ? "png" : "jpg";
    }

    private String buildNormalizedFilename(String originalFilename, String format) {
        String baseName = (originalFilename == null || originalFilename.isBlank()) ? "upload" : originalFilename;
        int extensionIndex = baseName.lastIndexOf('.');
        if (extensionIndex >= 0) {
            baseName = baseName.substring(0, extensionIndex);
        }
        return baseName + "." + format;
    }

    private record EncodedImage(byte[] bytes, String contentType, String format) {
    }
}
