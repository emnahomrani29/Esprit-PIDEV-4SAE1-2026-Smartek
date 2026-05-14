package com.smartek.authservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes
 * Run this to generate hashes for seed data
 */
public class PasswordHashGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordHashGenerator.class);
    
    public static void main(String[] args) {
        if (args.length < 1) {
            logger.info("Usage: java PasswordHashGenerator <password>");
            logger.info("Example: java PasswordHashGenerator MySecurePassword123");
            System.exit(1);
        }
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String inputPassword = args[0];
        
        logger.info("=== SMARTEK Password Hash Generator ===");
        
        String passwordHash = encoder.encode(inputPassword);
        logger.info("BCrypt Hash: {}", passwordHash);
        logger.info("Verify: {}", encoder.matches(inputPassword, passwordHash));
        
        logger.info("=== SQL INSERT EXAMPLE ===");
        logger.info("INSERT INTO users (first_name, email, password, phone, role, experience)");
        logger.info("VALUES ('FirstName', 'email@example.com', '{}', '+33123456789', 'ROLE', 0);", passwordHash);
    }
}
