package com.smartek.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerExamAnalyticsResponse {
    private Long examId;
    private String examTitle;
    private Long learnerId;
    private String learnerName;
    private String learnerEmail;
    private Integer score;
    private Integer maxScore;
    private Double percentage;
    private String status; // "passed" or "failed"
    private LocalDateTime completedAt;
}
