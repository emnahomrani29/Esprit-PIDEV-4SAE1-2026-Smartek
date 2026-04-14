package com.smartek.offersservice.scheduler;

import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OfferExpiryScheduler.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OfferExpiryScheduler Unit Tests")
class OfferExpirySchedulerTest {

    @Mock private OfferRepository offerRepository;

    @InjectMocks private OfferExpiryScheduler scheduler;

    private Offer expiredOffer1;
    private Offer expiredOffer2;

    @BeforeEach
    void setUp() {
        expiredOffer1 = new Offer();
        expiredOffer1.setId(1L);
        expiredOffer1.setTitle("Java Dev");
        expiredOffer1.setStatus("ACTIVE");
        expiredOffer1.setExpiresAt(LocalDateTime.now().minusDays(1));
        expiredOffer1.setViewCount(0L);

        expiredOffer2 = new Offer();
        expiredOffer2.setId(2L);
        expiredOffer2.setTitle("React Dev");
        expiredOffer2.setStatus("ACTIVE");
        expiredOffer2.setExpiresAt(LocalDateTime.now().minusHours(2));
        expiredOffer2.setViewCount(0L);
    }

    @Test
    @DisplayName("Expired offers → all marked as EXPIRED")
    void expiredOffers_allMarkedExpired() {
        when(offerRepository.findExpiredOffers(any())).thenReturn(List.of(expiredOffer1, expiredOffer2));
        when(offerRepository.saveAll(any())).thenReturn(List.of());

        scheduler.closeExpiredOffers();

        assertThat(expiredOffer1.getStatus()).isEqualTo("EXPIRED");
        assertThat(expiredOffer2.getStatus()).isEqualTo("EXPIRED");
        verify(offerRepository).saveAll(any());
    }

    @Test
    @DisplayName("No expired offers → saveAll not called")
    void noExpiredOffers_saveAllNotCalled() {
        when(offerRepository.findExpiredOffers(any())).thenReturn(List.of());

        scheduler.closeExpiredOffers();

        verify(offerRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Exactly 1 expired offer → only that one updated")
    void oneExpiredOffer_onlyThatOneUpdated() {
        when(offerRepository.findExpiredOffers(any())).thenReturn(List.of(expiredOffer1));
        when(offerRepository.saveAll(any())).thenReturn(List.of());

        scheduler.closeExpiredOffers();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Offer>> captor = ArgumentCaptor.forClass(List.class);
        verify(offerRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getStatus()).isEqualTo("EXPIRED");
    }
}
