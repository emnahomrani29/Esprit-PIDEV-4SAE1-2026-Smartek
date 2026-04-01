package com.smartek.certificationbadgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationStatisticsDTO {
    
    private Long certificationTemplateId;
    
    private String certificationTitle;
    
    private Long totalAwarded;
}
