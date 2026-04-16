package com.smartek.planning.security;

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
 * Configuration de sécurité pour le Planning Service.
 *
 * Règles de rôles :
 * - TRAINER : peut créer, modifier, supprimer et publier des plannings
 * - LEARNER  : peut consulter les plannings publiés uniquement
 * - ADMIN    : accès complet
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
                        .requestMatchers("/api/plannings/health").permitAll()
                        // Business health check
                        .requestMatchers("/api/plannings/business/health").permitAll()

                        // ── Plannings publiés (LEARNER peut lire) ─────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/plannings/published").authenticated()

                        // ── Gestion des plannings (TRAINER uniquement) ─────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/plannings").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/plannings/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/plannings/**").hasRole("TRAINER")
                        // Publication / dépublication
                        .requestMatchers(HttpMethod.POST,   "/api/plannings/*/publish").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.POST,   "/api/plannings/*/unpublish").hasRole("TRAINER")
                        // Lecture complète (tous les plannings)
                        .requestMatchers(HttpMethod.GET,    "/api/plannings/**").hasRole("TRAINER")

                        // ── Planning hebdomadaire ──────────────────────────────────────────
                        .requestMatchers("/api/plannings/weekly/**").hasRole("TRAINER")

                        // ── Logique métier (conflits, suggestions, charge) ─────────────────
                        .requestMatchers("/api/plannings/business/**").hasRole("TRAINER")

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
