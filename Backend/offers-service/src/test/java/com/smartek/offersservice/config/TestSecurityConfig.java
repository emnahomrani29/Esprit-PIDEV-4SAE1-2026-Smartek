package com.smartek.offersservice.config;

import com.smartek.offersservice.security.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour les tests WebMvc (@WebMvcTest).
 *
 * - Fournit un mock de JwtService pour satisfaire la dépendance de JwtAuthFilter
 * - Définit une SecurityFilterChain sans filtre JWT (stateless, rôles simulés)
 */
@TestConfiguration
public class TestSecurityConfig {

    // Fournit un mock de JwtService pour que JwtAuthFilter puisse être instancié
    // sans lever UnsatisfiedDependencyException dans @WebMvcTest
    @MockBean
    public JwtService jwtService;

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics
                .requestMatchers(HttpMethod.GET, "/api/offers/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/status/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/{id}").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/offers/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/top-viewed").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/company/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/stats/company/**").permitAll()

                // Gestion des offres : TRAINER ou ADMIN
                .requestMatchers(HttpMethod.POST, "/api/offers").hasAnyRole("TRAINER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/offers/**").hasAnyRole("TRAINER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/offers/**").hasAnyRole("TRAINER", "ADMIN")

                // Candidatures : LEARNER pour postuler/retirer, TRAINER pour statut
                .requestMatchers(HttpMethod.POST, "/api/applications").hasRole("LEARNER")
                .requestMatchers(HttpMethod.PUT, "/api/applications/*/status").hasAnyRole("TRAINER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/applications/*/withdraw").hasRole("LEARNER")
                .requestMatchers(HttpMethod.GET, "/api/applications/**").authenticated()

                // Entretiens et feedbacks : TRAINER ou ADMIN
                .requestMatchers("/api/interviews/**").hasAnyRole("TRAINER", "ADMIN", "LEARNER")
                .requestMatchers("/api/interview-feedbacks/**").hasAnyRole("TRAINER", "ADMIN")

                // Favoris : LEARNER
                .requestMatchers("/api/saved-offers/**").hasAnyRole("LEARNER", "TRAINER", "ADMIN")

                .anyRequest().authenticated()
            );

        return http.build();
    }
}
