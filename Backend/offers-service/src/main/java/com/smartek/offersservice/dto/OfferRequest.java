package com.smartek.offersservice.dto;

import com.smartek.offersservice.entity.Offer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Contract type is required")
    private String contractType;

    private String salary;
    private Integer salaryMin;
    private Integer salaryMax;
    private String domain;
    private Offer.ExperienceLevel experienceLevel;
    private Boolean remote;
    private Integer positions;
    private Set<String> requiredSkills;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private Offer.OfferStatus status;
    private LocalDateTime expiresAt;
}
