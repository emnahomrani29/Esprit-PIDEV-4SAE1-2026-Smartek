package com.smartek.authservice.exception;

/**
 * Custom exception thrown when attempting to register with an email that already exists.
 * Replaces generic RuntimeException for better error handling.
 */
public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String email) {
        super("Cet email est déjà utilisé: " + email);
    }
    
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
