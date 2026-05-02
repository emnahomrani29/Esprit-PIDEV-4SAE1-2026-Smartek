package com.smartek.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService - Tests unitaires")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "smartek-secret-key-for-jwt-token-generation-2024-very-secure";
    private static final long EXPIRATION = 86400000L; // 24h

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // generateToken
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("Génère un token non null pour un email valide")
        void generateToken_returnsNonNullToken() {
            String token = jwtService.generateToken("alice@smartek.com");

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Génère un token avec claims supplémentaires")
        void generateToken_withClaims_returnsToken() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "LEARNER");
            claims.put("userId", 1L);

            String token = jwtService.generateToken("alice@smartek.com", claims);

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Deux tokens générés pour le même utilisateur peuvent être identiques si générés dans la même seconde")
        void generateToken_twoTokens_generatedSuccessfully() throws InterruptedException {
            String token1 = jwtService.generateToken("alice@smartek.com");
            // JWT iat est en secondes — attendre 1100ms pour garantir des tokens différents
            Thread.sleep(1100);
            String token2 = jwtService.generateToken("alice@smartek.com");

            assertThat(token1).isNotNull().isNotBlank();
            assertThat(token2).isNotNull().isNotBlank();
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // extractUsername
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("extractUsername")
    class ExtractUsername {

        @Test
        @DisplayName("Extrait correctement l'email du token")
        void extractUsername_returnsCorrectEmail() {
            String token = jwtService.generateToken("alice@smartek.com");

            String username = jwtService.extractUsername(token);

            assertThat(username).isEqualTo("alice@smartek.com");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateToken
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("Token valide pour le bon utilisateur - retourne true")
        void validateToken_validToken_returnsTrue() {
            String token = jwtService.generateToken("alice@smartek.com");
            UserDetails userDetails = new User("alice@smartek.com", "password",
                    Collections.emptyList());

            boolean valid = jwtService.validateToken(token, userDetails);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Token pour un autre utilisateur - retourne false")
        void validateToken_wrongUser_returnsFalse() {
            String token = jwtService.generateToken("alice@smartek.com");
            UserDetails otherUser = new User("bob@smartek.com", "password",
                    Collections.emptyList());

            boolean valid = jwtService.validateToken(token, otherUser);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("Token expiré - lève ExpiredJwtException ou retourne false")
        void validateToken_expiredToken_returnsFalse() {
            ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
            String expiredToken = jwtService.generateToken("alice@smartek.com");
            UserDetails userDetails = new User("alice@smartek.com", "password",
                    Collections.emptyList());

            // Le service peut soit retourner false, soit lever une exception selon l'implémentation
            try {
                boolean valid = jwtService.validateToken(expiredToken, userDetails);
                assertThat(valid).isFalse();
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Comportement attendu : token expiré → exception
                assertThat(e).isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
            }
        }
    }
}
