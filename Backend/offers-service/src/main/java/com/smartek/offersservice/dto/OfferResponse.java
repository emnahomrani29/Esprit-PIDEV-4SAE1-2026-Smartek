package com.smartek.offersservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private String experienceLevel;
    private Boolean remote;
    private Integer positions;
    private Long viewCount;
    private Long companyId;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Nombre de candidatures (enrichi à la demande)
    private Long applicationCount;
    private Long savedCount;
}

