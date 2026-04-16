package com.smartek.courseservice.mapper;

import com.smartek.courseservice.dto.LiveSessionRequest;
import com.smartek.courseservice.dto.LiveSessionResponse;
import com.smartek.courseservice.entity.Course;
import com.smartek.courseservice.entity.LiveSession;
import com.smartek.courseservice.entity.SessionStatus;
import org.springframework.stereotype.Component;

@Component
public class LiveSessionMapper {
    
    /**
     * Convertit une LiveSessionRequest en entité LiveSession
     */
    public LiveSession toEntity(LiveSessionRequest request, Course course) {
        if (request == null) {
            return null;
        }
        
        return LiveSession.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .trainerId(request.getTrainerId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(SessionStatus.SCHEDULED)
                .maxParticipants(request.getMaxParticipants())
                .currentParticipants(0)
                .build();
    }
    
    /**
     * Convertit une entité LiveSession en LiveSessionResponse
     */
    public LiveSessionResponse toResponse(LiveSession session) {
        if (session == null) {
            return null;
        }
        
        Long courseId = null;
        String courseTitle = null;
        try {
            if (session.getCourse() != null) {
                courseId = session.getCourse().getCourseId();
                courseTitle = session.getCourse().getTitle();
            }
        } catch (Exception e) {
            // LazyInitializationException - course non chargé
        }
        
        return LiveSessionResponse.builder()
                .sessionId(session.getSessionId())
                .courseId(courseId)
                .courseTitle(courseTitle)
                .title(session.getTitle())
                .description(session.getDescription())
                .trainerId(session.getTrainerId())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .roomId(session.getRoomId())
                .status(session.getStatus())
                .maxParticipants(session.getMaxParticipants())
                .currentParticipants(session.getCurrentParticipants())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
    
    /**
     * Convertit une entité LiveSession en LiveSessionResponse avec message
     */
    public LiveSessionResponse toResponse(LiveSession session, String message) {
        LiveSessionResponse response = toResponse(session);
        if (response != null) {
            response.setMessage(message);
        }
        return response;
    }
    
    /**
     * Met à jour une entité LiveSession à partir d'une LiveSessionRequest
     */
    public void updateEntityFromRequest(LiveSession session, LiveSessionRequest request) {
        if (session == null || request == null) {
            return;
        }
        
        session.setTitle(request.getTitle());
        session.setDescription(request.getDescription());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setMaxParticipants(request.getMaxParticipants());
        // Le roomId ne change jamais une fois créé
    }
}
