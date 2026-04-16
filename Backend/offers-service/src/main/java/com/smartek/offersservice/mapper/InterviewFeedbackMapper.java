package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.InterviewFeedbackResponse;
import com.smartek.offersservice.entity.InterviewFeedback;
import org.springframework.stereotype.Component;

/**
 * Mapper pour la conversion entre l'entité InterviewFeedback et ses DTOs.
 */
@Component
public class InterviewFeedbackMapper {

    public InterviewFeedbackResponse toResponse(InterviewFeedback feedback) {
        Long interviewId = feedback.getInterview() != null ? feedback.getInterview().getId() : null;

        return InterviewFeedbackResponse.builder()
                .id(feedback.getId())
                .interviewId(interviewId)
                .applicationId(feedback.getApplicationId())
                .rating(feedback.getRating())
                .strengths(feedback.getStrengths())
                .weaknesses(feedback.getWeaknesses())
                .generalComment(feedback.getGeneralComment())
                .decision(feedback.getDecision())
                .submittedBy(feedback.getSubmittedBy())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
