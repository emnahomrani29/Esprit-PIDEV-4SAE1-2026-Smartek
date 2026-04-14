package com.smartek.offersservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long applicationId;
    
    @Column(nullable = false)
    private Long offerId;
    
    @Column(nullable = false)
    private Long learnerId;
    
    @Column(nullable = false)
    private String learnerName;
    
    @Column(nullable = false)
    private String learnerEmail;
    
    @Column(nullable = false)
    private LocalDateTime interviewDate;
    
    @Column(nullable = false)
    private String location; // Lieu de l'entretien (adresse ou "En ligne")
    
    @Column(length = 500)
    private String meetingLink; // Lien de visioconférence si en ligne
    
    @Column(length = 1000)
    private String notes; // Notes pour l'entretien
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InterviewStatus status;
    
    @Column(nullable = false)
    private Long createdBy; // ID du RH qui a créé l'entretien
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum InterviewStatus {
        SCHEDULED,    // Planifié
        COMPLETED,    // Terminé
        CANCELLED,    // Annulé
        RESCHEDULED   // Reporté
    }
}
