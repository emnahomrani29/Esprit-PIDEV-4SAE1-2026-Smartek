package com.smartek.authservice.service;

import com.smartek.authservice.dto.AuthResponse;
import com.smartek.authservice.dto.LoginRequest;
import com.smartek.authservice.dto.RegisterRequest;
import com.smartek.authservice.entity.User;
import com.smartek.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentative d'inscription pour l'email: {}", request.getEmail());
        
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }
        
        // Créer le nouvel utilisateur
        User user = User.builder()
                .firstName(request.getFirstName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .image(convertBase64ToBytes(request.getImageBase64()))
                .experience(request.getExperience() != null ? request.getExperience() : 0)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès: {}", savedUser.getEmail());
        
        // Générer le token JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", savedUser.getRole().name());
        claims.put("userId", savedUser.getUserId());
        
        String token = jwtService.generateToken(savedUser.getEmail(), claims);
        
        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .role(savedUser.getRole())
                .imageBase64(convertBytesToBase64(savedUser.getImage()))
                .experience(savedUser.getExperience())
                .message("Inscription réussie")
                .build();
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l'email: {}", request.getEmail());
        
        // Authentifier l'utilisateur
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Générer le token JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getUserId());
        
        String token = jwtService.generateToken(user.getEmail(), claims);
        
        log.info("Connexion réussie pour: {}", user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole())
                .imageBase64(convertBytesToBase64(user.getImage()))
                .experience(user.getExperience())
                .message("Connexion réussie")
                .build();
    }
    
    public boolean validateUser(Long userId) {
        return userRepository.existsById(userId);
    }
    
    public AuthResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole())
                .imageBase64(convertBytesToBase64(user.getImage()))
                .experience(user.getExperience())
                .message("Données utilisateur récupérées")
                .build();
    }
    
    private byte[] convertBase64ToBytes(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la conversion de l'image base64", e);
            return null;
        }
    }
    
    private String convertBytesToBase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("Erreur lors de la conversion des bytes en base64", e);
            return null;
        }
    }
}
