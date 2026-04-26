package com.smartek.event.service;

import com.smartek.event.dto.EventAnalyticsResponse;
import com.smartek.event.dto.EventRevenueResponse;
import com.smartek.event.model.*;
import com.smartek.event.repository.EventRegistrationRepository;
import com.smartek.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventAnalyticsService - Tests unitaires")
class EventAnalyticsServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventRegistrationRepository registrationRepository;

    @InjectMocks
    private EventAnalyticsService analyticsService;

    private Event freeEvent;
    private Event paidEvent;

    @BeforeEach
    void setUp() {
        freeEvent = new Event();
        freeEvent.setEventId(1L);
        freeEvent.setTitle("Formation Gratuite");
        freeEvent.setLocation("Tunis");
        freeEvent.setStartDate(LocalDateTime.now().plusDays(5));
        freeEvent.setEndDate(LocalDateTime.now().plusDays(6));
        freeEvent.setMaxParticipations(50);
        freeEvent.setCurrentParticipations(10);
        freeEvent.setPhysicalCapacity(50);
        freeEvent.setOnlineCapacity(0);
        freeEvent.setPhysicalRegistered(10);
        freeEvent.setOnlineRegistered(0);
        freeEvent.setPrice(BigDecimal.ZERO);
        freeEvent.setIsPaid(false);
        freeEvent.setCreatedBy(1L);
        freeEvent.setStatus(EventStatus.PUBLISHED);
        freeEvent.setMode(EventMode.PHYSICAL);

        paidEvent = new Event();
        paidEvent.setEventId(2L);
        paidEvent.setTitle("Formation Payante");
        paidEvent.setLocation("Tunis");
        paidEvent.setStartDate(LocalDateTime.now().plusDays(5));
        paidEvent.setEndDate(LocalDateTime.now().plusDays(6));
        paidEvent.setMaxParticipations(50);
        paidEvent.setCurrentParticipations(5);
        paidEvent.setPhysicalCapacity(50);
        paidEvent.setOnlineCapacity(0);
        paidEvent.setPhysicalRegistered(5);
        paidEvent.setOnlineRegistered(0);
        paidEvent.setPrice(new BigDecimal("100.00"));
        paidEvent.setIsPaid(true);
        paidEvent.setCreatedBy(1L);
        paidEvent.setStatus(EventStatus.PUBLISHED);
        paidEvent.setMode(EventMode.PHYSICAL);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAnalytics
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAnalytics()")
    class GetAnalytics {

        @Test
        @DisplayName("Doit calculer les analytics d'un événement gratuit")
        void shouldCalculateAnalyticsForFreeEvent() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(freeEvent));
            when(registrationRepository.countConfirmedByEventIdAndMode(1L, EventMode.PHYSICAL)).thenReturn(10L);
            when(registrationRepository.countConfirmedByEventIdAndMode(1L, EventMode.ONLINE)).thenReturn(0L);
            when(registrationRepository.findWaitingListByEventId(1L)).thenReturn(Collections.emptyList());
            when(registrationRepository.countCancelledByEventId(1L)).thenReturn(0L);
            when(registrationRepository.countPaidRegistrationsByEventId(1L)).thenReturn(0L);

            EventAnalyticsResponse result = analyticsService.getAnalytics(1L);

            assertThat(result).isNotNull();
            assertThat(result.getEventId()).isEqualTo(1L);
            assertThat(result.getEventTitle()).isEqualTo("Formation Gratuite");
            assertThat(result.getConfirmedRegistrations()).isEqualTo(10);
            assertThat(result.getTotalCapacity()).isEqualTo(50);
            assertThat(result.getFillRate()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("Doit retourner EXCELLENT si fillRate >= 90 et cancellationRate <= 5")
        void shouldReturnExcellentPerformance() {
            freeEvent.setPhysicalRegistered(46);
            freeEvent.setCurrentParticipations(46);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(freeEvent));
            when(registrationRepository.countConfirmedByEventIdAndMode(1L, EventMode.PHYSICAL)).thenReturn(46L);
            when(registrationRepository.countConfirmedByEventIdAndMode(1L, EventMode.ONLINE)).thenReturn(0L);
            when(registrationRepository.findWaitingListByEventId(1L)).thenReturn(Collections.emptyList());
            when(registrationRepository.countCancelledByEventId(1L)).thenReturn(0L);
            when(registrationRepository.countPaidRegistrationsByEventId(1L)).thenReturn(0L);

            EventAnalyticsResponse result = analyticsService.getAnalytics(1L);

            assertThat(result.getPerformanceIndicator()).isEqualTo("EXCELLENT");
        }

        @Test
        @DisplayName("Doit lever une exception si l'événement n'existe pas")
        void shouldThrowWhenEventNotFound() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> analyticsService.getAnalytics(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calculateRevenue
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calculateRevenue()")
    class CalculateRevenue {

        @Test
        @DisplayName("Doit retourner zéro pour un événement gratuit")
        void shouldReturnZeroForFreeEvent() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(freeEvent));

            EventRevenueResponse result = analyticsService.calculateRevenue(1L);

            assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getPaidRegistrations()).isZero();
        }

        @Test
        @DisplayName("Doit calculer les revenus pour un événement payant")
        void shouldCalculateRevenueForPaidEvent() {
            when(eventRepository.findById(2L)).thenReturn(Optional.of(paidEvent));
            when(registrationRepository.countPaidRegistrationsByEventId(2L)).thenReturn(3L);
            when(registrationRepository.countConfirmedByEventIdAndMode(2L, EventMode.PHYSICAL)).thenReturn(5L);
            when(registrationRepository.countConfirmedByEventIdAndMode(2L, EventMode.ONLINE)).thenReturn(0L);

            EventRevenueResponse result = analyticsService.calculateRevenue(2L);

            assertThat(result.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("300.00"));
            assertThat(result.getPaidRegistrations()).isEqualTo(3L);
            assertThat(result.getPendingPayments()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Doit lever une exception si l'événement n'existe pas")
        void shouldThrowWhenEventNotFound() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> analyticsService.calculateRevenue(99L))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
