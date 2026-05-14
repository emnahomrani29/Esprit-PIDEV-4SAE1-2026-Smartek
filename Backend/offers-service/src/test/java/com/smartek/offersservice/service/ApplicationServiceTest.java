package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.ApplicationRequest;
import com.smartek.offersservice.dto.ApplicationResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Application.ApplicationStatus;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.mapper.ApplicationMapper;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService - Tests unitaires")
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private OfferRepository offerRepository;
    @Mock private ApplicationMapper applicationMapper;
    @Mock private ApplicationScoringService scoringService;

    @InjectMocks private ApplicationService applicationService;

    private Offer activeOffer;
    private Application sampleApplication;
    private ApplicationRequest sampleRequest;
    private ApplicationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        activeOffer = Offer.builder()
                .id(1L)
                .title("Développeur Java")
                .companyName("TechCorp")
                .location("Tunis")
                .contractType("CDI")
                .companyId(10L)
                .status("ACTIVE")
                .build();

        sampleApplication = new Application();
        sampleApplication.setId(1L);
        sampleApplication.setOfferId(1L);
        sampleApplication.setLearnerId(5L);
        sampleApplication.setStatus("PENDING");
        sampleApplication.setScore(75);

        sampleRequest = new ApplicationRequest();
        sampleRequest.setOfferId(1L);
        sampleRequest.setLearnerId(5L);
        sampleRequest.setYearsOfExperience(3);

        sampleResponse = new ApplicationResponse();
        sampleResponse.setId(1L);
        sampleResponse.setOfferId(1L);
        sampleResponse.setLearnerId(5L);
        sampleResponse.setStatus(ApplicationStatus.PENDING);
        sampleResponse.setScore(75);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // applyToOffer
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("applyToOffer()")
    class ApplyToOffer {

        @Test
        @DisplayName("Doit créer une candidature avec succès")
        void shouldApplySuccessfully() {
            when(applicationRepository.existsByOfferIdAndLearnerId(1L, 5L)).thenReturn(false);
            when(offerRepository.findById(1L)).thenReturn(Optional.of(activeOffer));
            when(applicationMapper.toEntity(sampleRequest)).thenReturn(sampleApplication);
            when(scoringService.calculateScore(any(), any(), anyInt())).thenReturn(75);
            when(applicationRepository.save(any(Application.class))).thenReturn(sampleApplication);
            when(applicationMapper.toResponse(sampleApplication)).thenReturn(sampleResponse);

            ApplicationResponse result = applicationService.applyToOffer(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(result.getScore()).isEqualTo(75);
            verify(applicationRepository).save(any(Application.class));
        }

        @Test
        @DisplayName("Doit lever BusinessException si l'apprenant a déjà postulé")
        void shouldThrowWhenAlreadyApplied() {
            when(applicationRepository.existsByOfferIdAndLearnerId(1L, 5L)).thenReturn(true);

            assertThatThrownBy(() -> applicationService.applyToOffer(sampleRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("déjà postulé");

            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si l'offre n'existe pas")
        void shouldThrowWhenOfferNotFound() {
            when(applicationRepository.existsByOfferIdAndLearnerId(1L, 5L)).thenReturn(false);
            when(offerRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.applyToOffer(sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Doit lever BusinessException si l'offre n'est plus active")
        void shouldThrowWhenOfferNotActive() {
            activeOffer.setStatus("CLOSED");
            when(applicationRepository.existsByOfferIdAndLearnerId(1L, 5L)).thenReturn(false);
            when(offerRepository.findById(1L)).thenReturn(Optional.of(activeOffer));

            assertThatThrownBy(() -> applicationService.applyToOffer(sampleRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("disponible");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getApplicationsByOffer
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getApplicationsByOffer()")
    class GetApplicationsByOffer {

        @Test
        @DisplayName("Doit retourner les candidatures d'une offre")
        void shouldReturnApplicationsByOffer() {
            when(applicationRepository.findByOfferId(1L)).thenReturn(List.of(sampleApplication));
            when(applicationMapper.toResponse(sampleApplication)).thenReturn(sampleResponse);

            List<ApplicationResponse> result = applicationService.getApplicationsByOffer(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune candidature")
        void shouldReturnEmptyListWhenNoApplications() {
            when(applicationRepository.findByOfferId(99L)).thenReturn(Collections.emptyList());

            List<ApplicationResponse> result = applicationService.getApplicationsByOffer(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getApplicationsByLearner
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getApplicationsByLearner()")
    class GetApplicationsByLearner {

        @Test
        @DisplayName("Doit retourner les candidatures d'un apprenant")
        void shouldReturnApplicationsByLearner() {
            when(applicationRepository.findByLearnerId(5L)).thenReturn(List.of(sampleApplication));
            when(applicationMapper.toResponse(sampleApplication)).thenReturn(sampleResponse);

            List<ApplicationResponse> result = applicationService.getApplicationsByLearner(5L);

            assertThat(result).hasSize(1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // hasApplied
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("hasApplied()")
    class HasApplied {

        @Test
        @DisplayName("Doit retourner true si l'apprenant a déjà postulé")
        void shouldReturnTrueWhenAlreadyApplied() {
            when(applicationRepository.existsByOfferIdAndLearnerId(1L, 5L)).thenReturn(true);

            assertThat(applicationService.hasApplied(1L, 5L)).isTrue();
        }

        @Test
        @DisplayName("Doit retourner false si l'apprenant n'a pas encore postulé")
        void shouldReturnFalseWhenNotApplied() {
            when(applicationRepository.existsByOfferIdAndLearnerId(1L, 99L)).thenReturn(false);

            assertThat(applicationService.hasApplied(1L, 99L)).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateApplicationStatus
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateApplicationStatus()")
    class UpdateApplicationStatus {

        @Test
        @DisplayName("Doit mettre à jour le statut d'une candidature")
        void shouldUpdateStatus() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
            when(applicationRepository.save(any(Application.class))).thenReturn(sampleApplication);
            when(applicationMapper.toResponse(sampleApplication)).thenReturn(sampleResponse);

            ApplicationResponse result = applicationService.updateApplicationStatus(1L, "ACCEPTED", "Bon profil");

            assertThat(result).isNotNull();
            verify(applicationRepository).save(any(Application.class));
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si la candidature n'existe pas")
        void shouldThrowWhenApplicationNotFound() {
            when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(99L, "ACCEPTED", null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // withdrawApplication
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("withdrawApplication()")
    class WithdrawApplication {

        @Test
        @DisplayName("Doit retirer une candidature appartenant à l'apprenant")
        void shouldWithdrawOwnApplication() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));
            when(applicationRepository.save(any(Application.class))).thenReturn(sampleApplication);
            when(applicationMapper.toResponse(sampleApplication)).thenReturn(sampleResponse);

            ApplicationResponse result = applicationService.withdrawApplication(1L, 5L);

            assertThat(result).isNotNull();
            verify(applicationRepository).save(any(Application.class));
        }

        @Test
        @DisplayName("Doit lever BusinessException si l'apprenant ne possède pas la candidature")
        void shouldThrowWhenNotOwner() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApplication));

            assertThatThrownBy(() -> applicationService.withdrawApplication(1L, 99L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("retirer");
        }
    }
}
