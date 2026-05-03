package com.smartek.examservice.constants;

/**
 * Constants for API endpoints.
 * Centralizes endpoint definitions to avoid duplication.
 */
public final class ApiEndpoints {
    
    // Exam endpoints
    public static final String API_EXAMS = "/api/exams/**";
    
    // Prevent instantiation
    private ApiEndpoints() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
