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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class
AIServiceImpl implements AIService {

    private final UserRepository userRepository;
    private final AIConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIProvider aiProvider;
    // ✅ Works on Windows (python) and Linux/Mac (python3)
    private static final String PYTHON_PATH ="python3";
    private static final String SCRIPT_PATH = new File("ai/predict.py").getAbsolutePath();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public MessageResponse processMessage(ChatRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        AI_Conversations conversation =
                resolveConversation(request.getConversationId(), request.getMessage(), user);

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

    @Override
    public MessageResponse analyzeImage(
            MultipartFile image,
            String username
    ) {
        try {

            byte[] bytes = image.getBytes();
            String encoded = Base64.getEncoder().encodeToString(bytes);
            // This will print a HUGE string in your Railway logs
            System.out.println("DEBUG_IMAGE_BEGIN");
            System.out.println("data:" + image.getContentType() + ";base64," + encoded);
            System.out.println("DEBUG_IMAGE_END");
        }
        catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to convert image ");}


        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // 2. Validate image
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }
        String contentType = image.getContentType();
        System.out.println(contentType);
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // 5. Save user message
        Message userMessage = new Message();
        userMessage.setAI_conv(null);
        userMessage.setRole(Role.USER);
        userMessage.setContent("[IMAGE] ");
        messageRepository.save(userMessage);

        // 6. Call predict.py
        String reply;
        try {
            // Write uploaded image to a temp file
            File scriptFile = new File(SCRIPT_PATH).getAbsoluteFile();
            File tempImage  = File.createTempFile("upload_", "_" + image.getOriginalFilename());
            image.transferTo(tempImage);

            System.out.println("Working dir : " + System.getProperty("user.dir"));
            System.out.println("Script path : " + scriptFile.getAbsolutePath());
            System.out.println("Image  path : " + tempImage.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_PATH,
                    scriptFile.getAbsolutePath(),
                    tempImage.getAbsolutePath()
            );


            Process process = pb.start();

            // ✅ Drain stderr in background so process never blocks
            StringBuilder errors = new StringBuilder();
            Thread errThread = new Thread(() -> {
                try (BufferedReader err = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String l;
                    while ((l = err.readLine()) != null) {
                        errors.append(l).append("\n");
                    }
                } catch (IOException ignored) {}
            });
            errThread.start();

            // ✅ Read stdout — last non-empty line is always the JSON
            String lastLine = "";
            String line;
            int lineNum = 0;
            try (BufferedReader stdOut = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                while ((line = stdOut.readLine()) != null) {
                    lineNum++;
                    System.out.println("Python line [" + lineNum + "]: " + line);
                    if (!line.trim().isEmpty()) {
                        lastLine = line.trim();
                    }
                }
            }

            process.waitFor();
            errThread.join();
            tempImage.delete();

            System.out.println("=== stderr   : " + errors);
            System.out.println("=== last line: " + lastLine);
            System.out.println("=== exit code: " + process.exitValue());

            if (lastLine.isBlank()) {
                throw new IllegalArgumentException(
                        "Python returned no output. Stderr: " + errors
                );
            }

            // ✅ Parse only the last line as JSON
            Map<String, Object> result;
            try {
                result = mapper.readValue(lastLine, Map.class);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Failed to parse JSON.\nLast line: " + lastLine +
                                "\nStderr: " + errors
                );
            }

            // 7. Build reply from prediction result
            if (Boolean.FALSE.equals(result.get("valid"))) {
                reply = (String) result.get("message"); // "Not a leaf"
            } else {
                reply = String.format(
                        "Disease: %s | Confidence: %s%% | Top 3: %s",
                        result.get("disease"),
                        result.get("confidence"),
                        result.get("top3")
                );
            }

        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to run Python script: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Python process was interrupted", ex);
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
        if (trimmed.isEmpty()) {
            return "New chat";
        }
        int max = 40;
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
