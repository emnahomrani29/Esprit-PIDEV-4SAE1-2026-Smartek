package com.smartek.trainingservice.dto;

import lombok.Data;

@Data
public class TrainingEnrollmentRequest {
    private Long trainingId;
    private Long userId;
}
