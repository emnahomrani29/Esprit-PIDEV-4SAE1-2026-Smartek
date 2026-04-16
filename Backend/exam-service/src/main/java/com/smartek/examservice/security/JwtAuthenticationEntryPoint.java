package com.smartek.examservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Point d'entrée personnalisé pour les erreurs d'authentification.
 * Retourne une réponse 401 avec les détails de l'erreur en JSON.
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.error("Erreur d'authentification: {}", authException.getMessage());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("error", "Non autorisé");
        errorDetails.put("message", "Authentification requise: " + authException.getMessage());
        errorDetails.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}
