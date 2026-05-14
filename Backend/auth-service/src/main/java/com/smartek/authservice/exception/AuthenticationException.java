package com.smartek.authservice.exception;

/**
 * Custom exception for authentication-related errors.
 * Replaces generic RuntimeException for better error handling.
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
