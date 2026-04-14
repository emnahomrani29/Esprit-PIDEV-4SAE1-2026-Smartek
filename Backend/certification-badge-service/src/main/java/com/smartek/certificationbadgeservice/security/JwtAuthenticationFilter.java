package com.smartek.certificationbadgeservice.security;

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
 * JWT Authentication Filter that intercepts requests to validate JWT tokens.
 * Extracts JWT from Authorization header, validates it, and sets SecurityContext.
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
        
        // Skip filter if no Authorization header or not Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Extract JWT token
            final String jwt = authHeader.substring(7);
            
            // Validate token
            if (!jwtService.validateToken(jwt)) {
                log.warn("Invalid or expired JWT token - allowing request to continue (permitAll configured)");
                // Don't block the request - let Spring Security decide based on permitAll configuration
                filterChain.doFilter(request, response);
                return;
            }
            
            // Extract user details from token
            final String username = jwtService.extractUsername(jwt);
            final Long userId = jwtService.extractUserId(jwt);
            final String role = jwtService.extractRole(jwt);
            
            // Set authentication in SecurityContext if not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Create UserDetails with role
                UserDetailsImpl userDetails = new UserDetailsImpl(userId, username, role);
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("Authenticated user: {} with role: {}", username, role);
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Error processing JWT token: {} - allowing request to continue", e.getMessage());
            // Don't block the request - let Spring Security decide based on configuration
            filterChain.doFilter(request, response);
        }
    }
}
