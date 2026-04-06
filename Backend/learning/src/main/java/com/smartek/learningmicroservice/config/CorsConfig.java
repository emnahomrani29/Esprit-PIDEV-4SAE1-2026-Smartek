package com.smartek.learningmicroservice.config;

import org.springframework.context.annotation.Configuration;

// CORS is handled by the API Gateway (CorsWebFilter).
// No local CORS config needed in this microservice.
@Configuration
public class CorsConfig {
}

