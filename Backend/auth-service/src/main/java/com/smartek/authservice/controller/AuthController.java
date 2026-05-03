package com.smartek.authservice.controller;

import com.smartek.authservice.dto.AuthResponse;
import com.smartek.authservice.dto.LoginRequest;
import com.smartek.authservice.dto.RegisterRequest;
import com.smartek.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Requête d'inscription reçue pour: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Requête de connexion reçue pour: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
    
    @GetMapping("/validate/{userId}")
    public ResponseEntity<Boolean> validateUser(@PathVariable Long userId) {
        log.info("Validation de l'utilisateur avec ID: {}", userId);
        boolean isValid = authService.validateUser(userId);
        return ResponseEntity.ok(isValid);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<AuthResponse> getUserById(@PathVariable Long userId) {
        log.info("Récupération des données de l'utilisateur avec ID: {}", userId);
        AuthResponse response = authService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
}
