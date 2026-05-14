package com.smartek.offersservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign vers auth-service.
 * Permet de valider qu'un userId existe bien dans le système.
 */
@FeignClient(name = "auth-service", fallback = AuthClientFallback.class)
public interface AuthClient {

    @GetMapping("/api/auth/validate/{userId}")
    Boolean validateUser(@PathVariable("userId") Long userId);

    @GetMapping("/api/auth/user/{userId}")
    UserInfoResponse getUserById(@PathVariable("userId") Long userId);
}
