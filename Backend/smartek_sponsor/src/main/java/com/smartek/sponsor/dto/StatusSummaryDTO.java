package com.smartek.sponsor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusSummaryDTO {
    private Long sponsorId;
    private Long pendingCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Long completedCount;
    private Long cancelledCount;
}
