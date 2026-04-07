package com.appfor.ne3ma.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public ResponseEntity<MessageResponse> chat(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageResponse response = aiService.processMessage(
                request.getMessage(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok(response);
    }
}
