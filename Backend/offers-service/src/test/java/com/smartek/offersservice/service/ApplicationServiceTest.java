package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.ApplicationRequest;
import com.smartek.offersservice.dto.ApplicationResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationService.
 * Covers: apply, duplicate prevention, status transitions, queries.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService Unit Tests")
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks private ApplicationService applicationService;

    private Application pendingApp;
    private ApplicationRequest validRequest;

    @BeforeEach
    void setUp() {
        pendingApp = new Application();
        pendingApp.setId(1L);
        pendingApp.setOfferId(100L);
        pendingApp.setLearnerId(2L);
        pendingApp.setLearnerName("Alice Martin");
        pendingApp.setLearnerEmail("alice@test.com");
        pendingApp.setCoverLetter("Je suis motivée...");
        pendingApp.setStatus("PENDING");
        pendingApp.setAppliedAt(LocalDateTime.now());

        validRequest = new ApplicationRequest();
        validRequest.setOfferId(100L);
        validRequest.setLearnerId(2L);
        validRequest.setLearnerName("Alice Martin");
        validRequest.setLearnerEmail("alice@test.com");
        validRequest.setCoverLetter("Je suis motivée...");
    }

    // ─── APPLY TO OFFER ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Apply to Offer")
    class ApplyTests {

        @Test
        @DisplayName("First application → saved with PENDING status")
        void firstApplication_savedWithPendingStatus() {
            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 2L)).thenReturn(false);
            when(applicationRepository.save(any())).thenReturn(pendingApp);

            ApplicationResponse result = applicationService.applyToOffer(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("PENDING");
            verify(applicationRepository).save(any());
        }

        @Test
        @DisplayName("Duplicate application → RuntimeException thrown")
        void duplicateApplication_throwsException() {
            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 2L)).thenReturn(true);

            assertThatThrownBy(() -> applicationService.applyToOffer(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("déjà postulé");

            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Application data correctly mapped to entity")
        void applicationData_correctlyMapped() {
            when(applicationRepository.existsByOfferIdAndLearnerId(any(), any())).thenReturn(false);
            when(applicationRepository.save(any())).thenReturn(pendingApp);

            applicationService.applyToOffer(validRequest);

            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            verify(applicationRepository).save(captor.capture());
            Application captured = captor.getValue();

            assertThat(captured.getOfferId()).isEqualTo(100L);
            assertThat(captured.getLearnerId()).isEqualTo(2L);
            assertThat(captured.getLearnerName()).isEqualTo("Alice Martin");
            assertThat(captured.getLearnerEmail()).isEqualTo("alice@test.com");
            assertThat(captured.getStatus()).isEqualTo("PENDING");
        }
    }

    // ─── STATUS TRANSITIONS ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Application Status Transitions")
    class StatusTransitionTests {

        @ParameterizedTest(name = "Status ''{0}'' → updated correctly")
        @ValueSource(strings = {"ACCEPTED", "REJECTED", "PENDING"})
        @DisplayName("Valid status transitions")
        void validStatusTransitions(String newStatus) {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApp));
            when(applicationRepository.save(any())).thenReturn(pendingApp);

            applicationService.updateApplicationStatus(1L, newStatus);

            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            verify(applicationRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(newStatus);
        }

        @Test
        @DisplayName("Update status of non-existing application → RuntimeException")
        void updateNonExisting_throwsException() {
            when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(99L, "ACCEPTED"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─── QUERIES ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Application Queries")
    class QueryTests {

        @Test
        @DisplayName("Get applications by offer → returns mapped list")
        void getByOffer_returnsMappedList() {
            when(applicationRepository.findByOfferId(100L)).thenReturn(List.of(pendingApp));

            List<ApplicationResponse> result = applicationService.getApplicationsByOffer(100L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOfferId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Get applications by learner → returns mapped list")
        void getByLearner_returnsMappedList() {
            when(applicationRepository.findByLearnerId(2L)).thenReturn(List.of(pendingApp));

            List<ApplicationResponse> result = applicationService.getApplicationsByLearner(2L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLearnerId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Has applied — true when exists")
        void hasApplied_true_whenExists() {
            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 2L)).thenReturn(true);

            assertThat(applicationService.hasApplied(100L, 2L)).isTrue();
        }

        @Test
        @DisplayName("Has applied — false when not exists")
        void hasApplied_false_whenNotExists() {
            when(applicationRepository.existsByOfferIdAndLearnerId(100L, 99L)).thenReturn(false);

            assertThat(applicationService.hasApplied(100L, 99L)).isFalse();
        }

        @Test
        @DisplayName("Get applications by offer with no results → empty list")
        void getByOffer_noResults_emptyList() {
            when(applicationRepository.findByOfferId(999L)).thenReturn(List.of());

            List<ApplicationResponse> result = applicationService.getApplicationsByOffer(999L);

            assertThat(result).isEmpty();
        }
    }
}
