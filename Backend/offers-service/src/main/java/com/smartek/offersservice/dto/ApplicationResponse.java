package com.smartek.offersservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long offerId;
    private Long learnerId;
    private String learnerName;
    private String learnerEmail;
    private String coverLetter;
    private String cvBase64;
    private String cvFileName;
    private String status;
    private LocalDateTime appliedAt;
}
