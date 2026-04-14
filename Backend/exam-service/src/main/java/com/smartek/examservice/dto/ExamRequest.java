package com.smartek.examservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamRequest {
    private Long courseId;
    private Long trainingId;
    private String examType; // QUIZ ou EXAM
    private String title;
    private String description;
    private Integer duration;
    private Integer passingScore;
    private Integer totalMarks; // Optionnel - sera calculé automatiquement si non fourni
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Long createdBy; // ID du trainer qui crée l'examen
    private List<QuestionRequest> questions; // Questions à créer avec l'examen
}
