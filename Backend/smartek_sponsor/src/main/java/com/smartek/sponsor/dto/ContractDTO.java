package com.smartek.sponsor.dto;

import com.smartek.sponsor.entity.ContractStatus;
import com.smartek.sponsor.entity.ContractType;
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
public class ContractDTO {
    private Long id;
    
    @NotBlank(message = "Contract number is required")
    @Pattern(regexp = "^CNT-\\d{6}$", message = "Contract number must follow format CNT-XXXXXX")
    private String contractNumber;
    
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    @NotNull(message = "Amount is required")
    @Min(value = 1000, message = "Minimum contract amount is 1,000€")
    @Max(value = 10000000, message = "Maximum contract amount is 10,000,000€")
    private Double amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(EUR|USD|GBP)$", message = "Currency must be EUR, USD, or GBP")
    private String currency;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    private ContractStatus status;
    private ContractType type;
    
    // Sponsor info
    private Long sponsorId;
    private String sponsorName;
    private String sponsorEmail;
    
    // Budget info
    private Double availableBudget;
    private Double spentBudget;
    private Double reservedBudget;
}
