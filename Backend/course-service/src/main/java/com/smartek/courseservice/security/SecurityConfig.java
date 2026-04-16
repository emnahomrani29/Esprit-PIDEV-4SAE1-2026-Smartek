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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration de sécurité pour le Course Service.
 *
 * Règles de rôles :
 * - TRAINER : peut créer, modifier, supprimer des cours et chapitres
 * - LEARNER  : peut lire les cours et marquer les complétions
 * - Les deux rôles peuvent accéder aux sessions live en lecture
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
                        .requestMatchers("/api/courses/health").permitAll()

                        // ── Cours ──────────────────────────────────────────────────────────
                        // Seul le TRAINER peut créer / modifier / supprimer un cours
                        .requestMatchers(HttpMethod.POST,   "/api/courses").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/courses/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("TRAINER")
                        // Lecture accessible à tous les utilisateurs authentifiés
                        .requestMatchers(HttpMethod.GET,    "/api/courses/**").authenticated()

                        // ── Chapitres ──────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/courses/*/chapters").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/courses/*/chapters/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/*/chapters/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/courses/*/chapters/**").authenticated()

                        // ── Complétion de cours ────────────────────────────────────────────
                        // Le LEARNER marque ses propres complétions
                        .requestMatchers(HttpMethod.POST,   "/api/courses/*/complete").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/*/uncomplete").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.GET,    "/api/courses/*/is-completed").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/courses/completed-count").authenticated()

                        // ── Sessions live ──────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/courses/sessions").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/courses/*/sessions/**").authenticated()

                        // ── Statistiques ───────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/courses/stats/**").authenticated()

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
