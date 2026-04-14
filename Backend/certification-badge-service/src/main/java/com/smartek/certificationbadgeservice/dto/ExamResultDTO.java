package com.smartek.certificationbadgeservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receiving exam results from the exam service.
 * Used to automatically award certifications and badges based on exam performance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultDTO {
    
    @NotNull(message = "Learner ID is required")
    private Long learnerId;
    
    @NotNull(message = "Exam ID is required")
    private Long examId;
    
    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score must be non-negative")
    private Double score;
    
    @NotNull(message = "Max score is required")
    @Min(value = 1, message = "Max score must be positive")
    private Double maxScore;
    
    /**
     * Calculate the percentage score.
     * @return percentage (0-100)
     */
    public double getPercentage() {
        if (maxScore == null || maxScore == 0) {
            return 0.0;
        }
        return (score / maxScore) * 100.0;
    }
}
