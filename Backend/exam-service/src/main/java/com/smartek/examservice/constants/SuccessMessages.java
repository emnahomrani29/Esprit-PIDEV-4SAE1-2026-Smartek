package com.smartek.examservice.constants;

/**
 * Constants for success messages.
 * Centralizes success message definitions to avoid duplication.
 */
public final class SuccessMessages {
    
    // Enrollment Messages
    public static final String QUIZ_UNLOCKED = "Quiz déverrouillé avec succès";
    public static final String ENROLLMENT_CREATED = "Enrollment créé avec succès";
    public static final String ENROLLMENT_NOT_FOUND = "Enrollment non trouvé";
    
    // Prevent instantiation
    private SuccessMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
