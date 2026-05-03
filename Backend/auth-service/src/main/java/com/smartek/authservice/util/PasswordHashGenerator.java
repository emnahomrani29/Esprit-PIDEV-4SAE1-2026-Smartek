package com.smartek.authservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes
 * Run this to generate hashes for seed data
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java PasswordHashGenerator <password>");
            System.out.println("Example: java PasswordHashGenerator MySecurePassword123");
            System.exit(1);
        }
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String inputPassword = args[0];
        
        System.out.println("=== SMARTEK Password Hash Generator ===\n");
        
        String passwordHash = encoder.encode(inputPassword);
        System.out.println("BCrypt Hash: " + passwordHash);
        System.out.println("Verify: " + encoder.matches(inputPassword, passwordHash));
        System.out.println();
        
        System.out.println("=== SQL INSERT EXAMPLE ===\n");
        System.out.println("INSERT INTO users (first_name, email, password, phone, role, experience)");
        System.out.println("VALUES ('FirstName', 'email@example.com', '" + passwordHash + "', '+33123456789', 'ROLE', 0);");
    }
}
