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
    @Mock private OfferMapper offerMapper;

    @InjectMocks private OfferService offerService;

    private Offer sampleOffer;
    private OfferResponse sampleResponse;
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

        sampleResponse = OfferResponse.builder()
                .id(1L).title("Développeur Java").companyId(10L)
                .status("ACTIVE").viewCount(0L).positions(1).remote(false)
                .open(true).build();

        sampleRequest = OfferRequest.builder()
                .title("Développeur Java").description("Poste de développeur Java senior")
                .companyName("SMARTEK").location("Paris").contractType("CDI")
                .companyId(10L).build();
    }

    @Test
    @DisplayName("createOffer — retourne l'offre créée")
    void createOffer_shouldReturnCreatedOffer() {
        when(offerRepository.save(any())).thenReturn(sampleOffer);
        when(offerMapper.toResponse(any())).thenReturn(sampleResponse);

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
        when(offerMapper.toResponse(any())).thenReturn(sampleResponse);

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
        when(offerMapper.toResponse(any())).thenReturn(sampleResponse);

        List<OfferResponse> result = offerService.getOffersByCompanyId(10L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("updateOffer — met à jour et retourne l'offre")
    void updateOffer_shouldUpdateAndReturn() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(sampleOffer));
        when(offerRepository.save(any())).thenReturn(sampleOffer);
        when(offerMapper.toResponse(any())).thenReturn(sampleResponse);

        OfferResponse result = offerService.updateOffer(1L, sampleRequest);

        assertThat(result).isNotNull();
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
}
