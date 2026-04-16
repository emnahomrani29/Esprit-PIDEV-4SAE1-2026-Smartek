package com.smartek.courseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SignalingWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // sessionId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // roomId -> Set of sessionIds
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    // sessionId -> participant info
    private final Map<String, Map<String, Object>> participantInfo = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket connected: {}", session.getId());

        // Send session ID to client
        sendTo(session, Map.of("type", "session-id", "sessionId", session.getId()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");
        String roomId = (String) payload.get("roomId");

        switch (type) {
            case "join" -> handleJoin(session, payload, roomId);
            case "offer" -> handleRelay(session, payload, roomId, "offer");
            case "answer" -> handleRelay(session, payload, roomId, "answer");
            case "ice-candidate" -> handleRelay(session, payload, roomId, "ice-candidate");
            case "chat" -> handleChat(session, payload, roomId);
            case "media-state" -> handleMediaState(session, payload, roomId);
            case "leave" -> handleLeave(session, roomId);
            default -> log.warn("Unknown message type: {}", type);
        }
    }

    private void handleJoin(WebSocketSession session, Map<String, Object> payload, String roomId) throws IOException {
        String sessionId = session.getId();
        String userName = (String) payload.getOrDefault("userName", "Anonyme");
        String userId = (String) payload.getOrDefault("userId", sessionId);

        log.info("User {} ({}) joining room {}", userName, sessionId, roomId);

        // Get existing participants BEFORE adding new one
        Set<String> existing = rooms.getOrDefault(roomId, new HashSet<>());

        // Notify new user of all existing participants
        for (String existingId : existing) {
            Map<String, Object> info = participantInfo.get(existingId);
            if (info != null) {
                sendTo(session, Map.of(
                        "type", "existing-participant",
                        "sessionId", existingId,
                        "userName", info.getOrDefault("userName", "Anonyme"),
                        "userId", info.getOrDefault("userId", existingId)
                ));
            }
        }

        // Register participant
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        participantInfo.put(sessionId, new HashMap<>(Map.of(
                "sessionId", sessionId,
                "userId", userId,
                "userName", userName,
                "roomId", roomId
        )));

        // Notify all others in the room about the new user
        broadcastToRoom(roomId, sessionId, Map.of(
                "type", "user-joined",
                "sessionId", sessionId,
                "userName", userName,
                "userId", userId
        ));
    }

    private void handleRelay(WebSocketSession session, Map<String, Object> payload, String roomId, String type) throws IOException {
        String to = (String) payload.get("to");
        WebSocketSession target = sessions.get(to);
        if (target != null && target.isOpen()) {
            Map<String, Object> msg = new HashMap<>(payload);
            msg.put("type", type);
            msg.put("from", session.getId());
            sendTo(target, msg);
        }
    }

    private void handleChat(WebSocketSession session, Map<String, Object> payload, String roomId) throws IOException {
        Map<String, Object> info = participantInfo.getOrDefault(session.getId(), Map.of());
        String userName = (String) info.getOrDefault("userName", "Anonyme");

        broadcastToRoom(roomId, null, Map.of(
                "type", "chat",
                "from", session.getId(),
                "userName", userName,
                "message", payload.getOrDefault("message", ""),
                "timestamp", System.currentTimeMillis()
        ));
    }

    private void handleMediaState(WebSocketSession session, Map<String, Object> payload, String roomId) throws IOException {
        broadcastToRoom(roomId, session.getId(), Map.of(
                "type", "media-state",
                "from", session.getId(),
                "isMuted", payload.getOrDefault("isMuted", false),
                "isVideoOff", payload.getOrDefault("isVideoOff", false)
        ));
    }

    private void handleLeave(WebSocketSession session, String roomId) throws IOException {
        removeFromRoom(session.getId(), roomId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);

        Map<String, Object> info = participantInfo.remove(sessionId);
        if (info != null) {
            String roomId = (String) info.get("roomId");
            String userName = (String) info.getOrDefault("userName", "Anonyme");
            if (roomId != null) {
                Set<String> room = rooms.get(roomId);
                if (room != null) {
                    room.remove(sessionId);
                    if (room.isEmpty()) rooms.remove(roomId);
                }
                broadcastToRoom(roomId, null, Map.of(
                        "type", "user-left",
                        "sessionId", sessionId,
                        "userName", userName
                ));
            }
        }
        log.info("WebSocket disconnected: {}", sessionId);
    }

    private void removeFromRoom(String sessionId, String roomId) throws IOException {
        Set<String> room = rooms.get(roomId);
        if (room != null) {
            room.remove(sessionId);
            if (room.isEmpty()) rooms.remove(roomId);
        }
        Map<String, Object> info = participantInfo.remove(sessionId);
        String userName = info != null ? (String) info.getOrDefault("userName", "Anonyme") : "Anonyme";

        broadcastToRoom(roomId, null, Map.of(
                "type", "user-left",
                "sessionId", sessionId,
                "userName", userName
        ));
    }

    private void broadcastToRoom(String roomId, String excludeSessionId, Map<String, Object> message) throws IOException {
        Set<String> room = rooms.get(roomId);
        if (room == null) return;

        for (String sid : room) {
            if (excludeSessionId != null && sid.equals(excludeSessionId)) continue;
            WebSocketSession s = sessions.get(sid);
            if (s != null && s.isOpen()) {
                sendTo(s, message);
            }
        }
    }

    private synchronized void sendTo(WebSocketSession session, Map<String, Object> message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        }
    }
}
