package com.smartek.skillevidenceservice.dto;

import com.smartek.skillevidenceservice.entity.EvidenceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerAnalyticsDTO {
    
    private Integer totalCount;
    private Integer approvedCount;
    private Integer pendingCount;
    private Integer rejectedCount;
    private Double averageScore; // null if no approved evidence
    
    private Map<EvidenceCategory, Integer> categoryDistribution;
    private List<ScoreTrendPoint> scoreTrend; // Ordered by uploadDate
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreTrendPoint {
        private LocalDate date;
        private Integer score;
        private String title;
    }
}
