package com.smartek.trainingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingEnrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;
    
    @Column(nullable = false)
    private Long userId;
    
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    private Integer progress = 0; // Progression en pourcentage
    
    @Builder.Default
    @Column(name = "exam_taken")
    private Boolean examTaken = false;
    
    @Column(name = "exam_score")
    private Integer examScore;
    
    @Builder.Default
    @Column(name = "exam_passed")
    private Boolean examPassed = false;
    
    @Builder.Default
    @Column(name = "exam_attempts")
    private Integer examAttempts = 0;
    
    private LocalDateTime completedAt;
    
    @Builder.Default
    @Column(length = 50)
    private String status = "ENROLLED"; // ENROLLED, IN_PROGRESS, COURSES_COMPLETED, EXAM_FAILED, COMPLETED, CANCELLED
}
