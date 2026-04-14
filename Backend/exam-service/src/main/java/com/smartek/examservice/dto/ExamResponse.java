package com.smartek.examservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamResponse {
    private Long id;
    private Long courseId;
    private Long trainingId;
    private String examType; // QUIZ ou EXAM
    private String title;
    private String description;
    private Integer duration;
    private Integer passingScore;
    private Integer totalMarks;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Integer questionCount;
    private Integer exerciseCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Champs pour l'enrollment
    private Boolean isUnlocked;
    private Boolean isCompleted;
    
    // Questions détaillées (optionnel)
    private List<QuestionResponse> questions;
}
