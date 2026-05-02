package com.smartek.courseservice.service;

import com.smartek.courseservice.dto.LiveSessionRequest;
import com.smartek.courseservice.dto.LiveSessionResponse;
import com.smartek.courseservice.entity.Course;
import com.smartek.courseservice.entity.DeliveryMode;
import com.smartek.courseservice.entity.LiveSession;
import com.smartek.courseservice.entity.SessionStatus;
import com.smartek.courseservice.exception.BadRequestException;
import com.smartek.courseservice.exception.ResourceNotFoundException;
import com.smartek.courseservice.mapper.LiveSessionMapper;
import com.smartek.courseservice.repository.CourseRepository;
import com.smartek.courseservice.repository.LiveSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveSessionService {
    
    private final LiveSessionRepository liveSessionRepository;
    private final CourseRepository courseRepository;
    private final LiveSessionMapper liveSessionMapper;
    
    /**
     * Génère un ID unique pour la salle de visioconférence
     */
    private String generateRoomId() {
        return "room-" + UUID.randomUUID().toString();
    }
    
    /**
     * Crée une nouvelle session live
     */
    @Transactional
    public LiveSessionResponse createSession(LiveSessionRequest request) {
        log.info("Création d'une nouvelle session live: {}", request.getTitle());
        
        // Vérifier que le cours existe
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours", "id", request.getCourseId()));
        
        // Vérifier que le cours est en mode EN_LIGNE
        if (course.getDeliveryMode() != DeliveryMode.EN_LIGNE) {
            throw new BadRequestException("Les sessions live ne sont disponibles que pour les cours en ligne");
        }
        
        // Valider les dates
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("L'heure de fin doit être après l'heure de début");
        }
        
        // Note: We allow creating sessions with past dates for flexibility
        // The frontend should handle date validation if needed
        
        LiveSession session = liveSessionMapper.toEntity(request, course);
        
        // Générer un roomId unique
        session.setRoomId(generateRoomId());
        session.setCurrentParticipants(0);
        
        LiveSession savedSession = liveSessionRepository.save(session);
        
        log.info("Session live créée avec succès: ID {}, RoomID {}", savedSession.getSessionId(), savedSession.getRoomId());
        return liveSessionMapper.toResponse(savedSession, "Session créée avec succès");
    }
    
    /**
     * Récupère toutes les sessions d'un cours
     */
    @Transactional(readOnly = true)
    public List<LiveSessionResponse> getSessionsByCourseId(Long courseId) {
        log.info("Récupération des sessions du cours: {}", courseId);
        return liveSessionRepository.findByCourseId(courseId).stream()
                .map(liveSessionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les sessions à venir d'un cours
     */
    @Transactional(readOnly = true)
    public List<LiveSessionResponse> getUpcomingSessionsByCourseId(Long courseId) {
        log.info("Récupération des sessions à venir du cours: {}", courseId);
        return liveSessionRepository.findUpcomingSessionsByCourseId(courseId, LocalDateTime.now()).stream()
                .map(liveSessionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les sessions en cours d'un cours
     */
    @Transactional(readOnly = true)
    public List<LiveSessionResponse> getOngoingSessionsByCourseId(Long courseId) {
        log.info("Récupération des sessions en cours du cours: {}", courseId);
        return liveSessionRepository.findOngoingSessionsByCourseId(courseId).stream()
                .map(liveSessionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère une session par son ID
     */
    public LiveSessionResponse getSessionById(Long sessionId) {
        log.info("Récupération de la session: {}", sessionId);
        
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
        
        return liveSessionMapper.toResponse(session);
    }
    
    /**
     * Met à jour une session
     */
    @Transactional
    public LiveSessionResponse updateSession(Long sessionId, LiveSessionRequest request) {
        log.info("Mise à jour de la session: {}", sessionId);
        
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
        
        // Valider les dates
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("L'heure de fin doit être après l'heure de début");
        }
        
        liveSessionMapper.updateEntityFromRequest(session, request);
        LiveSession updatedSession = liveSessionRepository.save(session);
        
        log.info("Session mise à jour avec succès: ID {}", updatedSession.getSessionId());
        return liveSessionMapper.toResponse(updatedSession, "Session mise à jour avec succès");
    }
    
    /**
     * Change le statut d'une session
     */
    @Transactional
    public LiveSessionResponse updateSessionStatus(Long sessionId, SessionStatus status) {
        log.info("Changement du statut de la session {} vers {}", sessionId, status);
        
        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
        
        session.setStatus(status);
        LiveSession updatedSession = liveSessionRepository.save(session);
        
        log.info("Statut de la session mis à jour avec succès");
        return liveSessionMapper.toResponse(updatedSession, "Statut mis à jour avec succès");
    }
    
    /**
     * Supprime une session
     */
    @Transactional
    public void deleteSession(Long sessionId) {
        log.info("Suppression de la session: {}", sessionId);
        
        if (!liveSessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session", "id", sessionId);
        }
        
        liveSessionRepository.deleteById(sessionId);
        log.info("Session supprimée avec succès: ID {}", sessionId);
    }
    
    /**
     * Récupère toutes les sessions d'un trainer
     */
    @Transactional(readOnly = true)
    public List<LiveSessionResponse> getSessionsByTrainerId(Long trainerId) {
        log.info("Récupération des sessions du trainer: {}", trainerId);
        return liveSessionRepository.findByTrainerIdOrderByStartTimeDesc(trainerId).stream()
                .map(liveSessionMapper::toResponse)
                .collect(Collectors.toList());
    }
}
