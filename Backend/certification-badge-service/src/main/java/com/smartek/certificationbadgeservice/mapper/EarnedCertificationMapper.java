package com.smartek.certificationbadgeservice.mapper;

import com.smartek.certificationbadgeservice.dto.EarnedCertificationDTO;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EarnedCertificationMapper {
    
    private final CertificationTemplateMapper certificationTemplateMapper;
    
    public EarnedCertificationDTO toDTO(EarnedCertification entity) {
        if (entity == null) {
            return null;
        }
        
        EarnedCertificationDTO dto = new EarnedCertificationDTO();
        dto.setId(entity.getId());
        dto.setCertificationTemplate(certificationTemplateMapper.toDTO(entity.getCertificationTemplate()));
        dto.setLearnerId(entity.getLearnerId());
        dto.setIssueDate(entity.getIssueDate());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setCertificateUrl(entity.getCertificateUrl());
        dto.setAwardedBy(entity.getAwardedBy());
        dto.setIsExpired(entity.isExpired());
        dto.setExamId(entity.getExamId());
        
        return dto;
    }
}
