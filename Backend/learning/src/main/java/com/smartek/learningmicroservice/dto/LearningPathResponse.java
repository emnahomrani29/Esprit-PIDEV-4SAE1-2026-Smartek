package com.smartek.learningmicroservice.dto;

import com.smartek.learningmicroservice.entity.LearningPathStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathResponse {

    private Long pathId;
    private String title;
    private String description;
    private Long learnerId;
    private String learnerName;
    private LearningPathStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progress;
}
