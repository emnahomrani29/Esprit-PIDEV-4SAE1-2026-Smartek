package com.smartek.offersservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferStatsResponse {
    private Long companyId;
    private long totalOffers;
    private long activeOffers;
    private long closedOffers;
    private long draftOffers;
    private long expiredOffers;
    private long totalApplications;
    private long pendingApplications;
    private long acceptedApplications;
    private long rejectedApplications;
    private long totalInterviews;
    private long scheduledInterviews;
    private long completedInterviews;
    private double acceptanceRate;
    private double averageApplicationScore;
}
