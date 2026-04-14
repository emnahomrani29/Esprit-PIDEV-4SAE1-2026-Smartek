package com.smartek.offersservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private String experienceLevel;
    private Boolean remote;
    private Integer positions;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private String status;

    // Date d'expiration optionnelle
    private LocalDateTime expiresAt;
}

