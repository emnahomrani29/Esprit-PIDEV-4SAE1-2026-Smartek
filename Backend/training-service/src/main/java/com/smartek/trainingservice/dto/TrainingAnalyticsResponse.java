package com.smartek.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingAnalyticsResponse {
    private Long trainingId;
    private String trainingName;
    private String trainingTitle;
    private Integer totalLearners;
    private Integer completedLearners;
    private Integer activeLearners;
    private Double averageProgress;
    private Double averageScore;
    private Integer totalEnrollments;
    private Integer activeEnrollments;
    private Integer completedEnrollments;
}
