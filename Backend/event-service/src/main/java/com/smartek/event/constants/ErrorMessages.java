package com.smartek.event.constants;

/**
 * Constants for error messages.
 * Centralizes error message definitions to avoid duplication.
 */
public final class ErrorMessages {
    
    // Event Messages
    public static final String EVENT_NOT_FOUND_WITH_ID = "Event not found with id: ";
    public static final String EVENT_NOT_FOUND = "Event not found";
    
    // Prevent instantiation
    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
