package com.smartek.certificationbadgeservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InternalApiAuthService {
    
    @Value("${internal.api-key}")
    private String expectedApiKey;
    
    public boolean validate(String provided) {
        return provided != null && provided.equals(expectedApiKey);
    }
}
