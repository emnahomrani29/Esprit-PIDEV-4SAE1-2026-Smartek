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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration de sécurité pour l'Exam Service.
 *
 * Règles de rôles :
 * - TRAINER : peut créer, modifier, supprimer des examens et questions
 * - LEARNER  : peut soumettre des réponses, consulter ses résultats
 * - Les deux rôles peuvent lire les examens disponibles
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Actuator
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Health check interne
                        .requestMatchers("/api/exams/health").permitAll()

                        // ── Examens ────────────────────────────────────────────────────────
                        // Seul le TRAINER peut créer / modifier / supprimer
                        .requestMatchers(HttpMethod.POST,   "/api/exams").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/exams/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/exams/**").hasRole("TRAINER")
                        // Lecture accessible à tous les utilisateurs authentifiés
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
                        // Le LEARNER soumet ses réponses
                        .requestMatchers(HttpMethod.POST,   "/api/exam-results/submit").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.POST,   "/api/exam-results/submit-old").hasRole("LEARNER")
                        // Lecture des résultats : les deux rôles
                        .requestMatchers(HttpMethod.GET,    "/api/exam-results/**").authenticated()

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("PATCH");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
