package com.appfor.ne3ma.Controller;

import lombok.RequiredArgsConstructor;
import com.appfor.ne3ma.dto.ChatRequest;
import com.appfor.ne3ma.dto.MessageResponse;
import com.appfor.ne3ma.service.AIService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public ResponseEntity<MessageResponse> chat(
            @Valid @RequestBody ChatRequest request) {

        MessageResponse response = aiService.processMessage(request, request.getUsername());

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> analyzeImage(
            @RequestParam("photo") MultipartFile image,
            @RequestParam("username") String username

    ) {
        MessageResponse response = aiService.analyzeImage(image, username);
        return ResponseEntity.ok(response);
    }
}
