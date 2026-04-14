package com.smartek.examservice.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ExamDraftDTO {
    private Long examId;
    private Long userId;
    private Map<Long, String> answers; // questionId -> answer
}
