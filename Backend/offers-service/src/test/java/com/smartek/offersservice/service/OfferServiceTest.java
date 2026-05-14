package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewRepository;
import com.smartek.offersservice.repository.OfferRepository;
import com.smartek.offersservice.repository.SavedOfferRepository;
import com.smartek.offersservice.mapper.OfferMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OfferService - Tests unitaires")
class OfferServiceTest {

    @Mock private OfferRepository offerRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private InterviewRepository interviewRepository;
    @Mock private SavedOfferRepository savedOfferRepository;
    @Mock private OfferMapper offerMapper;

    @InjectMocks private OfferService offerService;

    private Offer sampleOffer;
    private OfferRequest sampleRequest;
    private OfferResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleOffer = Offer.builder()
                .id(1L)
                .title("Développeur Java Senior")
                .description("Poste de développeur Java")
                .companyName("TechCorp")
                .location("Tunis")
                .contractType("CDI")
                .companyId(10L)
                .status("ACTIVE")
                .remote(false)
                .positions(2)
                .viewCount(0L)
                .build();

        sampleRequest = OfferRequest.builder()
                .title("Développeur Java Senior")
                .description("Poste de développeur Java")
                .companyName("TechCorp")
                .location("Tunis")
                .contractType("CDI")
                .companyId(10L)
                .status("ACTIVE")
                .remote(false)
                .positions(2)
                .build();

        sampleResponse = OfferResponse.builder()
                .id(1L)
                .title("Développeur Java Senior")
                .companyName("TechCorp")
                .location("Tunis")
                .contractType("CDI")
                .companyId(10L)
                .status("ACTIVE")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createOffer
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createOffer()")
    class CreateOffer {

        @Test
        @DisplayName("Doit créer une offre avec succès")
        void shouldCreateOfferSuccessfully() {
            when(offerRepository.save(any(Offer.class))).thenReturn(sampleOffer);

            OfferResponse result = offerService.createOffer(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Développeur Java Senior");
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            verify(offerRepository).save(any(Offer.class));
        }

        @Test
        @DisplayName("Doit lever BusinessException si la date d'expiration est dans le passé")
        void shouldThrowWhenExpiryDateIsInThePast() {
            sampleRequest.setExpiresAt(LocalDateTime.now().minusDays(1));

            assertThatThrownBy(() -> offerService.createOffer(sampleRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("passé");

            verify(offerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit utiliser ACTIVE comme statut par défaut si non spécifié")
        void shouldDefaultToActiveStatus() {
            sampleRequest.setStatus(null);
            when(offerRepository.save(any(Offer.class))).thenReturn(sampleOffer);

            OfferResponse result = offerService.createOffer(sampleRequest);

            assertThat(result.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Doit accepter une date d'expiration dans le futur")
        void shouldAcceptFutureExpiryDate() {
            sampleRequest.setExpiresAt(LocalDateTime.now().plusDays(30));
            when(offerRepository.save(any(Offer.class))).thenReturn(sampleOffer);

            assertThatCode(() -> offerService.createOffer(sampleRequest))
                    .doesNotThrowAnyException();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getOfferById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getOfferById()")
    class GetOfferById {

        @Test
        @DisplayName("Doit retourner l'offre et incrémenter le compteur de vues")
        void shouldReturnOfferAndIncrementViewCount() {
            when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
            doNothing().when(offerRepository).incrementViewCount(1L);

            OfferResponse result = offerService.getOfferById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Développeur Java Senior");
            verify(offerRepository).incrementViewCount(1L);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si l'offre n'existe pas")
        void shouldThrowWhenOfferNotFound() {
            when(offerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> offerService.getOfferById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getOffersByCompanyId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getOffersByCompanyId()")
    class GetOffersByCompanyId {

        @Test
        @DisplayName("Doit retourner les offres d'une entreprise")
        void shouldReturnOffersByCompany() {
            when(offerRepository.findByCompanyId(10L)).thenReturn(List.of(sampleOffer));
            when(applicationRepository.countByOfferId(1L)).thenReturn(5L);

            List<OfferResponse> result = offerService.getOffersByCompanyId(10L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getApplicationCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune offre pour cette entreprise")
        void shouldReturnEmptyListWhenNoOffers() {
            when(offerRepository.findByCompanyId(99L)).thenReturn(Collections.emptyList());

            List<OfferResponse> result = offerService.getOffersByCompanyId(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateOffer
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateOffer()")
    class UpdateOffer {

        @Test
        @DisplayName("Doit mettre à jour une offre existante")
        void shouldUpdateOfferSuccessfully() {
            when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
            when(offerRepository.save(any(Offer.class))).thenReturn(sampleOffer);

            OfferResponse result = offerService.updateOffer(1L, sampleRequest);

            assertThat(result).isNotNull();
            verify(offerRepository).save(any(Offer.class));
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si l'offre à mettre à jour n'existe pas")
        void shouldThrowWhenUpdatingNonExistentOffer() {
            when(offerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> offerService.updateOffer(99L, sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteOffer
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteOffer()")
    class DeleteOffer {

        @Test
        @DisplayName("Doit supprimer une offre sans candidatures acceptées")
        void shouldDeleteOfferWithNoAcceptedApplications() {
            when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
            when(applicationRepository.countByOfferIdAndStatus(1L, "ACCEPTED")).thenReturn(0L);

            offerService.deleteOffer(1L);

            verify(offerRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Doit lever BusinessException si l'offre a des candidatures acceptées")
        void shouldThrowWhenOfferHasAcceptedApplications() {
            when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
            when(applicationRepository.countByOfferIdAndStatus(1L, "ACCEPTED")).thenReturn(2L);

            assertThatThrownBy(() -> offerService.deleteOffer(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("acceptées");

            verify(offerRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si l'offre n'existe pas")
        void shouldThrowWhenOfferNotFound() {
            when(offerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> offerService.deleteOffer(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTopViewedOffers
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTopViewedOffers()")
    class GetTopViewedOffers {

        @Test
        @DisplayName("Doit retourner les offres les plus vues")
        void shouldReturnTopViewedOffers() {
            when(offerRepository.findTopViewedOffers(any())).thenReturn(List.of(sampleOffer));

            List<OfferResponse> result = offerService.getTopViewedOffers(5);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune offre")
        void shouldReturnEmptyListWhenNoOffers() {
            when(offerRepository.findTopViewedOffers(any())).thenReturn(Collections.emptyList());

            List<OfferResponse> result = offerService.getTopViewedOffers(5);

            assertThat(result).isEmpty();
        }
    }
}
