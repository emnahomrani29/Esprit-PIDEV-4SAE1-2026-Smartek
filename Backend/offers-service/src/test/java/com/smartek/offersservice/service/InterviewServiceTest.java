package com.smartek.offersservice.service;

import com.smartek.offersservice.dto.InterviewRequest;
import com.smartek.offersservice.dto.InterviewResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Interview;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
 * Unit tests for InterviewService.
 * Covers: creation rules, status transitions, business constraints.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewService Unit Tests")
class InterviewServiceTest {

    @Mock private InterviewRepository interviewRepository;
    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks private InterviewService interviewService;

    private Application acceptedApp;
    private Application pendingApp;
    private Interview scheduledInterview;
    private InterviewRequest validRequest;

    @BeforeEach
    void setUp() {
        acceptedApp = new Application();
        acceptedApp.setId(1L);
        acceptedApp.setOfferId(100L);
        acceptedApp.setLearnerId(2L);
        acceptedApp.setLearnerName("Bob Dupont");
        acceptedApp.setLearnerEmail("bob@test.com");
        acceptedApp.setStatus("ACCEPTED");

        pendingApp = new Application();
        pendingApp.setId(2L);
        pendingApp.setOfferId(100L);
        pendingApp.setLearnerId(3L);
        pendingApp.setLearnerName("Carol Smith");
        pendingApp.setLearnerEmail("carol@test.com");
        pendingApp.setStatus("PENDING");

        scheduledInterview = Interview.builder()
                .id(10L)
                .applicationId(1L)
                .offerId(100L)
                .learnerId(2L)
                .learnerName("Bob Dupont")
                .learnerEmail("bob@test.com")
                .interviewDate(LocalDateTime.now().plusDays(3))
                .location("Salle A")
                .status(Interview.InterviewStatus.SCHEDULED)
                .createdBy(5L)
                .build();

        validRequest = InterviewRequest.builder()
                .applicationId(1L)
                .interviewDate(LocalDateTime.now().plusDays(3))
                .location("Salle A")
                .createdBy(5L)
                .build();
    }

    // ─── CREATE INTERVIEW ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Create Interview")
    class CreateInterviewTests {

        @Test
        @DisplayName("Accepted application → interview created with SCHEDULED status")
        void acceptedApp_interviewCreatedScheduled() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(acceptedApp));
            when(interviewRepository.save(any())).thenReturn(scheduledInterview);

            InterviewResponse result = interviewService.createInterview(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("SCHEDULED");
            verify(interviewRepository).save(any());
        }

        @Test
        @DisplayName("PENDING application → RuntimeException (must be ACCEPTED)")
        void pendingApp_throwsException() {
            validRequest.setApplicationId(2L);
            when(applicationRepository.findById(2L)).thenReturn(Optional.of(pendingApp));

            assertThatThrownBy(() -> interviewService.createInterview(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("acceptée");

            verify(interviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Non-existing application → RuntimeException")
        void nonExistingApp_throwsException() {
            when(applicationRepository.findById(99L)).thenReturn(Optional.empty());
            validRequest.setApplicationId(99L);

            assertThatThrownBy(() -> interviewService.createInterview(validRequest))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Interview data correctly populated from application")
        void interviewData_populatedFromApplication() {
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(acceptedApp));
            when(interviewRepository.save(any())).thenReturn(scheduledInterview);

            interviewService.createInterview(validRequest);

            ArgumentCaptor<Interview> captor = ArgumentCaptor.forClass(Interview.class);
            verify(interviewRepository).save(captor.capture());
            Interview captured = captor.getValue();

            assertThat(captured.getOfferId()).isEqualTo(100L);
            assertThat(captured.getLearnerId()).isEqualTo(2L);
            assertThat(captured.getLearnerName()).isEqualTo("Bob Dupont");
            assertThat(captured.getLearnerEmail()).isEqualTo("bob@test.com");
            assertThat(captured.getStatus()).isEqualTo(Interview.InterviewStatus.SCHEDULED);
        }
    }

    // ─── STATUS TRANSITIONS ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Interview Status Transitions")
    class StatusTransitionTests {

        @ParameterizedTest(name = "Status → {0}")
        @EnumSource(Interview.InterviewStatus.class)
        @DisplayName("All valid status transitions")
        void allValidStatusTransitions(Interview.InterviewStatus status) {
            when(interviewRepository.findById(10L)).thenReturn(Optional.of(scheduledInterview));
            when(interviewRepository.save(any())).thenReturn(scheduledInterview);

            interviewService.updateInterviewStatus(10L, status.name());

            ArgumentCaptor<Interview> captor = ArgumentCaptor.forClass(Interview.class);
            verify(interviewRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Update status of non-existing interview → RuntimeException")
        void updateNonExisting_throwsException() {
            when(interviewRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> interviewService.updateInterviewStatus(99L, "COMPLETED"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─── QUERIES ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Interview Queries")
    class QueryTests {

        @Test
        @DisplayName("Get by offer → returns mapped list")
        void getByOffer_returnsMappedList() {
            when(interviewRepository.findByOfferId(100L)).thenReturn(List.of(scheduledInterview));

            List<InterviewResponse> result = interviewService.getInterviewsByOffer(100L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOfferId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Get by learner → returns mapped list")
        void getByLearner_returnsMappedList() {
            when(interviewRepository.findByLearnerId(2L)).thenReturn(List.of(scheduledInterview));

            List<InterviewResponse> result = interviewService.getInterviewsByLearner(2L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLearnerId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Get all interviews → returns all")
        void getAllInterviews_returnsAll() {
            when(interviewRepository.findAll()).thenReturn(List.of(scheduledInterview));

            List<InterviewResponse> result = interviewService.getAllInterviews();

            assertThat(result).hasSize(1);
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Delete interview → deleteById called")
    void deleteInterview_deleteByIdCalled() {
        assertThatCode(() -> interviewService.deleteInterview(10L)).doesNotThrowAnyException();
        verify(interviewRepository).deleteById(10L);
    }
}
