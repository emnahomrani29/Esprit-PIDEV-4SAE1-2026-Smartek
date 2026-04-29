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

/**
 * Configuration de sécurité pour le Planning Service.
 * Le CORS est géré exclusivement par l'API Gateway (globalcors).
 *
 * Règles de rôles :
 * - TRAINER : peut créer, modifier, supprimer et publier des plannings
 * - LEARNER  : peut consulter les plannings publiés uniquement
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
                        // Health checks
                        .requestMatchers("/api/plannings/health").permitAll()
                        .requestMatchers("/api/plannings/business/health").permitAll()

                        // ── Plannings publiés (LEARNER peut lire) ─────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/plannings/published").authenticated()

                        // ── Gestion des plannings (TRAINER uniquement) ─────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/plannings").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/plannings/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/plannings/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.POST,   "/api/plannings/*/publish").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.POST,   "/api/plannings/*/unpublish").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/plannings/**").hasRole("TRAINER")

                        // ── Planning hebdomadaire ──────────────────────────────────────────
                        .requestMatchers("/api/plannings/weekly/**").hasRole("TRAINER")

                        // ── Logique métier ─────────────────────────────────────────────────
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
}
