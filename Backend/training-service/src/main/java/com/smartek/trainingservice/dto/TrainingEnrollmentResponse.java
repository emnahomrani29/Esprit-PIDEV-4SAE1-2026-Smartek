package com.smartek.trainingservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrainingEnrollmentResponse {
    private Long id;
    private Long trainingId;
    private String trainingTitle;
    private Long userId;
    private LocalDateTime enrolledAt;
    private Boolean isActive;
    private Integer progress;
    private LocalDateTime completedAt;
    private String status;
}
