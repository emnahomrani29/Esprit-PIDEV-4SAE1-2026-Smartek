package com.smartek.certificationbadgeservice.client;

import com.smartek.certificationbadgeservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for communicating with the Auth Service.
 * Used to fetch user details (e.g., email) by learnerId.
 */
@FeignClient(name = "auth-service", url = "${auth-service.base-url}")
public interface AuthServiceClient {

    @GetMapping("/api/auth/user/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);
}
