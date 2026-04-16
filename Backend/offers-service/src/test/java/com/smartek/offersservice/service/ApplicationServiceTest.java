package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.ApplicationRequest;
import com.smartek.offersservice.dto.ApplicationResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.mapper.ApplicationMapper;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.OfferRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService Unit Tests")
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private OfferRepository offerRepository;
    @Mock private ApplicationMapper applicationMapper;
    @Mock private ApplicationScoringService scoringService;

    @InjectMocks private ApplicationService applicationService;

    private Application pendingApp;
    private ApplicationResponse pendingResponse;
    private ApplicationRequest validRequest;
    private Offer activeOffer;

    @BeforeEach
    void setUp() {
        activeOffer = new Offer();
        activeOffer.setId(100L);
        activeOffer.setTitle("Dev Java");
        activeOffer.setStatus("ACTIVE");

        pendingApp = new Application();
        pendingApp.setId(1L);
        pendingApp.setOfferId(100L);
        pendingApp.setLearnerId(2L);
        pendingApp.setLearnerName("Alice Martin");
        pendingApp.setLearnerEmail("alice@test.com");
        pendingApp.setCoverLetter("Je suis motivée...");
        pendingApp.setStatus("PENDING");
        pendingApp.setScore(0);
        pendingApp.setAppliedAt(LocalDateTime.now());

        pendingResponse = ApplicationResponse.builder()
                .id(1L).offerId(100L).learnerId(2L)
                .learnerName("Alice Martin").learnerEmail("alice@test.com")
                .status(Application.ApplicationStatus.PENDING).score(0)
                .appliedAt(LocalDateTime.now())
                .build();

        validRequest = ApplicationRequest.builder()
                .offerId(100L).learnerId(2L)
                .learnerName("Alice Martin").learnerEmail("alice@test.com")
                .coverLetter("Je suis motivée...")
                .build();
    }

    @Nested
    @DisplayName("Apply to Offer")
    class ApplyTests {

        @Test
        @DisplayName("First application → saved with PENDING status and score")
        void firstApplication_savedWithPendingStatus() {
            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 2L)).thenReturn(false);
            when(offerRepository.findById(100L)).thenReturn(Optional.of(activeOffer));
            when(applicationMapper.toEntity(any())).thenReturn(pendingApp);
            when(scoringService.calculateScore(any(), any(), any())).thenReturn(42);
            when(applicationRepository.save(any())).thenReturn(pendingApp);
            when(applicationMapper.toResponse(any())).thenReturn(pendingResponse);

            ApplicationResponse result = applicationService.applyToOffer(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(Application.ApplicationStatus.PENDING);
            verify(applicationRepository).save(any());
        }

        @Test
        @DisplayName("Duplicate application → BusinessException")
        void duplicateApplication_throwsBusinessException() {
            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 2L)).thenReturn(true);

            assertThatThrownBy(() -> applicationService.applyToOffer(validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("déjà postulé");

            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Offer not ACTIVE → BusinessException")
        void closedOffer_throwsBusinessException() {
            Offer closedOffer = new Offer();
            closedOffer.setId(100L);
            closedOffer.setStatus("CLOSED");

            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 2L)).thenReturn(false);
            when(offerRepository.findById(100L)).thenReturn(Optional.of(closedOffer));

            assertThatThrownBy(() -> applicationService.applyToOffer(validRequest))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Offer not found → ResourceNotFoundException")
        void offerNotFound_throwsResourceNotFoundException() {
            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 2L)).thenReturn(false);
            when(offerRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.applyToOffer(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionTests {

        @ParameterizedTest(name = "Status ''{0}'' → updated correctly")
        @ValueSource(strings = {"ACCEPTED", "REJECTED", "PENDING"})
        void validStatusTransitions(String newStatus) {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApp));
            when(applicationRepository.save(any())).thenReturn(pendingApp);
            when(applicationMapper.toResponse(any())).thenReturn(pendingResponse);

            applicationService.updateApplicationStatus(1L, newStatus, null);

            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            verify(applicationRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(newStatus);
        }

        @Test
        @DisplayName("Update with recruiterNote → note saved")
        void updateWithRecruiterNote_noteSaved() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApp));
            when(applicationRepository.save(any())).thenReturn(pendingApp);
            when(applicationMapper.toResponse(any())).thenReturn(pendingResponse);

            applicationService.updateApplicationStatus(1L, "ACCEPTED", "Excellent profil");

            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            verify(applicationRepository).save(captor.capture());
            assertThat(captor.getValue().getRecruiterNote()).isEqualTo("Excellent profil");
        }

        @Test
        @DisplayName("Update non-existing → ResourceNotFoundException")
        void updateNonExisting_throwsException() {
            when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(99L, "ACCEPTED", null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Withdraw Application")
    class WithdrawTests {

        @Test
        @DisplayName("Withdraw own application → status WITHDRAWN")
        void withdrawOwnApplication_success() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApp));
            when(applicationRepository.save(any())).thenReturn(pendingApp);
            when(applicationMapper.toResponse(any())).thenReturn(pendingResponse);

            applicationService.withdrawApplication(1L, 2L);

            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            verify(applicationRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("WITHDRAWN");
        }

        @Test
        @DisplayName("Withdraw other's application → BusinessException")
        void withdrawOthersApplication_throwsException() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApp));

            assertThatThrownBy(() -> applicationService.withdrawApplication(1L, 999L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("Queries")
    class QueryTests {

        @Test
        @DisplayName("Get by offer → returns list")
        void getByOffer_returnsList() {
            when(applicationRepository.findByOfferId(100L)).thenReturn(List.of(pendingApp));
            when(applicationMapper.toResponse(any())).thenReturn(pendingResponse);

            List<ApplicationResponse> result = applicationService.getApplicationsByOffer(100L);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Get ranked by offer → sorted by score")
        void getRankedByOffer_returnsSortedList() {
            when(applicationRepository.findByOfferIdOrderByScoreDesc(100L)).thenReturn(List.of(pendingApp));
            when(applicationMapper.toResponse(any())).thenReturn(pendingResponse);

            List<ApplicationResponse> result = applicationService.getApplicationsByOfferSortedByScore(100L);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Get by learner → returns list")
        void getByLearner_returnsList() {
            when(applicationRepository.findByLearnerId(2L)).thenReturn(List.of(pendingApp));
            when(applicationMapper.toResponse(any())).thenReturn(pendingResponse);

            List<ApplicationResponse> result = applicationService.getApplicationsByLearner(2L);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("hasApplied → true when exists")
        void hasApplied_true() {
            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 2L)).thenReturn(true);
            assertThat(applicationService.hasApplied(100L, 2L)).isTrue();
        }

        @Test
        @DisplayName("hasApplied → false when not exists")
        void hasApplied_false() {
            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 99L)).thenReturn(false);
            assertThat(applicationService.hasApplied(100L, 99L)).isFalse();
        }
    }
}
