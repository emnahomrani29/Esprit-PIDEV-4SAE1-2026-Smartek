package com.smartek.courseservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant une session live pour un cours en ligne
 */
@Entity
@Table(name = "live_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "room_id", unique = true, nullable = false, length = 100)
    private String roomId; // ID unique de la salle de visioconférence
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;
    
    @Column(name = "max_participants")
    private Integer maxParticipants;
    
    @Column(name = "current_participants")
    @Builder.Default
    private Integer currentParticipants = 0;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SessionStatus.SCHEDULED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
