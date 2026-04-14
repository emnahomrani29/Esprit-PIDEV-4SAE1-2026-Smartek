package com.smartek.examservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long courseId;
    
    @Column(name = "training_id", nullable = true)
    private Long trainingId;
    
    @Column(nullable = false, length = 20)
    private String examType = "QUIZ"; // QUIZ ou EXAM

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer duration; // in minutes

    @Column(nullable = false)
    private Integer passingScore;

    @Column(nullable = false)
    private Integer totalMarks;

    private LocalDateTime startDate;
    
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy; // ID du trainer qui a créé l'examen

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exercise> exercises;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    private List<ExamResult> results;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
