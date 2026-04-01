package com.smartek.certificationbadgeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeStatisticsDTO {
    
    private Long badgeTemplateId;
    
    private String badgeName;
    
    private Long totalAwarded;
}
