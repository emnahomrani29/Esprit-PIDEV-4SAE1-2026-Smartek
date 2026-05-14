package com.smartek.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearnerExamResponse {
    private Long id;
    private Long courseId;
    private String courseName;
    private Long trainingId;
    private String trainingName;
    private String examType; // QUIZ ou EXAM
    private String title;
    private String description;
    private Integer duration;
    private Integer passingScore;
    private Integer totalMarks;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isLocked; // true si le learner n'a pas terminé tous les cours
    private Boolean hasAttempted; // true si le learner a déjà passé cet examen
    private Integer bestScore; // Meilleur score obtenu
    private Integer attemptsCount; // Nombre de tentatives
    private Long remainingTimeMinutes; // Temps restant en minutes avant la fin de l'examen
}
