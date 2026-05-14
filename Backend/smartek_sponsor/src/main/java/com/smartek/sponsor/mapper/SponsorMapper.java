package com.smartek.sponsor.mapper;

import com.smartek.sponsor.dto.SponsorDTO;
import com.smartek.sponsor.entity.Sponsor;
import org.springframework.stereotype.Component;

@Component
public class SponsorMapper {
    
    public SponsorDTO toDTO(Sponsor sponsor) {
        if (sponsor == null) {
            return null;
        }
        
        return SponsorDTO.builder()
                .id(sponsor.getId())
                .name(sponsor.getName())
                .email(sponsor.getEmail())
                .phone(sponsor.getPhone())
                .companyName(sponsor.getCompanyName())
                .industry(sponsor.getIndustry())
                .website(sponsor.getWebsite())
                .logoUrl(sponsor.getLogoUrl())
                .status(sponsor.getStatus())
                .build();
    }
    
    public Sponsor toEntity(SponsorDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Sponsor sponsor = new Sponsor();
        sponsor.setId(dto.getId());
        sponsor.setName(dto.getName());
        sponsor.setEmail(dto.getEmail());
        sponsor.setPhone(dto.getPhone());
        sponsor.setCompanyName(dto.getCompanyName());
        sponsor.setIndustry(dto.getIndustry());
        sponsor.setWebsite(dto.getWebsite());
        sponsor.setLogoUrl(dto.getLogoUrl());
        sponsor.setStatus(dto.getStatus());
        sponsor.setPassword(dto.getPassword()); // For registration
        
        return sponsor;
    }
}
