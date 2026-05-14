package com.smartek.certificationbadgeservice.exception;

public class DuplicateAwardException extends RuntimeException {
    
    public DuplicateAwardException(String message) {
        super(message);
    }
    
    public DuplicateAwardException(String message, Throwable cause) {
        super(message, cause);
    }
}
