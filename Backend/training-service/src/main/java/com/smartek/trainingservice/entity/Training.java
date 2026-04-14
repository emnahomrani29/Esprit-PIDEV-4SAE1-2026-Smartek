package com.smartek.trainingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Training {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainingId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private String level; // Débutant, Intermédiaire, Avancé
    
    @Column(nullable = false)
    private LocalDate duration;
    
    // Liste des IDs des cours associés (relation unidirectionnelle)
    @ElementCollection
    @CollectionTable(name = "training_courses", joinColumns = @JoinColumn(name = "training_id"))
    @Column(name = "course_id")
    @Builder.Default
    private List<Long> courseIds = new ArrayList<>();
    
    // ID de l'examen final (optionnel)
    @Column(name = "exam_id")
    private Long examId;
    
    // ID du trainer qui a créé la formation
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
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
}
