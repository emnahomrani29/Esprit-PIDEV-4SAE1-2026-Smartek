package com.smartek.courseservice.constants;

/**
 * Constants for error messages.
 * Centralizes error message definitions to avoid duplication.
 */
public final class ErrorMessages {
    
    // Chapter Messages
    public static final String CHAPTER_NOT_FOUND = "Chapitre non trouvé avec l'ID: ";
    
    // Session Messages
    public static final String SESSION = "Session";
    
    // Prevent instantiation
    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
