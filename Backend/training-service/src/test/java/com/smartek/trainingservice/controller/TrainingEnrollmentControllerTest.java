package com.smartek.trainingservice.controller;

import com.smartek.trainingservice.dto.TrainerTrainingAnalyticsResponse;
import com.smartek.trainingservice.dto.TrainingEnrollmentRequest;
import com.smartek.trainingservice.dto.TrainingEnrollmentResponse;
import com.smartek.trainingservice.dto.TrainingResponse;
import com.smartek.trainingservice.dto.TrainingStatsResponse;
import com.smartek.trainingservice.service.TrainingEnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour TrainingEnrollmentController.
 * Utilise MockitoExtension pour éviter le chargement du contexte Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingEnrollmentController - Tests unitaires")
class TrainingEnrollmentControllerTest {

    // ─── Mocks ───────────────────────────────────────────────────────────────

    @Mock
    private TrainingEnrollmentService enrollmentService;

    @InjectMocks
    private TrainingEnrollmentController enrollmentController;

    // ─── Données de test ─────────────────────────────────────────────────────

    private TrainingEnrollmentRequest enrollmentRequest;
    private TrainingEnrollmentResponse enrollmentResponse;
    private TrainingResponse trainingResponse;

    @BeforeEach
    void setUp() {
        // Initialisation d'une requête d'inscription
        enrollmentRequest = new TrainingEnrollmentRequest();
        enrollmentRequest.setTrainingId(1L);
        enrollmentRequest.setUserId(10L);

        // Initialisation d'une réponse d'inscription
        enrollmentResponse = new TrainingEnrollmentResponse();
        enrollmentResponse.setId(100L);
        enrollmentResponse.setTrainingId(1L);
        enrollmentResponse.setTrainingTitle("Formation Spring Boot");
        enrollmentResponse.setUserId(10L);
        enrollmentResponse.setEnrolledAt(LocalDateTime.now());
        enrollmentResponse.setIsActive(true);
        enrollmentResponse.setProgress(0);
        enrollmentResponse.setStatus("ACTIVE");

        // Initialisation d'une réponse de formation
        trainingResponse = TrainingResponse.builder()
                .trainingId(1L)
                .title("Formation Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/trainings/enrollments - Inscription à une formation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/trainings/enrollments - Inscription")
    class EnrollUser {

        @Test
        @DisplayName("Doit inscrire un utilisateur avec succès → 201 CREATED")
        void shouldEnrollUserSuccessfully() {
            // Arrange
            when(enrollmentService.enrollUser(any(TrainingEnrollmentRequest.class)))
                    .thenReturn(enrollmentResponse);

            // Act
            ResponseEntity<TrainingEnrollmentResponse> response =
                    enrollmentController.enrollUser(enrollmentRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTrainingId()).isEqualTo(1L);
            assertThat(response.getBody().getUserId()).isEqualTo(10L);
            verify(enrollmentService, times(1)).enrollUser(any(TrainingEnrollmentRequest.class));
        }

        @Test
        @DisplayName("Doit retourner 400 BAD REQUEST si l'inscription échoue")
        void shouldReturn400WhenEnrollmentFails() {
            // Arrange : le service lève une exception (déjà inscrit, formation inexistante, etc.)
            when(enrollmentService.enrollUser(any(TrainingEnrollmentRequest.class)))
                    .thenThrow(new RuntimeException("L'utilisateur est déjà inscrit à cette formation"));

            // Act
            ResponseEntity<TrainingEnrollmentResponse> response =
                    enrollmentController.enrollUser(enrollmentRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/enrollments/user/{userId} - Inscriptions d'un utilisateur
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/enrollments/user/{userId} - Inscriptions d'un utilisateur")
    class GetUserEnrollments {

        @Test
        @DisplayName("Doit retourner les inscriptions de l'utilisateur → 200 OK")
        void shouldReturnUserEnrollments() {
            // Arrange
            when(enrollmentService.getUserEnrollments(10L)).thenReturn(List.of(enrollmentResponse));

            // Act
            ResponseEntity<List<TrainingEnrollmentResponse>> response =
                    enrollmentController.getUserEnrollments(10L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getUserId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si l'utilisateur n'a aucune inscription → 200 OK")
        void shouldReturnEmptyListWhenNoEnrollments() {
            // Arrange
            when(enrollmentService.getUserEnrollments(99L)).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<TrainingEnrollmentResponse>> response =
                    enrollmentController.getUserEnrollments(99L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/enrollments/user/{userId}/details - Formations avec détails
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/enrollments/user/{userId}/details - Formations avec détails")
    class GetUserTrainingsWithDetails {

        @Test
        @DisplayName("Doit retourner les formations détaillées de l'utilisateur → 200 OK")
        void shouldReturnUserTrainingsWithDetails() {
            // Arrange
            when(enrollmentService.getUserTrainingsWithDetails(10L)).thenReturn(List.of(trainingResponse));

            // Act
            ResponseEntity<List<TrainingResponse>> response =
                    enrollmentController.getUserTrainingsWithDetails(10L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTitle()).isEqualTo("Formation Spring Boot");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune formation → 200 OK")
        void shouldReturnEmptyListWhenNoTrainings() {
            // Arrange
            when(enrollmentService.getUserTrainingsWithDetails(99L)).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<TrainingResponse>> response =
                    enrollmentController.getUserTrainingsWithDetails(99L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/enrollments/training/{trainingId} - Inscriptions d'une formation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/enrollments/training/{trainingId} - Inscriptions d'une formation")
    class GetTrainingEnrollments {

        @Test
        @DisplayName("Doit retourner les inscriptions d'une formation → 200 OK")
        void shouldReturnTrainingEnrollments() {
            // Arrange
            when(enrollmentService.getTrainingEnrollments(1L)).thenReturn(List.of(enrollmentResponse));

            // Act
            ResponseEntity<List<TrainingEnrollmentResponse>> response =
                    enrollmentController.getTrainingEnrollments(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTrainingId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune inscription → 200 OK")
        void shouldReturnEmptyListWhenNoEnrollments() {
            // Arrange
            when(enrollmentService.getTrainingEnrollments(99L)).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<TrainingEnrollmentResponse>> response =
                    enrollmentController.getTrainingEnrollments(99L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/trainings/enrollments/user/{userId}/training/{trainingId} - Désinscription
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/trainings/enrollments/user/{userId}/training/{trainingId} - Désinscription")
    class UnenrollUser {

        @Test
        @DisplayName("Doit désinscrire un utilisateur avec succès → 204 NO CONTENT")
        void shouldUnenrollUserSuccessfully() {
            // Arrange : le service ne lève pas d'exception
            doNothing().when(enrollmentService).unenrollUser(10L, 1L);

            // Act
            ResponseEntity<Void> response = enrollmentController.unenrollUser(10L, 1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(enrollmentService, times(1)).unenrollUser(10L, 1L);
        }

        @Test
        @DisplayName("Doit retourner 404 NOT FOUND si l'inscription n'existe pas")
        void shouldReturn404WhenEnrollmentNotFound() {
            // Arrange : le service lève une exception
            doThrow(new RuntimeException("Inscription non trouvée"))
                    .when(enrollmentService).unenrollUser(99L, 1L);

            // Act
            ResponseEntity<Void> response = enrollmentController.unenrollUser(99L, 1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/enrollments/check-completion - Vérification de complétion
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/enrollments/check-completion - Vérification de complétion")
    class CheckCompletion {

        @Test
        @DisplayName("Doit retourner true si l'utilisateur a complété tous les cours → 200 OK")
        void shouldReturnTrueWhenAllCoursesCompleted() {
            // Arrange
            when(enrollmentService.hasCompletedAllCourses(10L, 1L)).thenReturn(true);

            // Act
            ResponseEntity<Boolean> response = enrollmentController.hasCompletedAllCourses(10L, 1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
        }

        @Test
        @DisplayName("Doit retourner false si l'utilisateur n'a pas complété tous les cours → 200 OK")
        void shouldReturnFalseWhenCoursesNotCompleted() {
            // Arrange
            when(enrollmentService.hasCompletedAllCourses(10L, 1L)).thenReturn(false);

            // Act
            ResponseEntity<Boolean> response = enrollmentController.hasCompletedAllCourses(10L, 1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/enrollments/stats/user/{userId} - Statistiques utilisateur
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/enrollments/stats/user/{userId} - Statistiques")
    class GetUserTrainingStats {

        @Test
        @DisplayName("Doit retourner les statistiques de formation de l'utilisateur → 200 OK")
        void shouldReturnUserTrainingStats() {
            // Arrange : construction d'une réponse de statistiques
            TrainingStatsResponse stats = TrainingStatsResponse.builder()
                    .userId(10L)
                    .totalEnrolled(5)
                    .inProgress(2)
                    .completed(3)
                    .averageProgress(75.0)
                    .statusBreakdown(Map.of("ACTIVE", 2, "COMPLETED", 3))
                    .build();
            when(enrollmentService.getTrainingStatsByUserId(10L)).thenReturn(stats);

            // Act
            ResponseEntity<TrainingStatsResponse> response =
                    enrollmentController.getUserTrainingStats(10L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUserId()).isEqualTo(10L);
            assertThat(response.getBody().getTotalEnrolled()).isEqualTo(5);
            assertThat(response.getBody().getCompleted()).isEqualTo(3);
        }

        @Test
        @DisplayName("Doit retourner 500 INTERNAL SERVER ERROR si le service échoue")
        void shouldReturn500WhenServiceFails() {
            // Arrange
            when(enrollmentService.getTrainingStatsByUserId(99L))
                    .thenThrow(new RuntimeException("Erreur interne"));

            // Act
            ResponseEntity<TrainingStatsResponse> response =
                    enrollmentController.getUserTrainingStats(99L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/enrollments/trainer/{trainerId}/analytics - Analytics formateur
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/enrollments/trainer/{trainerId}/analytics - Analytics formateur")
    class GetTrainerAnalytics {

        @Test
        @DisplayName("Doit retourner les analytics du formateur → 200 OK")
        void shouldReturnTrainerAnalytics() {
            // Arrange
            TrainerTrainingAnalyticsResponse analytics =
                    new TrainerTrainingAnalyticsResponse(1L, "Formation Spring Boot", 20, 15, 5);
            when(enrollmentService.getTrainerTrainingAnalytics(1L)).thenReturn(List.of(analytics));

            // Act
            ResponseEntity<List<TrainerTrainingAnalyticsResponse>> response =
                    enrollmentController.getTrainerAnalytics(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTrainingTitle()).isEqualTo("Formation Spring Boot");
            assertThat(response.getBody().get(0).getTotalEnrollments()).isEqualTo(20);
        }

        @Test
        @DisplayName("Doit retourner 500 INTERNAL SERVER ERROR si le service échoue")
        void shouldReturn500WhenAnalyticsFail() {
            // Arrange
            when(enrollmentService.getTrainerTrainingAnalytics(99L))
                    .thenThrow(new RuntimeException("Erreur lors du calcul des analytics"));

            // Act
            ResponseEntity<List<TrainerTrainingAnalyticsResponse>> response =
                    enrollmentController.getTrainerAnalytics(99L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/enrollments/health - Vérification de santé
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/trainings/enrollments/health - Doit retourner 200 OK")
    void healthEndpointShouldReturnOk() {
        // Act
        ResponseEntity<String> response = enrollmentController.health();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Training Service is running");
    }
}
