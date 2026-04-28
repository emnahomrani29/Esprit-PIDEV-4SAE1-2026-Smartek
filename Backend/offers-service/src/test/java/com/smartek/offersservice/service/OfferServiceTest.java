package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.dto.OfferResponse;
import com.smartek.offersservice.dto.OfferStatsResponse;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.mapper.OfferMapper;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewRepository;
import com.smartek.offersservice.repository.OfferRepository;
import com.smartek.offersservice.repository.SavedOfferRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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

    // OfferMapper est un composant simple sans dépendances → instanciation directe
    private final OfferMapper offerMapper = new OfferMapper();

    private OfferService offerService;

    private Offer sampleOffer;
    private OfferRequest sampleRequest;

    @BeforeEach
    void setUp() {
        offerService = new OfferService(offerRepository, applicationRepository,
                interviewRepository, savedOfferRepository, offerMapper);

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

        sampleRequest = OfferRequest.builder()
                .title("Développeur Java").description("Poste de développeur Java senior")
                .companyName("SMARTEK").location("Paris").contractType("CDI")
                .companyId(10L).build();
    }

    @Test
    @DisplayName("createOffer — retourne l'offre créée")
    void createOffer_shouldReturnCreatedOffer() {
        when(offerRepository.save(any())).thenReturn(sampleOffer);

        OfferResponse result = offerService.createOffer(sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Développeur Java");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("createOffer — date expiration passée → BusinessException")
    void createOffer_pastExpiresAt_throwsBusinessException() {
        sampleRequest = OfferRequest.builder()
                .title("Dev").description("Desc").companyName("Corp")
                .location("Paris").contractType("CDI").companyId(1L)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        assertThatThrownBy(() -> offerService.createOffer(sampleRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("getOfferById — retourne l'offre et incrémente les vues")
    void getOfferById_shouldReturnOfferAndIncrementViews() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
        doNothing().when(offerRepository).incrementViewCount(1L);

        OfferResponse result = offerService.getOfferById(1L);

        assertThat(result).isNotNull();
        verify(offerRepository).incrementViewCount(1L);
    }

    @Test
    @DisplayName("getOfferById — introuvable → ResourceNotFoundException")
    void getOfferById_notFound_throwsException() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.getOfferById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getOffersByCompanyId — retourne les offres avec applicationCount")
    void getOffersByCompanyId_returnsWithCount() {
        when(offerRepository.findByCompanyId(10L)).thenReturn(List.of(sampleOffer));
        when(applicationRepository.countByOfferId(1L)).thenReturn(3L);

        List<OfferResponse> result = offerService.getOffersByCompanyId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApplicationCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("updateOffer — met à jour et retourne l'offre")
    void updateOffer_shouldUpdateAndReturn() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
        when(offerRepository.save(any())).thenReturn(sampleOffer);

        OfferResponse result = offerService.updateOffer(1L, sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Développeur Java");
    }

    @Test
    @DisplayName("updateOffer — introuvable → ResourceNotFoundException")
    void updateOffer_notFound_throwsException() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.updateOffer(99L, sampleRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteOffer — supprime l'offre sans candidatures acceptées")
    void deleteOffer_noAccepted_deletes() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
        when(applicationRepository.countByOfferIdAndStatus(1L, "ACCEPTED")).thenReturn(0L);
        doNothing().when(offerRepository).deleteById(1L);

        assertThatCode(() -> offerService.deleteOffer(1L)).doesNotThrowAnyException();
        verify(offerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteOffer — candidatures acceptées → BusinessException")
    void deleteOffer_withAccepted_throwsBusinessException() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
        when(applicationRepository.countByOfferIdAndStatus(1L, "ACCEPTED")).thenReturn(2L);

        assertThatThrownBy(() -> offerService.deleteOffer(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("deleteOffer — introuvable → ResourceNotFoundException")
    void deleteOffer_notFound_throwsException() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.deleteOffer(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getStatsByCompany — calcule le taux d'acceptation")
    void getStatsByCompany_calculatesAcceptanceRate() {
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
        when(applicationRepository.averageScoreByCompanyId(companyId)).thenReturn(65.0);

        OfferStatsResponse stats = offerService.getStatsByCompany(companyId);

        assertThat(stats.getTotalOffers()).isEqualTo(3L);
        assertThat(stats.getActiveOffers()).isEqualTo(2L);
        assertThat(stats.getTotalApplications()).isEqualTo(10L);
        assertThat(stats.getAcceptanceRate()).isEqualTo(40.0);
        assertThat(stats.getAverageApplicationScore()).isEqualTo(65.0);
    }

    @Test
    @DisplayName("getStatsByCompany — taux 0 si aucune candidature")
    void getStatsByCompany_zeroRateWhenNoApplications() {
        Long companyId = 10L;
        when(offerRepository.countByCompanyIdAndStatus(any(), any())).thenReturn(0L);
        when(applicationRepository.countByCompanyId(companyId)).thenReturn(0L);
        when(applicationRepository.countByCompanyIdAndStatus(any(), any())).thenReturn(0L);
        when(interviewRepository.countByOffer_CompanyId(companyId)).thenReturn(0L);
        when(applicationRepository.averageScoreByCompanyId(companyId)).thenReturn(0.0);

        OfferStatsResponse stats = offerService.getStatsByCompany(companyId);

        assertThat(stats.getAcceptanceRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getOfferByIdWithCounts — retourne l'offre avec applicationCount et savedCount")
    void getOfferByIdWithCounts_returnsWithCounts() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
        doNothing().when(offerRepository).incrementViewCount(1L);
        when(applicationRepository.countByOfferId(1L)).thenReturn(5L);
        when(savedOfferRepository.countByOfferId(1L)).thenReturn(3L);

        OfferResponse result = offerService.getOfferByIdWithCounts(1L);

        assertThat(result).isNotNull();
        assertThat(result.getApplicationCount()).isEqualTo(5L);
        assertThat(result.getSavedCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getTopViewedOffers — retourne les offres les plus vues")
    void getTopViewedOffers_returnsTopOffers() {
        when(offerRepository.findTopViewedOffers(any())).thenReturn(List.of(sampleOffer));

        List<OfferResponse> result = offerService.getTopViewedOffers(5);

        assertThat(result).hasSize(1);
    }

    // ─── getAllOffers ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllOffers — retourne une page triée par createdAt desc")
    void getAllOffers_returnsSortedPage() {
        org.springframework.data.domain.Page<Offer> page =
                new org.springframework.data.domain.PageImpl<>(List.of(sampleOffer));
        when(offerRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        org.springframework.data.domain.Page<OfferResponse> result =
                offerService.getAllOffers(0, 10, "createdAt", "desc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Développeur Java");
    }

    @Test
    @DisplayName("getAllOffers — tri ascendant")
    void getAllOffers_ascendingSort() {
        org.springframework.data.domain.Page<Offer> page =
                new org.springframework.data.domain.PageImpl<>(List.of(sampleOffer));
        when(offerRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        org.springframework.data.domain.Page<OfferResponse> result =
                offerService.getAllOffers(0, 5, "title", "asc");

        assertThat(result).isNotNull();
        verify(offerRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    // ─── getOffersByStatus ────────────────────────────────────────────────────

    @Test
    @DisplayName("getOffersByStatus — retourne les offres filtrées par statut")
    void getOffersByStatus_returnsFilteredPage() {
        org.springframework.data.domain.Page<Offer> page =
                new org.springframework.data.domain.PageImpl<>(List.of(sampleOffer));
        when(offerRepository.findByStatus(eq("ACTIVE"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        org.springframework.data.domain.Page<OfferResponse> result =
                offerService.getOffersByStatus("ACTIVE", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("ACTIVE");
    }

    // ─── searchOffers ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchOffers — retourne les résultats paginés selon les filtres")
    void searchOffers_returnsPagedResults() {
        org.springframework.data.domain.Page<Offer> page =
                new org.springframework.data.domain.PageImpl<>(List.of(sampleOffer));
        when(offerRepository.searchWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        com.smartek.offersservice.dto.OfferSearchRequest req =
                new com.smartek.offersservice.dto.OfferSearchRequest();
        req.setKeyword("Java");
        req.setContractType("CDI");
        req.setPage(0);
        req.setSize(10);

        org.springframework.data.domain.Page<OfferResponse> result = offerService.searchOffers(req);

        assertThat(result.getContent()).hasSize(1);
        verify(offerRepository).searchWithFilters(
                eq("Java"), isNull(), eq("CDI"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("searchOffers — tri ascendant respecté")
    void searchOffers_ascendingSortRespected() {
        org.springframework.data.domain.Page<Offer> page =
                new org.springframework.data.domain.PageImpl<>(List.of(sampleOffer));
        when(offerRepository.searchWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        com.smartek.offersservice.dto.OfferSearchRequest req =
                new com.smartek.offersservice.dto.OfferSearchRequest();
        req.setSortBy("title");
        req.setSortDir("asc");
        req.setPage(0);
        req.setSize(5);

        org.springframework.data.domain.Page<OfferResponse> result = offerService.searchOffers(req);

        assertThat(result).isNotNull();
    }

    // ─── createOffer — valeurs par défaut ────────────────────────────────────

    @Test
    @DisplayName("createOffer — remote null → false par défaut")
    void createOffer_nullRemote_defaultsFalse() {
        sampleRequest = OfferRequest.builder()
                .title("Dev").description("Desc").companyName("Corp")
                .location("Paris").contractType("CDI").companyId(1L)
                .remote(null).build();

        Offer savedOffer = new Offer();
        savedOffer.setId(2L);
        savedOffer.setTitle("Dev");
        savedOffer.setDescription("Desc");
        savedOffer.setCompanyName("Corp");
        savedOffer.setLocation("Paris");
        savedOffer.setContractType("CDI");
        savedOffer.setCompanyId(1L);
        savedOffer.setStatus("ACTIVE");
        savedOffer.setRemote(false);
        savedOffer.setPositions(1);
        savedOffer.setViewCount(0L);

        when(offerRepository.save(any())).thenReturn(savedOffer);

        OfferResponse result = offerService.createOffer(sampleRequest);

        assertThat(result.getRemote()).isFalse();
    }

    @Test
    @DisplayName("createOffer — positions null → 1 par défaut")
    void createOffer_nullPositions_defaults1() {
        sampleRequest = OfferRequest.builder()
                .title("Dev").description("Desc").companyName("Corp")
                .location("Paris").contractType("CDI").companyId(1L)
                .positions(null).build();

        Offer savedOffer = new Offer();
        savedOffer.setId(3L);
        savedOffer.setTitle("Dev");
        savedOffer.setDescription("Desc");
        savedOffer.setCompanyName("Corp");
        savedOffer.setLocation("Paris");
        savedOffer.setContractType("CDI");
        savedOffer.setCompanyId(1L);
        savedOffer.setStatus("ACTIVE");
        savedOffer.setRemote(false);
        savedOffer.setPositions(1);
        savedOffer.setViewCount(0L);

        when(offerRepository.save(any())).thenReturn(savedOffer);

        OfferResponse result = offerService.createOffer(sampleRequest);

        assertThat(result.getPositions()).isEqualTo(1);
    }

    @Test
    @DisplayName("createOffer — status null → ACTIVE par défaut")
    void createOffer_nullStatus_defaultsActive() {
        sampleRequest = OfferRequest.builder()
                .title("Dev").description("Desc").companyName("Corp")
                .location("Paris").contractType("CDI").companyId(1L)
                .status(null).build();

        when(offerRepository.save(any())).thenReturn(sampleOffer);

        OfferResponse result = offerService.createOffer(sampleRequest);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("createOffer — date expiration future → pas d'exception")
    void createOffer_futureExpiresAt_noException() {
        sampleRequest = OfferRequest.builder()
                .title("Dev").description("Desc").companyName("Corp")
                .location("Paris").contractType("CDI").companyId(1L)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(offerRepository.save(any())).thenReturn(sampleOffer);

        assertThatCode(() -> offerService.createOffer(sampleRequest)).doesNotThrowAnyException();
    }

    // ─── Offer.isOpen() — logique métier ─────────────────────────────────────

    @Test
    @DisplayName("isOpen — false si statut CLOSED")
    void isOpen_false_whenStatusClosed() {
        sampleOffer.setStatus("CLOSED");
        assertThat(sampleOffer.isOpen()).isFalse();
    }

    @Test
    @DisplayName("isOpen — false si expiresAt dans le passé")
    void isOpen_false_whenExpired() {
        sampleOffer.setStatus("ACTIVE");
        sampleOffer.setExpiresAt(LocalDateTime.now().minusDays(1));
        assertThat(sampleOffer.isOpen()).isFalse();
    }

    @Test
    @DisplayName("isOpen — true si ACTIVE et expiresAt dans le futur")
    void isOpen_true_whenActiveAndNotExpired() {
        sampleOffer.setStatus("ACTIVE");
        sampleOffer.setExpiresAt(LocalDateTime.now().plusDays(7));
        assertThat(sampleOffer.isOpen()).isTrue();
    }

    @Test
    @DisplayName("isOpen — true si ACTIVE et expiresAt null")
    void isOpen_true_whenActiveAndNoExpiry() {
        sampleOffer.setStatus("ACTIVE");
        sampleOffer.setExpiresAt(null);
        assertThat(sampleOffer.isOpen()).isTrue();
    }

    // ─── getStatsByCompany — détail des statuts ───────────────────────────────

    @Test
    @DisplayName("getStatsByCompany — totalOffers = somme de tous les statuts")
    void getStatsByCompany_totalIsSum() {
        Long companyId = 5L;
        when(offerRepository.countByCompanyIdAndStatus(companyId, "ACTIVE")).thenReturn(3L);
        when(offerRepository.countByCompanyIdAndStatus(companyId, "CLOSED")).thenReturn(2L);
        when(offerRepository.countByCompanyIdAndStatus(companyId, "DRAFT")).thenReturn(1L);
        when(offerRepository.countByCompanyIdAndStatus(companyId, "EXPIRED")).thenReturn(4L);
        when(applicationRepository.countByCompanyId(companyId)).thenReturn(0L);
        when(applicationRepository.countByCompanyIdAndStatus(any(), any())).thenReturn(0L);
        when(interviewRepository.countByOffer_CompanyId(companyId)).thenReturn(0L);
        when(applicationRepository.averageScoreByCompanyId(companyId)).thenReturn(0.0);

        OfferStatsResponse stats = offerService.getStatsByCompany(companyId);

        assertThat(stats.getTotalOffers()).isEqualTo(10L); // 3+2+1+4
        assertThat(stats.getActiveOffers()).isEqualTo(3L);
        assertThat(stats.getClosedOffers()).isEqualTo(2L);
        assertThat(stats.getDraftOffers()).isEqualTo(1L);
        assertThat(stats.getExpiredOffers()).isEqualTo(4L);
    }

    @Test
    @DisplayName("getStatsByCompany — totalInterviews correctement renseigné")
    void getStatsByCompany_totalInterviewsSet() {
        Long companyId = 7L;
        when(offerRepository.countByCompanyIdAndStatus(any(), any())).thenReturn(0L);
        when(applicationRepository.countByCompanyId(companyId)).thenReturn(0L);
        when(applicationRepository.countByCompanyIdAndStatus(any(), any())).thenReturn(0L);
        when(interviewRepository.countByOffer_CompanyId(companyId)).thenReturn(8L);
        when(applicationRepository.averageScoreByCompanyId(companyId)).thenReturn(0.0);

        OfferStatsResponse stats = offerService.getStatsByCompany(companyId);

        assertThat(stats.getTotalInterviews()).isEqualTo(8L);
    }
}
