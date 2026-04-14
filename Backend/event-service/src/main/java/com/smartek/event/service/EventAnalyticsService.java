package com.smartek.event.service;

import com.smartek.event.dto.EventAnalyticsResponse;
import com.smartek.event.dto.EventRevenueResponse;
import com.smartek.event.model.Event;
import com.smartek.event.model.EventMode;
import com.smartek.event.repository.EventRegistrationRepository;
import com.smartek.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class EventAnalyticsService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    /**
     * Calcule les analytics complètes d'un événement
     */
    public EventAnalyticsResponse getAnalytics(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        EventAnalyticsResponse analytics = new EventAnalyticsResponse();
        analytics.setEventId(eventId);
        analytics.setEventTitle(event.getTitle());

        // Statistiques de base
        analytics.setTotalCapacity(event.getTotalCapacity());
        analytics.setTotalRegistered(event.getTotalRegistered());
        
        long confirmedCount = registrationRepository.countConfirmedByEventIdAndMode(eventId, EventMode.PHYSICAL) +
                             registrationRepository.countConfirmedByEventIdAndMode(eventId, EventMode.ONLINE);
        analytics.setConfirmedRegistrations((int) confirmedCount);
        
        int waitingListSize = registrationRepository.findWaitingListByEventId(eventId).size();
        analytics.setWaitingListSize(waitingListSize);

        // Calcul des taux
        double fillRate = event.getTotalCapacity() > 0 ? 
                (double) analytics.getConfirmedRegistrations() / event.getTotalCapacity() * 100 : 0;
        analytics.setFillRate(Math.round(fillRate * 100.0) / 100.0);

        long totalRegistrations = analytics.getConfirmedRegistrations() + waitingListSize;
        long cancelledCount = registrationRepository.countCancelledByEventId(eventId);
        double cancellationRate = totalRegistrations > 0 ? 
                (double) cancelledCount / (totalRegistrations + cancelledCount) * 100 : 0;
        analytics.setCancellationRate(Math.round(cancellationRate * 100.0) / 100.0);

        // Répartition par mode
        analytics.setPhysicalCapacity(event.getPhysicalCapacity());
        analytics.setPhysicalRegistered(event.getPhysicalRegistered());
        analytics.setOnlineCapacity(event.getOnlineCapacity());
        analytics.setOnlineRegistered(event.getOnlineRegistered());

        // Statistiques financières
        EventRevenueResponse revenue = calculateRevenue(eventId);
        analytics.setTotalRevenue(revenue.getTotalRevenue());
        analytics.setPotentialRevenue(revenue.getPotentialRevenue());
        analytics.setPaidRegistrations(revenue.getPaidRegistrations());
        analytics.setPendingPayments(revenue.getPendingPayments());

        // Indicateurs de performance
        analytics.setPerformanceIndicator(calculatePerformanceIndicator(fillRate, cancellationRate));
        analytics.setRecommendation(generateRecommendation(event, analytics));

        return analytics;
    }

    /**
     * Calcule les revenus d'un événement
     */
    public EventRevenueResponse calculateRevenue(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        EventRevenueResponse revenue = new EventRevenueResponse();
        revenue.setEventId(eventId);

        if (!event.getIsPaid() || event.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            // Événement gratuit
            revenue.setTotalRevenue(BigDecimal.ZERO);
            revenue.setPotentialRevenue(BigDecimal.ZERO);
            revenue.setPaidRegistrations(0L);
            revenue.setPendingPayments(0L);
            revenue.setAverageRevenuePerParticipant(BigDecimal.ZERO);
            return revenue;
        }

        Long paidCount = registrationRepository.countPaidRegistrationsByEventId(eventId);
        revenue.setPaidRegistrations(paidCount);

        BigDecimal totalRevenue = event.getPrice().multiply(BigDecimal.valueOf(paidCount));
        revenue.setTotalRevenue(totalRevenue);

        Long confirmedCount = registrationRepository.countConfirmedByEventIdAndMode(eventId, EventMode.PHYSICAL) +
                             registrationRepository.countConfirmedByEventIdAndMode(eventId, EventMode.ONLINE);
        BigDecimal potentialRevenue = event.getPrice().multiply(BigDecimal.valueOf(confirmedCount));
        revenue.setPotentialRevenue(potentialRevenue);

        Long pendingPayments = confirmedCount - paidCount;
        revenue.setPendingPayments(pendingPayments);

        BigDecimal averageRevenue = confirmedCount > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(confirmedCount), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        revenue.setAverageRevenuePerParticipant(averageRevenue);

        return revenue;
    }

    private String calculatePerformanceIndicator(double fillRate, double cancellationRate) {
        if (fillRate >= 90 && cancellationRate <= 5) {
            return "EXCELLENT";
        } else if (fillRate >= 70 && cancellationRate <= 10) {
            return "GOOD";
        } else if (fillRate >= 50 && cancellationRate <= 20) {
            return "AVERAGE";
        } else {
            return "POOR";
        }
    }

    private String generateRecommendation(Event event, EventAnalyticsResponse analytics) {
        StringBuilder recommendation = new StringBuilder();

        // Recommandations basées sur le taux de remplissage
        if (analytics.getFillRate() >= 90) {
            recommendation.append("Événement très populaire. ");
            if (analytics.getWaitingListSize() > event.getTotalCapacity() * 0.5) {
                recommendation.append("Considérez une duplication automatique. ");
            }
        } else if (analytics.getFillRate() < 30) {
            recommendation.append("Faible taux de participation. Renforcez la promotion. ");
        }

        // Recommandations basées sur les annulations
        if (analytics.getCancellationRate() > 15) {
            recommendation.append("Taux d'annulation élevé. Vérifiez la qualité de l'événement. ");
        }

        // Recommandations financières
        if (analytics.getPendingPayments() > analytics.getPaidRegistrations()) {
            recommendation.append("Beaucoup de paiements en attente. Relancez les participants. ");
        }

        return recommendation.length() > 0 ? recommendation.toString().trim() : "Aucune recommandation particulière.";
    }
}