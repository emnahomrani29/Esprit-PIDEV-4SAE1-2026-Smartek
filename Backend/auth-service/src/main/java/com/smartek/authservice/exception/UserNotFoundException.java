package com.smartek.authservice.exception;

/**
 * Custom exception thrown when a user is not found.
 * Replaces generic RuntimeException for better error handling.
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long userId) {
        super("Utilisateur non trouvé avec l'ID: " + userId);
    }
}
