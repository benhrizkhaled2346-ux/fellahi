package com.appfor.ne3ma.config;

import com.appfor.ne3ma.dto.MessageResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<MessageResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        MessageResponse response = new MessageResponse(
                "Image is too large. Maximum allowed size is 10MB.",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }
}
