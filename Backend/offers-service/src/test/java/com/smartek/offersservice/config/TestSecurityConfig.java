package com.smartek.offersservice.config;

import com.smartek.offersservice.security.JwtAuthFilter;
import com.smartek.offersservice.security.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour les tests WebMvc.
 * Fournit une chaîne de filtres permissive et des mocks pour JwtAuthFilter/JwtService.
 * Les tests @WebMvcTest doivent exclure SecurityConfig ET JwtAuthFilter via excludeFilters.
 */
@TestConfiguration
public class TestSecurityConfig {

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
