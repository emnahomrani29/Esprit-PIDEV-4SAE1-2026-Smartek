package com.smartek.authservice.dto;

import com.smartek.authservice.enums.RoleType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DTO Validation - auth-service")
class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RegisterRequest
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("RegisterRequest")
    class RegisterRequestValidation {

        @Test
        @DisplayName("Requête valide - aucune violation")
        void validRequest_noViolations() {
            RegisterRequest req = RegisterRequest.builder()
                    .firstName("Alice")
                    .email("alice@smartek.com")
                    .password("password123")
                    .role(RoleType.LEARNER)
                    .build();

            assertThat(validator.validate(req)).isEmpty();
        }

        @Test
        @DisplayName("Prénom null → violation")
        void nullFirstName_violation() {
            RegisterRequest req = RegisterRequest.builder()
                    .firstName(null)
                    .email("alice@smartek.com")
                    .password("password123")
                    .role(RoleType.LEARNER)
                    .build();

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
        }

        @Test
        @DisplayName("Email invalide → violation")
        void invalidEmail_violation() {
            RegisterRequest req = RegisterRequest.builder()
                    .firstName("Alice")
                    .email("not-valid")
                    .password("password123")
                    .role(RoleType.LEARNER)
                    .build();

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("Mot de passe < 8 caractères → violation")
        void shortPassword_violation() {
            RegisterRequest req = RegisterRequest.builder()
                    .firstName("Alice")
                    .email("alice@smartek.com")
                    .password("short")
                    .role(RoleType.LEARNER)
                    .build();

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }

        @Test
        @DisplayName("Rôle null → violation")
        void nullRole_violation() {
            RegisterRequest req = RegisterRequest.builder()
                    .firstName("Alice")
                    .email("alice@smartek.com")
                    .password("password123")
                    .role(null)
                    .build();

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("role"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LoginRequest
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("LoginRequest")
    class LoginRequestValidation {

        @Test
        @DisplayName("Requête valide - aucune violation")
        void validRequest_noViolations() {
            LoginRequest req = new LoginRequest();
            req.setEmail("alice@smartek.com");
            req.setPassword("password123");

            assertThat(validator.validate(req)).isEmpty();
        }

        @Test
        @DisplayName("Email vide → violation")
        void emptyEmail_violation() {
            LoginRequest req = new LoginRequest();
            req.setEmail("");
            req.setPassword("password123");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AuthResponse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("AuthResponse")
    class AuthResponseBuilder {

        @Test
        @DisplayName("Builder crée une réponse complète")
        void builder_createsCompleteResponse() {
            AuthResponse response = AuthResponse.builder()
                    .token("jwt-token")
                    .userId(1L)
                    .email("alice@smartek.com")
                    .firstName("Alice")
                    .role(RoleType.LEARNER)
                    .message("Connexion réussie")
                    .build();

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getType()).isEqualTo("Bearer");
            assertThat(response.getUserId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("alice@smartek.com");
            assertThat(response.getRole()).isEqualTo(RoleType.LEARNER);
            assertThat(response.getMessage()).isEqualTo("Connexion réussie");
        }

        @Test
        @DisplayName("Type par défaut est Bearer")
        void defaultType_isBearer() {
            AuthResponse response = AuthResponse.builder().build();
            assertThat(response.getType()).isEqualTo("Bearer");
        }
    }
}
