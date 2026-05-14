package com.smartek.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamStatsResponse {
    private Long userId;
    private Integer totalAvailable;
    private Integer attempted;
    private Integer passed;
    private Integer failed;
    private Double averageScore;
    private Double successRate;
    private Integer totalAttempts;
}
