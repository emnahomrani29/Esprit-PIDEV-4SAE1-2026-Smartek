package com.smartek.courseservice.constants;

/**
 * Constants for API endpoints.
 * Centralizes endpoint definitions to avoid duplication.
 */
public final class ApiEndpoints {
    
    // Course endpoints
    public static final String API_COURSES = "/api/courses/**";
    public static final String API_COURSES_SESSIONS = "/api/courses/sessions/**";
    public static final String API_COURSES_CHAPTERS = "/api/courses/*/chapters/**";
    
    // Prevent instantiation
    private ApiEndpoints() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
