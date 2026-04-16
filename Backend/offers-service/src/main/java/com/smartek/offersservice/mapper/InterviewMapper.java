package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.InterviewResponse;
import com.smartek.offersservice.entity.Interview;
import org.springframework.stereotype.Component;

@Component
public class InterviewMapper {

    public InterviewResponse toResponse(Interview interview) {
        return InterviewResponse.builder()
                .id(interview.getId())
                .applicationId(interview.getApplicationId())
                .offerId(interview.getOfferId())
                .learnerId(interview.getLearnerId())
                .learnerName(interview.getLearnerName())
                .learnerEmail(interview.getLearnerEmail())
                .interviewDate(interview.getInterviewDate())
                .location(interview.getLocation())
                .meetingLink(interview.getMeetingLink())
                .notes(interview.getNotes())
                .status(interview.getStatus() != null ? interview.getStatus().name() : null)
                .createdBy(interview.getCreatedBy())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }
}
