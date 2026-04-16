package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.InterviewFeedbackResponse;
import com.smartek.offersservice.entity.InterviewFeedback;
import org.springframework.stereotype.Component;

@Component
public class InterviewFeedbackMapper {

    public InterviewFeedbackResponse toResponse(InterviewFeedback feedback) {
        return InterviewFeedbackResponse.builder()
                .id(feedback.getId())
                .interviewId(feedback.getInterviewId())
                .applicationId(feedback.getApplicationId())
                .rating(feedback.getRating())
                .strengths(feedback.getStrengths())
                .weaknesses(feedback.getWeaknesses())
                .generalComment(feedback.getGeneralComment())
                .decision(feedback.getDecision() != null ? feedback.getDecision().name() : null)
                .submittedBy(feedback.getSubmittedBy())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
