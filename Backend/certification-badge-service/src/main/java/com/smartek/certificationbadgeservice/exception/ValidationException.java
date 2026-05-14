package com.smartek.certificationbadgeservice.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends RuntimeException {
    
    private final List<String> errors;
    
    public ValidationException(String message) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(message);
    }
    
    public ValidationException(List<String> errors) {
        super(errors.isEmpty() ? "Validation failed" : errors.get(0));
        this.errors = new ArrayList<>(errors);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = new ArrayList<>();
        this.errors.add(message);
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
}
