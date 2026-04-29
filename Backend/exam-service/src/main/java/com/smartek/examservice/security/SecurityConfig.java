package com.smartek.examservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de sécurité pour l'Exam Service.
 * Le CORS est géré exclusivement par l'API Gateway (globalcors).
 *
 * Règles de rôles :
 * - TRAINER : peut créer, modifier, supprimer des examens et questions
 * - LEARNER  : peut soumettre des réponses, consulter ses résultats
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // CORS désactivé côté service : géré par l'API Gateway (globalcors)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Preflight OPTIONS — toujours permis
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Actuator
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus", "/actuator/metrics").permitAll()
                        // Health check interne
                        .requestMatchers("/api/exams/health").permitAll()

                        // ── Examens ────────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/exams").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/exams/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/exams/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/exams/**").authenticated()

                        // ── Questions ──────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/questions").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/questions/**").authenticated()

                        // ── Exercices ──────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/exams/*/exercises").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/exams/*/exercises/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/exams/*/exercises/**").authenticated()

                        // ── Résultats d'examen ─────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/exam-results/submit").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.POST,   "/api/exam-results/submit-old").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.POST,   "/api/exams/*/submit-quiz").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.POST,   "/api/exams/*/submit-exam").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.GET,    "/api/exam-results/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/exam-results/*/finalize").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/exam-results/pending").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/exams/exercise-answers/*/correct").hasRole("TRAINER")

                        // ── Enrollments ────────────────────────────────────────────────────
                        .requestMatchers("/api/exam-enrollments/**").authenticated()

                        // ── Brouillons ─────────────────────────────────────────────────────
                        .requestMatchers("/api/exams/*/draft/**").hasRole("LEARNER")

                        // ── Analytics (TRAINER uniquement) ─────────────────────────────────
                        .requestMatchers("/api/analytics/**").hasRole("TRAINER")

                        // Tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
