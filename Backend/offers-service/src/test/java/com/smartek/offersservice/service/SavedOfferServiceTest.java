package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.entity.SavedOffer;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.repository.OfferRepository;
import com.smartek.offersservice.repository.SavedOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SavedOfferService.
 * Covers: save, unsave, duplicate prevention, offer not found.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SavedOfferService Unit Tests")
class SavedOfferServiceTest {

    @Mock private SavedOfferRepository savedOfferRepository;
    @Mock private OfferRepository offerRepository;

    @InjectMocks private SavedOfferService savedOfferService;

    private Offer offer;
    private SavedOffer savedOffer;

    @BeforeEach
    void setUp() {
        offer = new Offer();
        offer.setId(1L);
        offer.setTitle("Java Developer");
        offer.setDescription("desc");
        offer.setCompanyName("Smartek");
        offer.setLocation("Tunis");
        offer.setContractType("CDI");
        offer.setCompanyId(10L);
        offer.setStatus("ACTIVE");
        offer.setViewCount(0L);
        offer.setRemote(false);
        offer.setPositions(1);

        savedOffer = new SavedOffer();
        savedOffer.setId(1L);
        savedOffer.setOfferId(1L);
        savedOffer.setLearnerId(2L);
    }

    // ─── SAVE OFFER ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Save new offer → saved successfully")
    void saveNewOffer_savedSuccessfully() {
        when(offerRepository.existsById(1L)).thenReturn(true);
        when(savedOfferRepository.existsByOfferIdAndLearnerId(1L, 2L)).thenReturn(false);

        assertThatCode(() -> savedOfferService.saveOffer(1L, 2L)).doesNotThrowAnyException();
        verify(savedOfferRepository).save(any());
    }

    @Test
    @DisplayName("Save offer that doesn't exist → ResourceNotFoundException")
    void saveNonExistingOffer_throwsResourceNotFoundException() {
        when(offerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> savedOfferService.saveOffer(99L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(savedOfferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Save already saved offer → BusinessException")
    void saveAlreadySavedOffer_throwsBusinessException() {
        when(offerRepository.existsById(1L)).thenReturn(true);
        when(savedOfferRepository.existsByOfferIdAndLearnerId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> savedOfferService.saveOffer(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("favoris");

        verify(savedOfferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Save offer → correct offerId and learnerId stored")
    void saveOffer_correctDataStored() {
        when(offerRepository.existsById(1L)).thenReturn(true);
        when(savedOfferRepository.existsByOfferIdAndLearnerId(1L, 2L)).thenReturn(false);

        savedOfferService.saveOffer(1L, 2L);

        ArgumentCaptor<SavedOffer> captor = ArgumentCaptor.forClass(SavedOffer.class);
        verify(savedOfferRepository).save(captor.capture());
        assertThat(captor.getValue().getOfferId()).isEqualTo(1L);
        assertThat(captor.getValue().getLearnerId()).isEqualTo(2L);
    }

    // ─── UNSAVE OFFER ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Unsave existing saved offer → deleted successfully")
    void unsaveExistingOffer_deletedSuccessfully() {
        when(savedOfferRepository.existsByOfferIdAndLearnerId(1L, 2L)).thenReturn(true);

        assertThatCode(() -> savedOfferService.unsaveOffer(1L, 2L)).doesNotThrowAnyException();
        verify(savedOfferRepository).deleteByOfferIdAndLearnerId(1L, 2L);
    }

    @Test
    @DisplayName("Unsave non-existing saved offer → ResourceNotFoundException")
    void unsaveNonExistingOffer_throwsResourceNotFoundException() {
        when(savedOfferRepository.existsByOfferIdAndLearnerId(1L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> savedOfferService.unsaveOffer(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(savedOfferRepository, never()).deleteByOfferIdAndLearnerId(any(), any());
    }

    // ─── GET SAVED OFFERS ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Get saved offers by learner → returns mapped list")
    void getSavedOffersByLearner_returnsMappedList() {
        when(savedOfferRepository.findByLearnerId(2L)).thenReturn(List.of(savedOffer));
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));

        List<OfferResponse> result = savedOfferService.getSavedOffersByLearner(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Developer");
    }

    @Test
    @DisplayName("Get saved offers — offer deleted → filtered out gracefully")
    void getSavedOffers_deletedOffer_filteredOut() {
        when(savedOfferRepository.findByLearnerId(2L)).thenReturn(List.of(savedOffer));
        when(offerRepository.findById(1L)).thenReturn(Optional.empty()); // Offer deleted

        List<OfferResponse> result = savedOfferService.getSavedOffersByLearner(2L);

        assertThat(result).isEmpty(); // Gracefully filtered
    }

    // ─── IS SAVED ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("isSaved — true when saved")
    void isSaved_true_whenSaved() {
        when(savedOfferRepository.existsByOfferIdAndLearnerId(1L, 2L)).thenReturn(true);

        assertThat(savedOfferService.isSaved(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("isSaved — false when not saved")
    void isSaved_false_whenNotSaved() {
        when(savedOfferRepository.existsByOfferIdAndLearnerId(1L, 99L)).thenReturn(false);

        assertThat(savedOfferService.isSaved(1L, 99L)).isFalse();
    }
}
