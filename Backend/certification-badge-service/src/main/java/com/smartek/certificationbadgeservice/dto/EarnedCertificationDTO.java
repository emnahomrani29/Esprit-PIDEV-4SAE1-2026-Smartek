package com.smartek.certificationbadgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EarnedCertificationDTO {
    
    private Long id;
    
    private CertificationTemplateDTO certificationTemplate;
    
    private Long learnerId;
    
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
    
    private String certificateUrl;
    
    private Long awardedBy;
    
    private Boolean isExpired;
    
    private String examId;
}
