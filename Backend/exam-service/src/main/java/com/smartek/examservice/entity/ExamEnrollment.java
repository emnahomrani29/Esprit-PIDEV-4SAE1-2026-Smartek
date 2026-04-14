package com.smartek.examservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_enrollments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exam_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamEnrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    @Column(name = "course_id")
    private Long courseId;
    
    @Column(name = "training_id")
    private Long trainingId;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isUnlocked = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;
    
    private LocalDateTime unlockedAt;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime pausedAt;
    
    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;
    
    private LocalDateTime completedAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime enrolledAt;
    
    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
    }
}
