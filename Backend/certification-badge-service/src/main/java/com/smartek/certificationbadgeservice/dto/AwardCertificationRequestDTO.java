package com.smartek.certificationbadgeservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AwardCertificationRequestDTO {
    
    @NotNull(message = "Certification template ID is required")
    private Long certificationTemplateId;
    
    @NotNull(message = "Learner ID is required")
    private Long learnerId;
    
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
    
    @Size(max = 500, message = "Certificate URL must not exceed 500 characters")
    private String certificateUrl;
    
    private Long awardedBy;
}
