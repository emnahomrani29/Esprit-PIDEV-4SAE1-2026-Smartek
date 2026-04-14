package com.smartek.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * Filtre global JWT au niveau de l'API Gateway.
 * Valide le token Bearer avant de router la requête vers les microservices.
 * Les routes publiques (auth, health, offres publiques) sont exemptées.
 */
@Component
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGatewayFilter.class);

    @Value("${jwt.secret}")
    private String secret;

    // Routes accessibles sans token
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/health",
        "/api/offers/health",
        "/api/offers/top-viewed",
        "/api/offers/search",
        "/api/skill-evidence/analytics",
        "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // Laisser passer les routes publiques
        if (isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for: {} {}", method, path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.getExpiration().before(new Date())) {
                log.warn("Expired JWT token for: {} {}", method, path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Propager les infos utilisateur dans les headers vers les microservices
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId")))
                    .header("X-User-Role", String.valueOf(claims.get("role")))
                    .header("X-User-Email", claims.getSubject())
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {} for: {} {}", e.getMessage(), method, path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path, String method) {
        if ("OPTIONS".equals(method)) return true;
        // GET sur les offres actives est public
        if ("GET".equals(method) && path.matches("/api/offers/status/ACTIVE.*")) return true;
        if ("GET".equals(method) && path.matches("/api/offers/\\d+")) return true;
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public int getOrder() {
        return -100; // Avant tous les autres filtres
    }
}
