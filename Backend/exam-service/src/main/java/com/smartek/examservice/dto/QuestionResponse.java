package com.smartek.examservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionResponse {
    private Long id;
    private String questionText;
    private String questionType;
    private Integer marks;
    private List<OptionResponse> options;
    private String correctAnswer;
}
