package com.smartek.skillevidenceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign pour communiquer avec l'auth-service.
 * Permet de valider l'existence d'un utilisateur (reviewer) avant d'approuver/rejeter une preuve.
 */
@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthServiceClient {

    @GetMapping("/validate/{userId}")
    Boolean validateUser(@PathVariable("userId") Long userId);

    @GetMapping("/user/{userId}")
    UserSummary getUserById(@PathVariable("userId") Long userId);

    record UserSummary(
            Long userId,
            String email,
            String firstName,
            String role,
            Integer experience,
            String message
    ) {}
}
