package com.smartek.authservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes
 * Run this to generate hashes for seed data
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== SMARTEK Password Hash Generator ===\n");
        
        // Generate hash for Formateur123
        String formateurPassword = "Formateur123";
        String formateurHash = encoder.encode(formateurPassword);
        System.out.println("Password: " + formateurPassword);
        System.out.println("BCrypt Hash: " + formateurHash);
        System.out.println("Verify: " + encoder.matches(formateurPassword, formateurHash));
        System.out.println();
        
        // Generate hash for Learner123
        String learnerPassword = "Learner123";
        String learnerHash = encoder.encode(learnerPassword);
        System.out.println("Password: " + learnerPassword);
        System.out.println("BCrypt Hash: " + learnerHash);
        System.out.println("Verify: " + encoder.matches(learnerPassword, learnerHash));
        System.out.println();
        
        System.out.println("=== SQL INSERT STATEMENTS ===\n");
        System.out.println("-- Delete existing users");
        System.out.println("DELETE FROM users WHERE email IN ('Formateur@smartek.com', 'Learner@smartek.com');");
        System.out.println();
        System.out.println("-- Insert Trainer");
        System.out.println("INSERT INTO users (first_name, email, password, phone, role, experience)");
        System.out.println("VALUES ('Formateur', 'Formateur@smartek.com', '" + formateurHash + "', '+33123456789', 'TRAINER', 5);");
        System.out.println();
        System.out.println("-- Insert Learner");
        System.out.println("INSERT INTO users (first_name, email, password, phone, role, experience)");
        System.out.println("VALUES ('Learner', 'Learner@smartek.com', '" + learnerHash + "', '+33123456790', 'LEARNER', 0);");
    }
}
