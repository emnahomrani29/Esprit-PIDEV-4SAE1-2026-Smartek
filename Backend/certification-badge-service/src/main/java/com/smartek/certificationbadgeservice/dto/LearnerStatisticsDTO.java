package com.smartek.certificationbadgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearnerStatisticsDTO {
    
    private Long learnerId;
    
    private Long totalBadges;
    
    private Long activeCertifications;
    
    private Long expiredCertifications;
}
