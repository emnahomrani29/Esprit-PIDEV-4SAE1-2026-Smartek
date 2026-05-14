package com.smartek.offersservice.config;

import org.springframework.context.annotation.Configuration;

/**
 * Redis cache configuration - disabled.
 * Cache is set to 'none' in application.yml.
 */
@Configuration
public class RedisConfig {
    // No-op: cache.type=none in application.yml disables all caching
}
