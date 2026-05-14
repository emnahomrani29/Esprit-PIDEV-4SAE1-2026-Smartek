package com.smartek.authservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.smartek.authservice.dto.AuthResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AuthResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        log.error("Runtime exception: {}", ex.getMessage());
        
        // Determine status based on exception message
        HttpStatus status = determineStatus(ex.getMessage());
        
        AuthResponse response = AuthResponse.builder()
                .message(ex.getMessage())
                .build();
        
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "Erreur de validation des données");
        response.put("errors", errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<AuthResponse> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        log.error("User not found: {}", ex.getMessage());
        
        AuthResponse response = AuthResponse.builder()
                .message(ex.getMessage())
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        
        AuthResponse response = AuthResponse.builder()
                .message("Une erreur interne s'est produite")
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Determine HTTP status based on exception message
     */
    private HttpStatus determineStatus(String message) {
        if (message == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("non trouvé") || lowerMessage.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        } else if (lowerMessage.contains("déjà utilisé") || lowerMessage.contains("already exists") || 
                   lowerMessage.contains("duplicate")) {
            return HttpStatus.BAD_REQUEST;
        } else if (lowerMessage.contains("credentials") || lowerMessage.contains("invalide") || 
                   lowerMessage.contains("incorrect")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (lowerMessage.contains("forbidden") || lowerMessage.contains("interdit")) {
            return HttpStatus.FORBIDDEN;
        }
        
        return HttpStatus.BAD_REQUEST;
    }
}
