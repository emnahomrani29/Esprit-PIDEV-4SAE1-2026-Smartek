package com.smartek.offersservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics (lecture des offres actives)
                .requestMatchers(HttpMethod.GET, "/api/offers/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/status/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/{id}").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/offers/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/top-viewed").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/company/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/stats/company/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/offers").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/offers/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/offers/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offers/my").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
