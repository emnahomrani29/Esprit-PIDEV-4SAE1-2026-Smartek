package com.smartek.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerTrainingAnalyticsResponse {
    private Long trainingId;
    private String trainingTitle;
    private Integer totalEnrollments;
    private Integer activeEnrollments;
    private Integer completedEnrollments;
}
