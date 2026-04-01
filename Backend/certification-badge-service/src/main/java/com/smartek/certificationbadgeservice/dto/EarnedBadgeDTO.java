package com.smartek.certificationbadgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EarnedBadgeDTO {
    
    private Long id;
    
    private BadgeTemplateDTO badgeTemplate;
    
    private Long learnerId;
    
    private LocalDate awardDate;
    
    private Long awardedBy;
}
