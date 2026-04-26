package com.smartek.courseservice.security;

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
 * Configuration de sécurité pour le Course Service.
 * Le CORS est géré exclusivement par l'API Gateway (globalcors).
 * Ne pas configurer le CORS ici pour éviter les headers dupliqués.
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
                        // Actuator & health
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/courses/health").permitAll()
                        // WebSocket signaling — pas d'auth HTTP possible lors du handshake WS
                        .requestMatchers("/ws/signaling", "/ws/signaling/**").permitAll()

                        // ── Complétion de cours (LEARNER) — avant les règles génériques ───
                        .requestMatchers(HttpMethod.POST,   "/api/courses/*/complete").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/*/uncomplete").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.GET,    "/api/courses/*/is-completed").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/courses/completed-count").authenticated()

                        // ── Sessions live — avant les règles génériques ────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/courses/sessions").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/courses/course/*/sessions").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/courses/course/*/sessions/**").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/courses/sessions/**").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/courses/trainer/*/sessions").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/courses/sessions/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PATCH,  "/api/courses/sessions/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/sessions/**").hasRole("TRAINER")

                        // ── Chapitres — avant les règles génériques ────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/courses/*/chapters").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/courses/*/chapters/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/*/chapters/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/courses/*/chapters/**").authenticated()

                        // ── Statistiques ───────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/courses/stats/**").authenticated()

                        // ── Cours — règles génériques ──────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/courses").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/courses/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/courses/**").authenticated()

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
