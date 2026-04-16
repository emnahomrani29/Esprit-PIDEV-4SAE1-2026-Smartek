package com.smartek.courseservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtre JWT qui intercepte chaque requête pour valider le token Bearer.
 * Extrait l'utilisateur et son rôle depuis le token et les place dans le SecurityContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            if (!jwtService.validateToken(jwt)) {
                log.warn("Token JWT invalide ou expiré pour la requête: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            final String username = jwtService.extractUsername(jwt);
            final Long userId = jwtService.extractUserId(jwt);
            final String role = jwtService.extractRole(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetailsImpl userDetails = new UserDetailsImpl(userId, username, role);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Utilisateur authentifié: {} avec le rôle: {}", username, role);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Erreur lors du traitement du token JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}
