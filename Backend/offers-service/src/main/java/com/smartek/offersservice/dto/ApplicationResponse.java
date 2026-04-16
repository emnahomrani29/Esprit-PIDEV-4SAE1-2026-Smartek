package com.smartek.offersservice.dto;

import com.smartek.offersservice.entity.Application;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {
    private Long id;
    private Long offerId;
    private String offerTitle;
    private Long learnerId;
    private String learnerName;
    private String learnerEmail;
    private String coverLetter;
    private String cvBase64;
    private String cvFileName;
    private Application.ApplicationStatus status;
    private int score;
    private String recruiterNote;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
