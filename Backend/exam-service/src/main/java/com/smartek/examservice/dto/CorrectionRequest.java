package com.smartek.examservice.dto;

import lombok.Data;

@Data
public class CorrectionRequest {
    private Integer marksObtained;
    private String trainerFeedback;
}
