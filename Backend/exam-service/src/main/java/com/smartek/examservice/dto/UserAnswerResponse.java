package com.smartek.examservice.dto;

import lombok.Data;

@Data
public class UserAnswerResponse {
    private Long id;
    private Long questionId;
    private String selectedAnswer;
    private Boolean isCorrect;
    private Integer marksObtained;
}
