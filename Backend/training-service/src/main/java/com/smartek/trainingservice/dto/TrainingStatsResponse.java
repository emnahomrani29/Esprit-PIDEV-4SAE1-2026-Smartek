package com.smartek.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingStatsResponse {
    private Long userId;
    private Integer totalEnrolled;
    private Integer inProgress;
    private Integer completed;
    private Double averageProgress;
    private Map<String, Integer> statusBreakdown;
}
