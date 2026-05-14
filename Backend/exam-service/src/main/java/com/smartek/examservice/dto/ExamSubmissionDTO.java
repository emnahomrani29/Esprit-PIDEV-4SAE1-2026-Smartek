package com.smartek.examservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubmissionDTO {
    private Long examId;
    private Long userId;
    private Integer timeTaken; // in minutes
    private List<AnswerDTO> answers;
    
    @Data
    public static class AnswerDTO {
        private Long questionId;
        private String selectedAnswer;
        private List<Integer> selectedOptions; // indices des options sélectionnées
    }
}
