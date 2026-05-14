package com.smartek.certificationbadgeservice.mapper;

import com.smartek.certificationbadgeservice.dto.BadgeTemplateDTO;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import org.springframework.stereotype.Component;

@Component
public class BadgeTemplateMapper {
    
    public BadgeTemplateDTO toDTO(BadgeTemplate entity) {
        if (entity == null) {
            return null;
        }
        
        BadgeTemplateDTO dto = new BadgeTemplateDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setExamId(entity.getExamId());
        dto.setMinimumScore(entity.getMinimumScore());
        
        return dto;
    }
    
    public BadgeTemplate toEntity(BadgeTemplateDTO dto) {
        if (dto == null) {
            return null;
        }
        
        BadgeTemplate entity = new BadgeTemplate();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setExamId(dto.getExamId());
        entity.setMinimumScore(dto.getMinimumScore());
        
        return entity;
    }
    
    public void updateEntityFromDTO(BadgeTemplateDTO dto, BadgeTemplate entity) {
        if (dto == null || entity == null) {
            return;
        }
        
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setExamId(dto.getExamId());
        entity.setMinimumScore(dto.getMinimumScore());
    }
}
