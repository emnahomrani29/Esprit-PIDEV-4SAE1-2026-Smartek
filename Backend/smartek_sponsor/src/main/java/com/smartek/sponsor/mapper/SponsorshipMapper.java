package com.smartek.sponsor.mapper;

import com.smartek.sponsor.dto.CreateSponsorshipRequest;
import com.smartek.sponsor.dto.SponsorshipResponseDTO;
import com.smartek.sponsor.entity.Sponsorship;
import org.springframework.stereotype.Component;

@Component
public class SponsorshipMapper {
    
    public SponsorshipResponseDTO toResponseDTO(Sponsorship sponsorship) {
        if (sponsorship == null) {
            return null;
        }
        
        return SponsorshipResponseDTO.builder()
                .id(sponsorship.getId())
                .sponsorshipType(sponsorship.getSponsorshipType())
                .amountAllocated(sponsorship.getAmountAllocated())
                .startDate(sponsorship.getStartDate())
                .endDate(sponsorship.getEndDate())
                .visibilityLevel(sponsorship.getVisibilityLevel())
                .targetType(sponsorship.getTargetType())
                .targetId(sponsorship.getTargetId())
                .status(sponsorship.getStatus())
                .rejectionReason(sponsorship.getRejectionReason())
                .approvedBy(sponsorship.getApprovedBy())
                .approvedAt(sponsorship.getApprovedAt())
                .createdAt(sponsorship.getCreatedAt())
                .updatedAt(sponsorship.getUpdatedAt())
                .contractId(sponsorship.getContract() != null ? sponsorship.getContract().getId() : null)
                .contractNumber(sponsorship.getContract() != null ? sponsorship.getContract().getContractNumber() : null)
                .sponsorId(sponsorship.getContract() != null && sponsorship.getContract().getSponsor() != null 
                        ? sponsorship.getContract().getSponsor().getId() : null)
                .sponsorName(sponsorship.getContract() != null && sponsorship.getContract().getSponsor() != null 
                        ? sponsorship.getContract().getSponsor().getName() : null)
                .sponsorEmail(sponsorship.getContract() != null && sponsorship.getContract().getSponsor() != null 
                        ? sponsorship.getContract().getSponsor().getEmail() : null)
                .build();
    }
    
    public Sponsorship toEntity(CreateSponsorshipRequest request) {
        if (request == null) {
            return null;
        }
        
        Sponsorship sponsorship = new Sponsorship();
        sponsorship.setSponsorshipType(request.getSponsorshipType());
        sponsorship.setAmountAllocated(request.getAmountAllocated());
        sponsorship.setStartDate(request.getStartDate());
        sponsorship.setEndDate(request.getEndDate());
        sponsorship.setVisibilityLevel(request.getVisibilityLevel());
        sponsorship.setTargetType(request.getTargetType());
        sponsorship.setTargetId(request.getTargetId());
        
        return sponsorship;
    }
}
