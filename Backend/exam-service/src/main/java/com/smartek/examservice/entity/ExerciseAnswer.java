package com.smartek.examservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercise_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id", nullable = false)
    private ExamResult examResult;
    
    @Column(columnDefinition = "TEXT")
    private String answerText; // Réponse du learner
    
    @Column
    private Integer marksObtained; // Points obtenus (null si pas encore corrigé)
    
    @Column(columnDefinition = "TEXT")
    private String trainerFeedback; // Commentaire du trainer
    
    @Column
    @Builder.Default
    private Boolean isCorrected = false; // Si l'exercice a été corrigé
}
