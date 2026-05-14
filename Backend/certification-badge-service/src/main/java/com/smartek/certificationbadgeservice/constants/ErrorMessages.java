package com.smartek.certificationbadgeservice.constants;

/**
 * Constants for error messages.
 * Centralizes error message definitions to avoid duplication.
 */
public final class ErrorMessages {
    
    // Badge Template Messages
    public static final String BADGE_TEMPLATE_NOT_FOUND_LOG = "Badge template not found with id: {}";
    public static final String BADGE_TEMPLATE_NOT_FOUND = "Badge template not found with id: ";
    
    // Certification Template Messages
    public static final String CERTIFICATION_TEMPLATE_NOT_FOUND_LOG = "Certification template not found with id: {}";
    public static final String CERTIFICATION_TEMPLATE_NOT_FOUND = "Certification template not found with id: ";
    
    // Prevent instantiation
    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
