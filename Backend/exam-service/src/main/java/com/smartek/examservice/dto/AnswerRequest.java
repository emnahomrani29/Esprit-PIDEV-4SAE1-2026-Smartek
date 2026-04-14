package com.smartek.examservice.dto;

import lombok.Data;

@Data
public class AnswerRequest {
    private Long questionId;
    private String answer;
}
