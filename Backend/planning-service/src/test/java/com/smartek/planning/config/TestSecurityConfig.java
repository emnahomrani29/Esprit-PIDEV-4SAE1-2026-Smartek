package com.smartek.planning.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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
                        .requestMatchers("/api/plannings/health").permitAll()
                        .requestMatchers("/api/plannings/business/health").permitAll()

                        // Plannings publiés : tous les authentifiés
                        .requestMatchers(HttpMethod.GET, "/api/plannings/published").authenticated()

                        // Gestion : TRAINER uniquement
                        .requestMatchers(HttpMethod.POST,   "/api/plannings").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/plannings/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/plannings/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.POST,   "/api/plannings/*/publish").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.POST,   "/api/plannings/*/unpublish").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/plannings/**").hasRole("TRAINER")

                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
