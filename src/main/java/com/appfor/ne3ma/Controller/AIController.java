package com.appfor.ne3ma.Controller;

import com.appfor.ne3ma.dto.ImageResponse;
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

        MessageResponse response = aiService.processMessage(request, request.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> analyzeImage(
            @RequestParam("photo") MultipartFile image,
            @RequestParam("email") String email

    ) {
        ImageResponse response = aiService.analyzeImage(image, email);
        return ResponseEntity.ok(response);
    }
}
