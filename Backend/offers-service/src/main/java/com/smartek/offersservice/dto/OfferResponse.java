package com.smartek.offersservice.dto;

import com.smartek.offersservice.entity.Offer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferResponse {
    private Long id;
    private String title;
    private String description;
    private String companyName;
    private String location;
    private String contractType;
    private String salary;
    private Integer salaryMin;
    private Integer salaryMax;
    private String domain;
    private Offer.ExperienceLevel experienceLevel;
    private Boolean remote;
    private Integer positions;
    private Set<String> requiredSkills;
    private Long viewCount;
    private Long companyId;
    private Offer.OfferStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean open;
    private Long applicationCount;
    private Long savedCount;
}

