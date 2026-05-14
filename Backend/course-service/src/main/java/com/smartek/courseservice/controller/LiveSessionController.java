package com.smartek.courseservice.controller;

import com.smartek.courseservice.dto.LiveSessionRequest;
import com.smartek.courseservice.dto.LiveSessionResponse;
import com.smartek.courseservice.entity.SessionStatus;
import com.smartek.courseservice.service.LiveSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class LiveSessionController {
    
    private final LiveSessionService liveSessionService;
    
    /**
     * Crée une nouvelle session live
     */
    @PostMapping("/sessions")
    public ResponseEntity<LiveSessionResponse> createSession(@Valid @RequestBody LiveSessionRequest request) {
        log.info("Requête de création de session live: {}", request.getTitle());
        try {
            LiveSessionResponse response = liveSessionService.createSession(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création de la session: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(LiveSessionResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    /**
     * Récupère toutes les sessions d'un cours
     */
    @GetMapping("/course/{courseId}/sessions")
    public ResponseEntity<List<LiveSessionResponse>> getSessionsByCourseId(@PathVariable Long courseId) {
        log.info("Requête de récupération des sessions du cours: {}", courseId);
        try {
            List<LiveSessionResponse> sessions = liveSessionService.getSessionsByCourseId(courseId);
            return ResponseEntity.ok(sessions);
        } catch (RuntimeException e) {
            log.warn("Cours {} non trouvé ou sans sessions: {}", courseId, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
    
    /**
     * Récupère les sessions à venir d'un cours
     */
    @GetMapping("/course/{courseId}/sessions/upcoming")
    public ResponseEntity<List<LiveSessionResponse>> getUpcomingSessionsByCourseId(@PathVariable Long courseId) {
        log.info("Requête de récupération des sessions à venir du cours: {}", courseId);
        try {
            List<LiveSessionResponse> sessions = liveSessionService.getUpcomingSessionsByCourseId(courseId);
            return ResponseEntity.ok(sessions);
        } catch (RuntimeException e) {
            log.warn("Cours {} non trouvé ou sans sessions à venir: {}", courseId, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
    
    /**
     * Récupère les sessions en cours d'un cours
     */
    @GetMapping("/course/{courseId}/sessions/ongoing")
    public ResponseEntity<List<LiveSessionResponse>> getOngoingSessionsByCourseId(@PathVariable Long courseId) {
        log.info("Requête de récupération des sessions en cours du cours: {}", courseId);
        try {
            List<LiveSessionResponse> sessions = liveSessionService.getOngoingSessionsByCourseId(courseId);
            return ResponseEntity.ok(sessions);
        } catch (RuntimeException e) {
            log.warn("Cours {} non trouvé ou sans sessions en cours: {}", courseId, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
    
    /**
     * Récupère une session par son ID
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<LiveSessionResponse> getSessionById(@PathVariable Long sessionId) {
        log.info("Requête de récupération de la session: {}", sessionId);
        try {
            LiveSessionResponse response = liveSessionService.getSessionById(sessionId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération de la session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(LiveSessionResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    /**
     * Met à jour une session
     */
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<LiveSessionResponse> updateSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody LiveSessionRequest request) {
        log.info("Requête de mise à jour de la session: {}", sessionId);
        try {
            LiveSessionResponse response = liveSessionService.updateSession(sessionId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de la session: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(LiveSessionResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    /**
     * Change le statut d'une session
     */
    @PatchMapping("/sessions/{sessionId}/status")
    public ResponseEntity<LiveSessionResponse> updateSessionStatus(
            @PathVariable Long sessionId,
            @RequestParam SessionStatus status) {
        log.info("Requête de changement de statut de la session {} vers {}", sessionId, status);
        try {
            LiveSessionResponse response = liveSessionService.updateSessionStatus(sessionId, status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors du changement de statut: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(LiveSessionResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    /**
     * Supprime une session
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        log.info("Requête de suppression de la session: {}", sessionId);
        try {
            liveSessionService.deleteSession(sessionId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de la session: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Récupère toutes les sessions d'un trainer
     */
    @GetMapping("/trainer/{trainerId}/sessions")
    public ResponseEntity<List<LiveSessionResponse>> getSessionsByTrainerId(@PathVariable Long trainerId) {
        log.info("Requête de récupération des sessions du trainer: {}", trainerId);
        List<LiveSessionResponse> sessions = liveSessionService.getSessionsByTrainerId(trainerId);
        return ResponseEntity.ok(sessions);
    }
}
