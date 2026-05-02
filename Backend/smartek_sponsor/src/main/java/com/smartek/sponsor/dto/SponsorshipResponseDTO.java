package com.smartek.sponsor.dto;

import com.smartek.sponsor.entity.SponsorshipStatus;
import com.smartek.sponsor.entity.SponsorshipType;
import com.smartek.sponsor.entity.TargetType;
import com.smartek.sponsor.entity.VisibilityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipResponseDTO {
    private Long id;
    private SponsorshipType sponsorshipType;
    private Double amountAllocated;
    private LocalDate startDate;
    private LocalDate endDate;
    private VisibilityLevel visibilityLevel;
    private TargetType targetType;
    private Long targetId;
    private SponsorshipStatus status;
    private String rejectionReason;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Contract info
    private Long contractId;
    private String contractNumber;
    
    // Sponsor info
    private Long sponsorId;
    private String sponsorName;
    private String sponsorEmail;
}
