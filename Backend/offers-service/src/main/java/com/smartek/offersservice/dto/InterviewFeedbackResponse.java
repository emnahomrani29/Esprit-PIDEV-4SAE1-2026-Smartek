package com.smartek.offersservice.dto;

import com.smartek.offersservice.entity.InterviewFeedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewFeedbackResponse {

    private Long id;
    private Long interviewId;
    private Long applicationId;
    private Integer rating;
    private String strengths;
    private String weaknesses;
    private String generalComment;
    private String decision;
    private Long submittedBy;
    private LocalDateTime createdAt;

    public static InterviewFeedbackResponse fromEntity(InterviewFeedback f) {
        return InterviewFeedbackResponse.builder()
                .id(f.getId())
                .interviewId(f.getInterviewId())
                .applicationId(f.getApplicationId())
                .rating(f.getRating())
                .strengths(f.getStrengths())
                .weaknesses(f.getWeaknesses())
                .generalComment(f.getGeneralComment())
                .decision(f.getDecision().name())
                .submittedBy(f.getSubmittedBy())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
