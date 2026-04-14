package com.smartek.examservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubmissionRequest {
    private Long examId;
    private Long userId;
    private Integer timeTaken;
    private List<AnswerRequest> answers;
}
