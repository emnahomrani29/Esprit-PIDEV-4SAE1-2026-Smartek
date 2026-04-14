package com.smartek.examservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionRequest {
    private Long examId;
    private String questionText;
    private String questionType;
    private Integer marks;
    private List<OptionRequest> options;
    private String correctAnswer;
}
