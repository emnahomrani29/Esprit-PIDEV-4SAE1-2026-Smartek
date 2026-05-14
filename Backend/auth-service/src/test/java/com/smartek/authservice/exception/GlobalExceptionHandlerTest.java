package com.smartek.authservice.exception;

import com.smartek.authservice.controller.AuthController;
import com.smartek.authservice.dto.LoginRequest;
import com.smartek.authservice.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du GlobalExceptionHandler via le contexte Spring MVC.
 * Vérifie que les exceptions sont correctement transformées en réponses HTTP.
 */
@WebMvcTest(AuthController.class)
@org.springframework.context.annotation.Import(com.smartek.authservice.config.TestSecurityConfig.class)
@DisplayName("GlobalExceptionHandler (auth-service) - Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("Gestion des erreurs de validation (@Valid)")
    class ValidationErrors {

        @Test
        @DisplayName("POST /register avec email vide → 400 Bad Request")
        @WithMockUser
        void register_emptyEmail_returns400() throws Exception {
            String invalidJson = """
                    {
                        "firstName": "Alice",
                        "email": "",
                        "password": "password123",
                        "role": "LEARNER"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /login avec corps vide → 400 Bad Request")
        @WithMockUser
        void login_emptyBody_returns400() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Gestion des RuntimeException")
    class RuntimeExceptions {

        @Test
        @DisplayName("Email déjà utilisé → 400 avec message d'erreur")
        @WithMockUser
        void register_duplicateEmail_returns400WithMessage() throws Exception {
            when(authService.register(any()))
                    .thenThrow(new RuntimeException("Email déjà utilisé"));

            String validJson = """
                    {
                        "firstName": "Alice",
                        "email": "alice@smartek.com",
                        "password": "password123",
                        "role": "LEARNER"
                    }
                    """;

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Email déjà utilisé"));
        }

        @Test
        @DisplayName("Mauvais credentials → 401 Unauthorized")
        @WithMockUser
        void login_badCredentials_returns401() throws Exception {
            when(authService.login(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            String validJson = """
                    {
                        "email": "alice@smartek.com",
                        "password": "wrongpassword"
                    }
                    """;

            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validJson))
                    .andExpect(status().isUnauthorized());
        }
    }
}
