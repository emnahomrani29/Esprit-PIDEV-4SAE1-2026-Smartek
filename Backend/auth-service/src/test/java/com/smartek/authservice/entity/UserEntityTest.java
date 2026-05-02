package com.smartek.authservice.entity;

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

@DisplayName("User Entity - Tests de validation")
class UserEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Validation des contraintes")
    class ValidationConstraints {

        @Test
        @DisplayName("Entité valide - aucune violation")
        void validUser_noViolations() {
            User user = User.builder()
                    .firstName("Alice")
                    .email("alice@smartek.com")
                    .password("password123")
                    .phone("0612345678")
                    .role(RoleType.LEARNER)
                    .experience(2)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Prénom vide - violation @NotBlank")
        void blankFirstName_violation() {
            User user = User.builder()
                    .firstName("")
                    .email("alice@smartek.com")
                    .password("password123")
                    .role(RoleType.LEARNER)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
        }

        @Test
        @DisplayName("Email invalide - violation @Email")
        void invalidEmail_violation() {
            User user = User.builder()
                    .firstName("Alice")
                    .email("not-an-email")
                    .password("password123")
                    .role(RoleType.LEARNER)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("Mot de passe trop court - violation @Size(min=8)")
        void shortPassword_violation() {
            User user = User.builder()
                    .firstName("Alice")
                    .email("alice@smartek.com")
                    .password("short")
                    .role(RoleType.LEARNER)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }
    }

    @Nested
    @DisplayName("Builder et valeurs par défaut")
    class BuilderAndDefaults {

        @Test
        @DisplayName("Builder crée un utilisateur avec les bons champs")
        void builder_createsUserCorrectly() {
            User user = User.builder()
                    .userId(1L)
                    .firstName("Bob")
                    .email("bob@smartek.com")
                    .password("securepass")
                    .role(RoleType.TRAINER)
                    .experience(5)
                    .build();

            assertThat(user.getUserId()).isEqualTo(1L);
            assertThat(user.getFirstName()).isEqualTo("Bob");
            assertThat(user.getEmail()).isEqualTo("bob@smartek.com");
            assertThat(user.getRole()).isEqualTo(RoleType.TRAINER);
            assertThat(user.getExperience()).isEqualTo(5);
        }

        @Test
        @DisplayName("Expérience par défaut est 0 ou null si non défini")
        void defaultExperience_isZeroOrNull() {
            User user = User.builder()
                    .firstName("Alice")
                    .email("alice@smartek.com")
                    .password("password123")
                    .role(RoleType.LEARNER)
                    .build();

            // Le builder Lombok ne set pas la valeur par défaut JPA — experience peut être null
            assertThat(user.getExperience()).isIn(0, null);
        }

        @Test
        @DisplayName("Tous les rôles sont supportés")
        void allRoles_areSupported() {
            for (RoleType role : RoleType.values()) {
                User user = User.builder()
                        .firstName("Test")
                        .email("test@smartek.com")
                        .password("password123")
                        .role(role)
                        .build();
                assertThat(user.getRole()).isEqualTo(role);
            }
        }
    }
}
