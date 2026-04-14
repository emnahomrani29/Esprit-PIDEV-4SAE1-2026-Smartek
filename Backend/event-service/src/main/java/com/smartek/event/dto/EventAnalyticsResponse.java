package com.smartek.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAnalyticsResponse {
    
    private Long eventId;
    private String eventTitle;
    
    // Statistiques de capacité
    private Integer totalCapacity;
    private Integer totalRegistered;
    private Integer confirmedRegistrations;
    private Integer waitingListSize;
    
    // Taux calculés (en pourcentage)
    private Double fillRate; // Taux de remplissage
    private Double cancellationRate; // Taux d'annulation
    
    // Répartition par mode
    private Integer physicalCapacity;
    private Integer physicalRegistered;
    private Integer onlineCapacity;
    private Integer onlineRegistered;
    
    // Statistiques financières
    private BigDecimal totalRevenue;
    private BigDecimal potentialRevenue;
    private Long paidRegistrations;
    private Long pendingPayments;
    
    // Indicateurs de performance
    private String performanceIndicator; // EXCELLENT, GOOD, AVERAGE, POOR
    private String recommendation; // Recommandation automatique
}