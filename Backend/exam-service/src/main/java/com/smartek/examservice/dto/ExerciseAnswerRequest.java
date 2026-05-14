package com.smartek.examservice.dto;

import lombok.Data;

@Data
public class ExerciseAnswerRequest {
    private Long exerciseId;
    private String answerText;
}
