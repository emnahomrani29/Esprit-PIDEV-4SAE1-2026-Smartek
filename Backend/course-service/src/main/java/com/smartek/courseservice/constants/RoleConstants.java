package com.smartek.courseservice.constants;

/**
 * Constants for user roles used throughout the application.
 * Centralizes role definitions to avoid duplication and ensure consistency.
 */
public final class RoleConstants {
    
    // User Roles
    public static final String TRAINER = "TRAINER";
    public static final String LEARNER = "LEARNER";
    public static final String ADMIN = "ADMIN";
    
    // Prevent instantiation
    private RoleConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
