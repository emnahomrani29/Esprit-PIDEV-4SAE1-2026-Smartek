package com.smartek.trainingservice.service;

import com.smartek.trainingservice.dto.TrainingAnalyticsResponse;
import com.smartek.trainingservice.entity.Training;
import com.smartek.trainingservice.entity.TrainingEnrollment;
import com.smartek.trainingservice.repository.TrainingEnrollmentRepository;
import com.smartek.trainingservice.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService - Tests unitaires")
class AnalyticsServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainingEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Training training;

    @BeforeEach
    void setUp() {
        training = Training.builder()
                .trainingId(1L)
                .title("Formation DevOps")
                .category("DevOps")
                .level("Avancé")
                .duration(LocalDate.now().plusMonths(2))
                .createdBy(10L)
                .build();
    }

    @Test
    @DisplayName("Doit retourner une liste vide si le trainer n'a aucune formation")
    void shouldReturnEmptyWhenNoTrainings() {
        when(trainingRepository.findByCreatedBy(10L)).thenReturn(Collections.emptyList());

        List<TrainingAnalyticsResponse> result = analyticsService.getTrainerTrainingAnalytics(10L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Doit calculer les analytics pour une formation avec des inscriptions")
    void shouldCalculateAnalyticsWithEnrollments() {
        TrainingEnrollment enrolled = TrainingEnrollment.builder()
                .id(1L).training(training).userId(5L)
                .progress(50).status("IN_PROGRESS").isActive(true).build();
        TrainingEnrollment completed = TrainingEnrollment.builder()
                .id(2L).training(training).userId(6L)
                .progress(100).status("COMPLETED").isActive(true)
                .examScore(85).examPassed(true).build();

        when(trainingRepository.findByCreatedBy(10L)).thenReturn(List.of(training));
        when(enrollmentRepository.findByTrainingTrainingId(1L)).thenReturn(List.of(enrolled, completed));

        List<TrainingAnalyticsResponse> result = analyticsService.getTrainerTrainingAnalytics(10L);

        assertThat(result).hasSize(1);
        TrainingAnalyticsResponse analytics = result.get(0);
        assertThat(analytics.getTrainingId()).isEqualTo(1L);
        assertThat(analytics.getTotalEnrollments()).isEqualTo(2);
        assertThat(analytics.getCompletedEnrollments()).isEqualTo(1);
        assertThat(analytics.getAverageProgress()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Doit retourner 0 inscriptions si aucun learner n'est inscrit")
    void shouldReturnZeroEnrollmentsWhenNoLearners() {
        when(trainingRepository.findByCreatedBy(10L)).thenReturn(List.of(training));
        when(enrollmentRepository.findByTrainingTrainingId(1L)).thenReturn(Collections.emptyList());

        List<TrainingAnalyticsResponse> result = analyticsService.getTrainerTrainingAnalytics(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalEnrollments()).isZero();
        assertThat(result.get(0).getAverageProgress()).isZero();
    }
}
