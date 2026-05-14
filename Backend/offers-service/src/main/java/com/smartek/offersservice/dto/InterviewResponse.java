package com.smartek.offersservice.dto;

import com.smartek.offersservice.entity.Interview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewResponse {
    
    private Long id;
    private Long applicationId;
    private Long offerId;
    private Long learnerId;
    private String learnerName;
    private String learnerEmail;
    private LocalDateTime interviewDate;
    private String location;
    private String meetingLink;
    private String notes;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static InterviewResponse fromEntity(Interview interview) {
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
                .status(interview.getStatus().name())
                .createdBy(interview.getCreatedBy())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }
}
