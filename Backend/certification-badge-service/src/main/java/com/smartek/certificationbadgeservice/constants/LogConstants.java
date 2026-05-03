package com.smartek.certificationbadgeservice.constants;

/**
 * Constants for logging operations.
 * Centralizes log-related string literals to avoid duplication.
 */
public final class LogConstants {
    
    // Log Keys
    public static final String OPERATION = "operation";
    public static final String USER_ID = "userId";
    public static final String LEARNER = "Learner";
    
    // Prevent instantiation
    private LogConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
