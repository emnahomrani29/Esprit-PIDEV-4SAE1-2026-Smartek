package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.dto.OfferStatsResponse;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewFeedbackRepository;
import com.smartek.offersservice.repository.InterviewRepository;
import com.smartek.offersservice.repository.OfferRepository;
import com.smartek.offersservice.repository.SavedOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OfferService — Tests Unitaires")
class OfferServiceTest {

    @Mock private OfferRepository offerRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private InterviewRepository interviewRepository;
    @Mock private SavedOfferRepository savedOfferRepository;
    @Mock private InterviewFeedbackRepository feedbackRepository;

    @InjectMocks
    private OfferService offerService;

    private Offer sampleOffer;
    private OfferRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleOffer = new Offer();
        sampleOffer.setId(1L);
        sampleOffer.setTitle("Développeur Java");
        sampleOffer.setDescription("Poste de développeur Java senior");
        sampleOffer.setCompanyName("SMARTEK");
        sampleOffer.setLocation("Paris");
        sampleOffer.setContractType("CDI");
        sampleOffer.setCompanyId(10L);
        sampleOffer.setStatus("ACTIVE");
        sampleOffer.setViewCount(0L);
        sampleOffer.setPositions(1);
        sampleOffer.setRemote(false);
        sampleOffer.setCreatedAt(LocalDateTime.now());
        sampleOffer.setUpdatedAt(LocalDateTime.now());

        sampleRequest = new OfferRequest();
        sampleRequest.setTitle("Développeur Java");
        sampleRequest.setDescription("Poste de développeur Java senior");
        sampleRequest.setCompanyName("SMARTEK");
        sampleRequest.setLocation("Paris");
        sampleRequest.setContractType("CDI");
        sampleRequest.setCompanyId(10L);
    }

    // ── CREATE ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createOffer — doit créer et retourner une offre")
    void createOffer_shouldReturnCreatedOffer() {
        when(offerRepository.save(any(Offer.class))).thenReturn(sampleOffer);

        OfferResponse result = offerService.createOffer(sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Développeur Java");
        assertThat(result.getCompanyId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(offerRepository, times(1)).save(any(Offer.class));
    }

    @Test
    @DisplayName("createOffer — statut par défaut ACTIVE si non fourni")
    void createOffer_defaultStatusIsActive() {
        sampleRequest.setStatus(null);
        when(offerRepository.save(any(Offer.class))).thenReturn(sampleOffer);

        OfferResponse result = offerService.createOffer(sampleRequest);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getOfferById — doit retourner l'offre et incrémenter les vues")
    void getOfferById_shouldReturnOfferAndIncrementViews() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
        doNothing().when(offerRepository).incrementViewCount(1L);

        OfferResponse result = offerService.getOfferById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(offerRepository).incrementViewCount(1L);
    }

    @Test
    @DisplayName("getOfferById — doit lever ResourceNotFoundException si introuvable")
    void getOfferById_shouldThrowWhenNotFound() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.getOfferById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── GET BY COMPANY ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getOffersByCompanyId — doit retourner les offres avec applicationCount")
    void getOffersByCompanyId_shouldReturnOffersWithCount() {
        when(offerRepository.findByCompanyId(10L)).thenReturn(List.of(sampleOffer));
        when(applicationRepository.countByOfferId(1L)).thenReturn(3L);

        List<OfferResponse> result = offerService.getOffersByCompanyId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApplicationCount()).isEqualTo(3L);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateOffer — doit mettre à jour et retourner l'offre modifiée")
    void updateOffer_shouldUpdateAndReturn() {
        sampleRequest.setTitle("Développeur Java Senior");
        Offer updated = new Offer();
        updated.setId(1L);
        updated.setTitle("Développeur Java Senior");
        updated.setCompanyId(10L);
        updated.setStatus("ACTIVE");
        updated.setViewCount(0L);
        updated.setPositions(1);
        updated.setRemote(false);
        updated.setCreatedAt(LocalDateTime.now());
        updated.setUpdatedAt(LocalDateTime.now());

        when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
        when(offerRepository.save(any(Offer.class))).thenReturn(updated);

        OfferResponse result = offerService.updateOffer(1L, sampleRequest);

        assertThat(result.getTitle()).isEqualTo("Développeur Java Senior");
    }

    @Test
    @DisplayName("updateOffer — doit lever RuntimeException si offre introuvable")
    void updateOffer_shouldThrowWhenNotFound() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.updateOffer(99L, sampleRequest))
                .isInstanceOf(RuntimeException.class);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteOffer — doit supprimer l'offre existante")
    void deleteOffer_shouldDeleteExistingOffer() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
        when(applicationRepository.countByOfferIdAndStatus(1L, "ACCEPTED")).thenReturn(0L);
        doNothing().when(offerRepository).deleteById(1L);

        assertThatCode(() -> offerService.deleteOffer(1L)).doesNotThrowAnyException();
        verify(offerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteOffer — doit lever ResourceNotFoundException si introuvable")
    void deleteOffer_shouldThrowWhenNotFound() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.deleteOffer(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── STATS ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getStatsByCompany — doit calculer le taux d'acceptation correctement")
    void getStatsByCompany_shouldCalculateAcceptanceRate() {
        Long companyId = 10L;
        when(offerRepository.countByCompanyIdAndStatus(companyId, "ACTIVE")).thenReturn(2L);
        when(offerRepository.countByCompanyIdAndStatus(companyId, "CLOSED")).thenReturn(1L);
        when(offerRepository.countByCompanyIdAndStatus(companyId, "DRAFT")).thenReturn(0L);
        when(offerRepository.countByCompanyIdAndStatus(companyId, "EXPIRED")).thenReturn(0L);
        when(applicationRepository.countByCompanyId(companyId)).thenReturn(10L);
        when(applicationRepository.countByCompanyIdAndStatus(companyId, "PENDING")).thenReturn(4L);
        when(applicationRepository.countByCompanyIdAndStatus(companyId, "ACCEPTED")).thenReturn(4L);
        when(applicationRepository.countByCompanyIdAndStatus(companyId, "REJECTED")).thenReturn(2L);
        when(interviewRepository.countByOffer_CompanyId(companyId)).thenReturn(3L);
        when(interviewRepository.countByOffer_CompanyIdAndStatus(eq(companyId), any())).thenReturn(2L);
        when(feedbackRepository.countByDecision(any())).thenReturn(1L);
        when(applicationRepository.averageScoreByCompanyId(companyId)).thenReturn(65.0);

        OfferStatsResponse stats = offerService.getStatsByCompany(companyId);

        assertThat(stats.getTotalOffers()).isEqualTo(3L);
        assertThat(stats.getActiveOffers()).isEqualTo(2L);
        assertThat(stats.getTotalApplications()).isEqualTo(10L);
        assertThat(stats.getAcceptanceRate()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("getStatsByCompany — taux 0 si aucune candidature")
    void getStatsByCompany_zeroRateWhenNoApplications() {
        Long companyId = 10L;
        when(offerRepository.countByCompanyIdAndStatus(any(), any())).thenReturn(0L);
        when(applicationRepository.countByCompanyId(companyId)).thenReturn(0L);
        when(applicationRepository.countByCompanyIdAndStatus(any(), any())).thenReturn(0L);
        when(interviewRepository.countByOffer_CompanyId(companyId)).thenReturn(0L);
        when(interviewRepository.countByOffer_CompanyIdAndStatus(any(), any())).thenReturn(0L);
        when(feedbackRepository.countByDecision(any())).thenReturn(0L);
        when(applicationRepository.averageScoreByCompanyId(companyId)).thenReturn(0.0);

        OfferStatsResponse stats = offerService.getStatsByCompany(companyId);

        assertThat(stats.getAcceptanceRate()).isEqualTo(0.0);
    }
}
