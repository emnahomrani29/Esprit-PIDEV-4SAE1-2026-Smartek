package com.smartek.offersservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService — Tests unitaires")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "test-secret-key-for-jwt-token-generation-2024-very-secure-test";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    private String buildToken(String username, Long userId, String role, long expirationMs) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        Map<String, Object> claims = new HashMap<>();
        if (userId != null) claims.put("userId", userId);
        if (role != null) claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ─── extractUsername ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Retourne le username du token valide")
        void shouldExtractUsername() {
            String token = buildToken("alice@test.com", 1L, "LEARNER", 3_600_000);
            assertThat(jwtService.extractUsername(token)).isEqualTo("alice@test.com");
        }
    }

    // ─── extractUserId ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("extractUserId()")
    class ExtractUserIdTests {

        @Test
        @DisplayName("Retourne le userId du token")
        void shouldExtractUserId() {
            String token = buildToken("alice@test.com", 42L, "LEARNER", 3_600_000);
            assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        }

        @Test
        @DisplayName("Retourne null si userId absent du token")
        void shouldReturnNull_whenUserIdAbsent() {
            String token = buildToken("alice@test.com", null, "LEARNER", 3_600_000);
            assertThat(jwtService.extractUserId(token)).isNull();
        }
    }

    // ─── extractRole ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("extractRole()")
    class ExtractRoleTests {

        @Test
        @DisplayName("Retourne le rôle du token")
        void shouldExtractRole() {
            String token = buildToken("trainer@test.com", 5L, "TRAINER", 3_600_000);
            assertThat(jwtService.extractRole(token)).isEqualTo("TRAINER");
        }

        @Test
        @DisplayName("Retourne null si rôle absent du token")
        void shouldReturnNull_whenRoleAbsent() {
            String token = buildToken("user@test.com", 1L, null, 3_600_000);
            assertThat(jwtService.extractRole(token)).isNull();
        }
    }

    // ─── isTokenValid ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValidTests {

        @Test
        @DisplayName("Token valide → true")
        void validToken_returnsTrue() {
            String token = buildToken("alice@test.com", 1L, "LEARNER", 3_600_000);
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("Token expiré → false")
        void expiredToken_returnsFalse() {
            String token = buildToken("alice@test.com", 1L, "LEARNER", -1000);
            assertThat(jwtService.isTokenValid(token)).isFalse();
        }

        @Test
        @DisplayName("Token malformé → false")
        void malformedToken_returnsFalse() {
            assertThat(jwtService.isTokenValid("not.a.valid.token")).isFalse();
        }

        @Test
        @DisplayName("Token vide → false")
        void emptyToken_returnsFalse() {
            assertThat(jwtService.isTokenValid("")).isFalse();
        }

        @Test
        @DisplayName("Token avec mauvaise signature → false")
        void wrongSignatureToken_returnsFalse() {
            Key wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-that-is-long-enough-for-hmac-256".getBytes());
            String token = Jwts.builder()
                    .setSubject("alice@test.com")
                    .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                    .signWith(wrongKey, SignatureAlgorithm.HS256)
                    .compact();
            assertThat(jwtService.isTokenValid(token)).isFalse();
        }
    }
}
