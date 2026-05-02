package com.smartek.examservice.config;

import jakarta.servlet.http.HttpServletResponse;
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
                .exceptionHandling(ex -> ex
                        // Return 401 for unauthenticated requests instead of the default 403
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage()))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/exams/health").permitAll()

                        // Examens : TRAINER pour écriture, authentifié pour lecture
                        .requestMatchers(HttpMethod.POST,   "/api/exams").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.PUT,    "/api/exams/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.DELETE, "/api/exams/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET,    "/api/exams/**").authenticated()

                        // Résultats : LEARNER pour soumettre
                        .requestMatchers(HttpMethod.POST, "/api/exam-results/submit").hasRole("LEARNER")
                        .requestMatchers(HttpMethod.GET,  "/api/exam-results/**").authenticated()

                        // Analytics : TRAINER uniquement
                        .requestMatchers("/api/analytics/**").hasRole("TRAINER")

                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
