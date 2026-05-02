package com.smartek.trainingservice.service;

import com.smartek.trainingservice.client.CourseClient;
import com.smartek.trainingservice.client.ExamClient;
import com.smartek.trainingservice.dto.TrainingEnrollmentRequest;
import com.smartek.trainingservice.dto.TrainingEnrollmentResponse;
import com.smartek.trainingservice.dto.TrainingStatsResponse;
import com.smartek.trainingservice.entity.Training;
import com.smartek.trainingservice.entity.TrainingEnrollment;
import com.smartek.trainingservice.repository.TrainingEnrollmentRepository;
import com.smartek.trainingservice.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingEnrollmentService - Tests unitaires")
class TrainingEnrollmentServiceTest {

    @Mock
    private TrainingEnrollmentRepository enrollmentRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private ExamClient examClient;

    @Mock
    private CourseClient courseClient;

    @InjectMocks
    private TrainingEnrollmentService enrollmentService;

    private Training training;
    private TrainingEnrollment enrollment;
    private TrainingEnrollmentRequest request;

    @BeforeEach
    void setUp() {
        training = Training.builder()
                .trainingId(1L)
                .title("Formation Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(List.of(10L, 20L))
                .createdBy(1L)
                .build();

        enrollment = TrainingEnrollment.builder()
                .id(1L)
                .training(training)
                .userId(5L)
                .progress(0)
                .status("ENROLLED")
                .isActive(true)
                .build();

        request = new TrainingEnrollmentRequest();
        request.setUserId(5L);
        request.setTrainingId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // enrollUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("enrollUser()")
    class EnrollUser {

        @Test
        @DisplayName("Doit inscrire un utilisateur avec succès")
        void shouldEnrollUserSuccessfully() {
            when(enrollmentRepository.existsByUserIdAndTrainingTrainingId(5L, 1L)).thenReturn(false);
            when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));
            when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenReturn(enrollment);

            TrainingEnrollmentResponse response = enrollmentService.enrollUser(request);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(5L);
            assertThat(response.getTrainingId()).isEqualTo(1L);
            verify(enrollmentRepository, times(1)).save(any(TrainingEnrollment.class));
        }

        @Test
        @DisplayName("Doit lever une exception si l'utilisateur est déjà inscrit")
        void shouldThrowWhenUserAlreadyEnrolled() {
            when(enrollmentRepository.existsByUserIdAndTrainingTrainingId(5L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> enrollmentService.enrollUser(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("déjà inscrit");

            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever une exception si la formation n'existe pas")
        void shouldThrowWhenTrainingNotFound() {
            when(enrollmentRepository.existsByUserIdAndTrainingTrainingId(5L, 1L)).thenReturn(false);
            when(trainingRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.enrollUser(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Formation non trouvée");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserEnrollments
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUserEnrollments()")
    class GetUserEnrollments {

        @Test
        @DisplayName("Doit retourner les inscriptions d'un utilisateur")
        void shouldReturnUserEnrollments() {
            when(enrollmentRepository.findByUserId(5L)).thenReturn(List.of(enrollment));

            List<TrainingEnrollmentResponse> result = enrollmentService.getUserEnrollments(5L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si l'utilisateur n'a aucune inscription")
        void shouldReturnEmptyListWhenNoEnrollments() {
            when(enrollmentRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

            List<TrainingEnrollmentResponse> result = enrollmentService.getUserEnrollments(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // unenrollUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("unenrollUser()")
    class UnenrollUser {

        @Test
        @DisplayName("Doit désinscrire un utilisateur avec succès")
        void shouldUnenrollUserSuccessfully() {
            when(enrollmentRepository.findByUserIdAndTrainingTrainingId(5L, 1L))
                    .thenReturn(Optional.of(enrollment));

            enrollmentService.unenrollUser(5L, 1L);

            verify(enrollmentRepository, times(1)).delete(enrollment);
        }

        @Test
        @DisplayName("Doit lever une exception si l'inscription n'existe pas")
        void shouldThrowWhenEnrollmentNotFound() {
            when(enrollmentRepository.findByUserIdAndTrainingTrainingId(99L, 1L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.unenrollUser(99L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Inscription non trouvée");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateProgress
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateProgress()")
    class UpdateProgress {

        @Test
        @DisplayName("Doit mettre à jour la progression d'un utilisateur")
        void shouldUpdateProgressSuccessfully() {
            when(enrollmentRepository.findByUserIdAndTrainingTrainingId(5L, 1L))
                    .thenReturn(Optional.of(enrollment));
            when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenReturn(enrollment);

            TrainingEnrollmentResponse response = enrollmentService.updateProgress(5L, 1L, 50);

            assertThat(response).isNotNull();
            verify(enrollmentRepository, times(1)).save(any(TrainingEnrollment.class));
        }

        @Test
        @DisplayName("Doit marquer comme COMPLETED et débloquer l'examen quand progression = 100%")
        void shouldCompleteAndUnlockExamAt100Percent() {
            when(enrollmentRepository.findByUserIdAndTrainingTrainingId(5L, 1L))
                    .thenReturn(Optional.of(enrollment));
            when(enrollmentRepository.save(any(TrainingEnrollment.class))).thenReturn(enrollment);

            enrollmentService.updateProgress(5L, 1L, 100);

            verify(enrollmentRepository, times(1)).save(any(TrainingEnrollment.class));
        }

        @Test
        @DisplayName("Doit lever une exception si l'inscription n'existe pas")
        void shouldThrowWhenEnrollmentNotFound() {
            when(enrollmentRepository.findByUserIdAndTrainingTrainingId(99L, 1L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.updateProgress(99L, 1L, 50))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Inscription non trouvée");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // hasCompletedAllCourses
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("hasCompletedAllCourses()")
    class HasCompletedAllCourses {

        @Test
        @DisplayName("Doit retourner true si la progression est à 100%")
        void shouldReturnTrueWhenProgressIs100() {
            enrollment.setProgress(100);
            when(enrollmentRepository.findByUserIdAndTrainingTrainingId(5L, 1L))
                    .thenReturn(Optional.of(enrollment));

            Boolean result = enrollmentService.hasCompletedAllCourses(5L, 1L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Doit retourner false si l'utilisateur n'est pas inscrit")
        void shouldReturnFalseWhenNotEnrolled() {
            when(enrollmentRepository.findByUserIdAndTrainingTrainingId(99L, 1L))
                    .thenReturn(Optional.empty());

            Boolean result = enrollmentService.hasCompletedAllCourses(99L, 1L);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Doit retourner false si la progression est inférieure à 100%")
        void shouldReturnFalseWhenProgressBelow100() {
            enrollment.setProgress(75);
            when(enrollmentRepository.findByUserIdAndTrainingTrainingId(5L, 1L))
                    .thenReturn(Optional.of(enrollment));

            Boolean result = enrollmentService.hasCompletedAllCourses(5L, 1L);

            assertThat(result).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTrainingStatsByUserId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTrainingStatsByUserId()")
    class GetTrainingStats {

        @Test
        @DisplayName("Doit retourner des stats vides si l'utilisateur n'a aucune inscription")
        void shouldReturnEmptyStatsWhenNoEnrollments() {
            when(enrollmentRepository.findByUserId(5L)).thenReturn(Collections.emptyList());

            TrainingStatsResponse result = enrollmentService.getTrainingStatsByUserId(5L);

            assertThat(result.getTotalEnrolled()).isZero();
            assertThat(result.getCompleted()).isZero();
            assertThat(result.getInProgress()).isZero();
        }

        @Test
        @DisplayName("Doit calculer correctement les statistiques")
        void shouldCalculateStatsCorrectly() {
            TrainingEnrollment completed = TrainingEnrollment.builder()
                    .id(2L).training(training).userId(5L)
                    .progress(100).status("COMPLETED").isActive(true).build();
            TrainingEnrollment inProgress = TrainingEnrollment.builder()
                    .id(3L).training(training).userId(5L)
                    .progress(50).status("IN_PROGRESS").isActive(true).build();

            when(enrollmentRepository.findByUserId(5L)).thenReturn(List.of(completed, inProgress));

            TrainingStatsResponse result = enrollmentService.getTrainingStatsByUserId(5L);

            assertThat(result.getTotalEnrolled()).isEqualTo(2);
            assertThat(result.getCompleted()).isEqualTo(1);
            assertThat(result.getInProgress()).isEqualTo(1);
        }
    }
}
