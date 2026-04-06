package com.smartek.skillevidenceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenceAnalyticsResponse {

    private Long totalEvidences;
    private Long approvedEvidences;
    private Long pendingEvidences;
    private Long rejectedEvidences;
    private Double averageScore;
    private Double approvalRate;
    
    // Statistics by category
    private Map<String, Long> evidencesByCategory;
    private Map<String, Double> averageScoreByCategory;
    
    // For learner-specific analytics
    private Long learnerId;
    private String learnerName;
}
