package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.InterviewResponse;
import com.smartek.offersservice.entity.Interview;
import org.springframework.stereotype.Component;

/**
 * Mapper pour la conversion entre l'entité Interview et ses DTOs.
 */
@Component
public class InterviewMapper {

    public InterviewResponse toResponse(Interview interview) {
        Long applicationId = interview.getApplication() != null ? interview.getApplication().getId() : null;

        return InterviewResponse.builder()
                .id(interview.getId())
                .applicationId(applicationId)
                .offerId(interview.getOfferId())
                .learnerId(interview.getLearnerId())
                .learnerName(interview.getLearnerName())
                .learnerEmail(interview.getLearnerEmail())
                .interviewDate(interview.getInterviewDate())
                .location(interview.getLocation())
                .meetingLink(interview.getMeetingLink())
                .notes(interview.getNotes())
                .status(interview.getStatus())
                .createdBy(interview.getCreatedBy())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .hasFeedback(interview.getFeedback() != null)
                .build();
    }
}
