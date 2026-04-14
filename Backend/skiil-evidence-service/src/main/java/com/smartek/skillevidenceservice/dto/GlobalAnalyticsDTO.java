package com.smartek.skillevidenceservice.dto;

import com.smartek.skillevidenceservice.entity.EvidenceCategory;
import com.smartek.skillevidenceservice.entity.EvidenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalAnalyticsDTO {
    
    private Integer totalCount;
    private Integer approvedCount;
    private Integer pendingCount;
    private Integer rejectedCount;
    private Double approvalRate; // approvedCount / (approvedCount + rejectedCount)
    private Double averageScore; // Average across all approved evidence
    
    private Map<EvidenceCategory, Integer> categoryDistribution;
    private Map<LocalDate, Integer> submissionTrend; // Grouped by uploadDate
    private Map<EvidenceStatus, Integer> statusDistribution;
}
