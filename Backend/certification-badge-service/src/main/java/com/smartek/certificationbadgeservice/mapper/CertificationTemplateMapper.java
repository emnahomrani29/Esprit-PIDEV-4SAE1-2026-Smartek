package com.smartek.certificationbadgeservice.mapper;

import com.smartek.certificationbadgeservice.dto.CertificationTemplateDTO;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import org.springframework.stereotype.Component;

@Component
public class CertificationTemplateMapper {
    
    public CertificationTemplateDTO toDTO(CertificationTemplate entity) {
        if (entity == null) {
            return null;
        }
        
        CertificationTemplateDTO dto = new CertificationTemplateDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setExamId(entity.getExamId());
        
        return dto;
    }
    
    public CertificationTemplate toEntity(CertificationTemplateDTO dto) {
        if (dto == null) {
            return null;
        }
        
        CertificationTemplate entity = new CertificationTemplate();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setExamId(dto.getExamId());
        
        return entity;
    }
    
    public void updateEntityFromDTO(CertificationTemplateDTO dto, CertificationTemplate entity) {
        if (dto == null || entity == null) {
            return;
        }
        
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setExamId(dto.getExamId());
    }
}
