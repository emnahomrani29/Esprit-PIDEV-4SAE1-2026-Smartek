package com.smartek.offersservice.mapper;

import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.entity.Offer;
import org.springframework.stereotype.Component;

/**
 * Mapper pour la conversion entre l'entité Offer et ses DTOs.
 * Design Pattern : Mapper / Converter
 */
@Component
public class OfferMapper {

    /** Convertit un OfferRequest en entité Offer */
    public Offer toEntity(OfferRequest request) {
        return Offer.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .companyName(request.getCompanyName())
                .location(request.getLocation())
                .contractType(request.getContractType())
                .salary(request.getSalary())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .domain(request.getDomain())
                .experienceLevel(request.getExperienceLevel() != null ? request.getExperienceLevel().name() : null)
                .remote(request.getRemote() != null ? request.getRemote() : false)
                .positions(request.getPositions() != null ? request.getPositions() : 1)
                .requiredSkills(request.getRequiredSkills() != null ? request.getRequiredSkills() : new java.util.HashSet<>())
                .companyId(request.getCompanyId())
                .status(request.getStatus() != null ? request.getStatus() : Offer.OfferStatus.ACTIVE)
                .expiresAt(request.getExpiresAt())
                .build();
    }

    /** Convertit une entité Offer en OfferResponse */
    public OfferResponse toResponse(Offer offer) {
        return OfferResponse.builder()
                .id(offer.getId())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .companyName(offer.getCompanyName())
                .location(offer.getLocation())
                .contractType(offer.getContractType())
                .salary(offer.getSalary())
                .salaryMin(offer.getSalaryMin())
                .salaryMax(offer.getSalaryMax())
                .domain(offer.getDomain())
                .experienceLevel(offer.getExperienceLevel() != null ? Offer.ExperienceLevel.valueOf(offer.getExperienceLevel()) : null)
                .remote(offer.getRemote())
                .positions(offer.getPositions())
                .requiredSkills(offer.getRequiredSkills())
                .viewCount(offer.getViewCount())
                .companyId(offer.getCompanyId())
                .status(offer.getStatus())
                .expiresAt(offer.getExpiresAt())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .open(offer.isOpen())
                .build();
    }

    /** Met à jour une entité Offer existante depuis un OfferRequest */
    public void updateEntityFromRequest(Offer offer, OfferRequest request) {
        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setCompanyName(request.getCompanyName());
        offer.setLocation(request.getLocation());
        offer.setContractType(request.getContractType());
        offer.setSalary(request.getSalary());
        offer.setSalaryMin(request.getSalaryMin());
        offer.setSalaryMax(request.getSalaryMax());
        offer.setDomain(request.getDomain());
        offer.setExperienceLevel(request.getExperienceLevel() != null ? request.getExperienceLevel().name() : null);
        if (request.getRemote() != null) offer.setRemote(request.getRemote());
        if (request.getPositions() != null) offer.setPositions(request.getPositions());
        if (request.getRequiredSkills() != null) offer.setRequiredSkills(request.getRequiredSkills());
        if (request.getStatus() != null) offer.setStatus(request.getStatus());
        if (request.getExpiresAt() != null) offer.setExpiresAt(request.getExpiresAt());
    }
}
