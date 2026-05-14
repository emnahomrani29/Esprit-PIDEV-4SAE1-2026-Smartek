package com.smartek.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRevenueResponse {
    
    private Long eventId;
    private BigDecimal totalRevenue;
    private BigDecimal potentialRevenue;
    private Long paidRegistrations;
    private Long pendingPayments;
    private BigDecimal averageRevenuePerParticipant;
}