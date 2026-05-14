package com.smartek.sponsor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetSummaryDTO {
    private Long contractId;
    private String contractNumber;
    private Double totalBudget;
    private Double spent;
    private Double reserved;
    private Double available;
    private Double usagePercentage;
    private String warningLevel; // NORMAL, WARNING, CRITICAL
}
