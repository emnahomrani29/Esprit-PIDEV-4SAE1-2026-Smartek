package com.smartek.offersservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewRequest {
    
    private Long applicationId;
    private LocalDateTime interviewDate;
    private String location;
    private String meetingLink;
    private String notes;
    private Long createdBy;
}
