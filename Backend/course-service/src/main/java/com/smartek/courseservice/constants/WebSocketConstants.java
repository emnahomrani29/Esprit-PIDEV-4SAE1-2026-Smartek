package com.smartek.courseservice.constants;

/**
 * Constants for WebSocket messaging.
 * Centralizes WebSocket-related string literals to avoid duplication.
 */
public final class WebSocketConstants {
    
    // Message Keys
    public static final String SESSION_ID = "sessionId";
    public static final String ROOM_ID = "roomId";
    public static final String USER_NAME = "userName";
    public static final String USER_ID = "userId";
    
    // Default Values
    public static final String DEFAULT_USER_NAME = "Anonyme";
    
    // Prevent instantiation
    private WebSocketConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
