package com.smartek.examservice.dto;

import lombok.Data;

@Data
public class ExerciseRequest {
    private Long examId;
    private Integer exerciseNumber;
    private String content;
    private Integer marks;
    private String instructions;
}
