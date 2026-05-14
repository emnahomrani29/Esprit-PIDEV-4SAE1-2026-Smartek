package com.smartek.examservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExamResultResponse {
    private Long id;
    private Long examId;
    private String examTitle;
    private Long userId;
    private Integer obtainedMarks;
    private Integer totalMarks;
    private Double percentage;
    private Boolean passed;
    private LocalDateTime submittedAt;
    private Integer timeTaken;
    private Boolean isCorrected;
    private Long correctedBy;
    private LocalDateTime correctedAt;
}
