package com.smartek.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.authservice.dto.AuthResponse;
import com.smartek.authservice.dto.LoginRequest;
import com.smartek.authservice.dto.RegisterRequest;
import com.smartek.authservice.enums.RoleType;
import com.smartek.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.smartek.authservice.config.TestSecurityConfig.class)
@DisplayName("AuthController - Tests d'intégration Web")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService authService;

    private AuthResponse successResponse;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        successResponse = AuthResponse.builder()
                .token("jwt-token")
                .userId(1L)
                .email("alice@smartek.com")
                .firstName("Alice")
                .role(RoleType.LEARNER)
                .message("Succès")
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Alice");
        registerRequest.setEmail("alice@smartek.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(RoleType.LEARNER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@smartek.com");
        loginRequest.setPassword("password123");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("Inscription réussie - retourne 201 avec token")
        void register_success_returns201() throws Exception {
            when(authService.register(any(RegisterRequest.class))).thenReturn(successResponse);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.email").value("alice@smartek.com"))
                    .andExpect(jsonPath("$.message").value("Succès"));
        }

        @Test
        @DisplayName("Email déjà utilisé - retourne 400")
        void register_emailExists_returns400() throws Exception {
            when(authService.register(any())).thenThrow(new RuntimeException("Email déjà utilisé"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Email déjà utilisé"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("Connexion réussie - retourne 200 avec token")
        void login_success_returns200() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenReturn(successResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.email").value("alice@smartek.com"));
        }

        @Test
        @DisplayName("Mauvais credentials - retourne 401")
        void login_badCredentials_returns401() throws Exception {
            when(authService.login(any())).thenThrow(new RuntimeException("Email ou mot de passe incorrect"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Email ou mot de passe incorrect"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/auth/health
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/auth/health")
    class HealthEndpoint {

        @Test
        @DisplayName("Health check - retourne 200")
        void health_returns200() throws Exception {
            mockMvc.perform(get("/api/auth/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Auth Service is running"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/auth/validate/{userId}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/auth/validate/{userId}")
    class ValidateEndpoint {

        @Test
        @DisplayName("Utilisateur valide - retourne true")
        void validate_validUser_returnsTrue() throws Exception {
            when(authService.validateUser(1L)).thenReturn(true);

            mockMvc.perform(get("/api/auth/validate/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("Utilisateur invalide - retourne false")
        void validate_invalidUser_returnsFalse() throws Exception {
            when(authService.validateUser(99L)).thenReturn(false);

            mockMvc.perform(get("/api/auth/validate/99"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/auth/user/{userId}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/auth/user/{userId}")
    class GetUserEndpoint {

        @Test
        @DisplayName("Utilisateur trouvé - retourne 200 avec données")
        void getUser_found_returns200() throws Exception {
            when(authService.getUserById(1L)).thenReturn(successResponse);

            mockMvc.perform(get("/api/auth/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.email").value("alice@smartek.com"));
        }

        @Test
        @DisplayName("Utilisateur non trouvé - retourne 404")
        void getUser_notFound_returns404() throws Exception {
            when(authService.getUserById(99L)).thenThrow(new RuntimeException("Utilisateur non trouvé"));

            mockMvc.perform(get("/api/auth/user/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Utilisateur non trouvé"));
        }
    }
}
