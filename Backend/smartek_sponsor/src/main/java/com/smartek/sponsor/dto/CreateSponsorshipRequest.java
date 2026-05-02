package com.smartek.sponsor.dto;

import com.smartek.sponsor.entity.SponsorshipType;
import com.smartek.sponsor.entity.TargetType;
import com.smartek.sponsor.entity.VisibilityLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSponsorshipRequest {
    
    @NotNull(message = "Contract ID is required")
    private Long contractId;
    
    @NotNull(message = "Sponsorship type is required")
    private SponsorshipType sponsorshipType;
    
    @NotNull(message = "Amount is required")
    @Min(value = 500, message = "Minimum amount is 500€")
    @Max(value = 1000000, message = "Maximum amount is 1,000,000€")
    private Double amountAllocated;
    
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    @NotNull(message = "Visibility level is required")
    private VisibilityLevel visibilityLevel;
    
    @NotNull(message = "Target type is required")
    private TargetType targetType;
    
    @NotNull(message = "Target ID is required")
    @Min(value = 1, message = "Target ID must be positive")
    private Long targetId;
}
