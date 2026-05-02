package com.smartek.courseservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour les tests.
 * Reproduit les règles de rôles du SecurityConfig principal
 * sans le filtre JWT (inutile en test avec @WithMockUser).
 */
@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/courses/health").permitAll()

                        // Cours : TRAINER pour écriture, authentifié pour lecture
                        .requestMatchers(HttpMethod.POST,   "/api/courses").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/courses/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/courses/**").authenticated()

                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
