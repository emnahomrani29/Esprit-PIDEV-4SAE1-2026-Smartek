package com.smartek.certificationbadgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the result of processing an exam.
 * Indicates what certifications and badges were awarded.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamProcessingResultDTO {
    
    private Long learnerId;
    private Long examId;
    private Double percentage;
    private boolean passed;
    private boolean certificationAwarded;
    private Long certificationId;
    private boolean badgeAwarded;
    private Long badgeId;
    private String message;
}
