package com.smartek.trainingservice.service;

import com.smartek.trainingservice.client.CourseClient;
import com.smartek.trainingservice.client.ExamClient;
import com.smartek.trainingservice.dto.PerformanceStatsResponse;
import com.smartek.trainingservice.entity.Training;
import com.smartek.trainingservice.entity.TrainingEnrollment;
import com.smartek.trainingservice.repository.TrainingEnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PerformanceService - Tests unitaires")
class PerformanceServiceTest {

    @Mock
    private TrainingEnrollmentRepository trainingEnrollmentRepository;

    @Mock
    private CourseClient courseClient;

    @Mock
    private ExamClient examClient;

    @InjectMocks
    private PerformanceService performanceService;

    private Training training;
    private TrainingEnrollment completedEnrollment;
    private TrainingEnrollment inProgressEnrollment;

    @BeforeEach
    void setUp() {
        training = Training.builder()
                .trainingId(1L).title("Formation Spring Boot")
                .category("Backend").level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(new ArrayList<>(List.of(10L, 20L)))
                .createdBy(1L).build();

        completedEnrollment = TrainingEnrollment.builder()
                .id(1L).training(training).userId(5L)
                .progress(100).status("COMPLETED").isActive(true).build();

        inProgressEnrollment = TrainingEnrollment.builder()
                .id(2L).training(training).userId(5L)
                .progress(50).status("IN_PROGRESS").isActive(true).build();
    }

    @Test
    @DisplayName("Doit retourner des stats vides si aucune inscription")
    void shouldReturnEmptyStatsWhenNoEnrollments() {
        when(trainingEnrollmentRepository.findByUserId(5L)).thenReturn(Collections.emptyList());
        when(examClient.getUserExamStats(5L)).thenThrow(new RuntimeException("feign"));

        PerformanceStatsResponse result = performanceService.getUserPerformanceStats(5L);

        assertThat(result).isNotNull();
        assertThat(result.getCourses().getTotalEnrolled()).isZero();
        assertThat(result.getTrainings().getTotalEnrolled()).isZero();
        assertThat(result.getExams().getTotalAvailable()).isZero();
    }

    @Test
    @DisplayName("Doit calculer les stats de formation correctement")
    void shouldCalculateTrainingStats() {
        when(trainingEnrollmentRepository.findByUserId(5L))
                .thenReturn(List.of(completedEnrollment, inProgressEnrollment));
        when(courseClient.isCourseCompleted(anyLong(), eq(5L))).thenReturn(false);
        when(examClient.getUserExamStats(5L)).thenThrow(new RuntimeException("feign"));

        PerformanceStatsResponse result = performanceService.getUserPerformanceStats(5L);

        assertThat(result.getTrainings().getTotalEnrolled()).isEqualTo(2);
        assertThat(result.getTrainings().getCompleted()).isEqualTo(1);
        assertThat(result.getTrainings().getInProgress()).isEqualTo(1);
        assertThat(result.getTrainings().getAverageProgress()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Doit calculer les stats de cours correctement")
    void shouldCalculateCourseStats() {
        when(trainingEnrollmentRepository.findByUserId(5L))
                .thenReturn(List.of(completedEnrollment));
        when(courseClient.isCourseCompleted(10L, 5L)).thenReturn(true);
        when(courseClient.isCourseCompleted(20L, 5L)).thenReturn(false);
        when(examClient.getUserExamStats(5L)).thenThrow(new RuntimeException("feign"));

        PerformanceStatsResponse result = performanceService.getUserPerformanceStats(5L);

        assertThat(result.getCourses().getTotalEnrolled()).isEqualTo(2);
        assertThat(result.getCourses().getCompleted()).isEqualTo(1);
        assertThat(result.getCourses().getInProgress()).isEqualTo(1);
        assertThat(result.getCourses().getCompletionRate()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Doit retourner des stats d'examen vides si le client échoue")
    void shouldReturnEmptyExamStatsOnClientError() {
        when(trainingEnrollmentRepository.findByUserId(5L)).thenReturn(Collections.emptyList());
        when(examClient.getUserExamStats(5L)).thenThrow(new RuntimeException("Service unavailable"));

        PerformanceStatsResponse result = performanceService.getUserPerformanceStats(5L);

        assertThat(result.getExams().getTotalAvailable()).isZero();
        assertThat(result.getExams().getAttempted()).isZero();
        assertThat(result.getExams().getSuccessRate()).isZero();
    }

    @Test
    @DisplayName("Doit calculer les stats d'examen depuis le client")
    void shouldCalculateExamStatsFromClient() {
        when(trainingEnrollmentRepository.findByUserId(5L)).thenReturn(Collections.emptyList());
        when(examClient.getUserExamStats(5L)).thenReturn(Map.of(
                "totalAvailable", 10,
                "attempted", 5,
                "passed", 4,
                "failed", 1,
                "averageScore", 78.5,
                "successRate", 80.0,
                "totalAttempts", 6
        ));

        PerformanceStatsResponse result = performanceService.getUserPerformanceStats(5L);

        assertThat(result.getExams().getTotalAvailable()).isEqualTo(10);
        assertThat(result.getExams().getPassed()).isEqualTo(4);
        assertThat(result.getExams().getSuccessRate()).isEqualTo(80.0);
    }
}
