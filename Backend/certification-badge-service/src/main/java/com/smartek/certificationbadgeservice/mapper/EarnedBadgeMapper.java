package com.smartek.certificationbadgeservice.mapper;

import com.smartek.certificationbadgeservice.dto.EarnedBadgeDTO;
import com.smartek.certificationbadgeservice.entity.EarnedBadge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EarnedBadgeMapper {
    
    private final BadgeTemplateMapper badgeTemplateMapper;
    
    public EarnedBadgeDTO toDTO(EarnedBadge entity) {
        if (entity == null) {
            return null;
        }
        
        EarnedBadgeDTO dto = new EarnedBadgeDTO();
        dto.setId(entity.getId());
        dto.setBadgeTemplate(badgeTemplateMapper.toDTO(entity.getBadgeTemplate()));
        dto.setLearnerId(entity.getLearnerId());
        dto.setAwardDate(entity.getAwardDate());
        dto.setAwardedBy(entity.getAwardedBy());
        
        return dto;
    }
}
