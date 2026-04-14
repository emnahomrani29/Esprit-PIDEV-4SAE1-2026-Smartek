package com.smartek.examservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface UserClient {
    
    @GetMapping("/api/auth/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);
}
