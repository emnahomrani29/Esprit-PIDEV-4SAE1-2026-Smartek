package com.smartek.trainingservice.controller;

import com.smartek.trainingservice.dto.TrainingRequest;
import com.smartek.trainingservice.dto.TrainingResponse;
import com.smartek.trainingservice.service.TrainingService;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour TrainingController.
 * Utilise MockitoExtension pour éviter le chargement du contexte Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingController - Tests unitaires")
class TrainingControllerTest {

    // ─── Mocks ───────────────────────────────────────────────────────────────

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController trainingController;

    // ─── Données de test ─────────────────────────────────────────────────────

    private TrainingRequest validRequest;
    private TrainingResponse sampleResponse;

    @BeforeEach
    void setUp() {
        // Initialisation d'une requête valide
        validRequest = TrainingRequest.builder()
                .title("Formation Spring Boot")
                .description("Microservices avec Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(List.of(1L, 2L))
                .createdBy(1L)
                .build();

        // Initialisation d'une réponse de formation
        sampleResponse = TrainingResponse.builder()
                .trainingId(1L)
                .title("Formation Spring Boot")
                .description("Microservices avec Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(List.of(1L, 2L))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/trainings - Création de formation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/trainings - Création de formation")
    class CreateTraining {

        @Test
        @DisplayName("Doit créer une formation avec succès → 201 CREATED")
        void shouldCreateTrainingSuccessfully() {
            // Arrange : le service retourne la formation créée
            when(trainingService.createTraining(any(TrainingRequest.class))).thenReturn(sampleResponse);

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.createTraining(validRequest);

            // Assert : statut 201 et corps non nul
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Formation Spring Boot");
            assertThat(response.getBody().getCategory()).isEqualTo("Backend");
            verify(trainingService, times(1)).createTraining(any(TrainingRequest.class));
        }

        @Test
        @DisplayName("Doit retourner 400 BAD REQUEST si le service lève une exception")
        void shouldReturn400WhenServiceThrowsException() {
            // Arrange : le service lève une exception (titre déjà existant, etc.)
            when(trainingService.createTraining(any(TrainingRequest.class)))
                    .thenThrow(new RuntimeException("La formation existe déjà"));

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.createTraining(validRequest);

            // Assert : statut 400 avec message d'erreur
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("La formation existe déjà");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings - Liste de toutes les formations
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings - Liste des formations")
    class GetAllTrainings {

        @Test
        @DisplayName("Doit retourner la liste de toutes les formations → 200 OK")
        void shouldReturnAllTrainings() {
            // Arrange
            when(trainingService.getAllTrainings()).thenReturn(List.of(sampleResponse));

            // Act
            ResponseEntity<List<TrainingResponse>> response = trainingController.getAllTrainings();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTitle()).isEqualTo("Formation Spring Boot");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune formation n'existe → 200 OK")
        void shouldReturnEmptyListWhenNoTrainings() {
            // Arrange
            when(trainingService.getAllTrainings()).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<TrainingResponse>> response = trainingController.getAllTrainings();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/{id} - Récupération par ID
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/{id} - Récupération par ID")
    class GetTrainingById {

        @Test
        @DisplayName("Doit retourner la formation correspondant à l'ID → 200 OK")
        void shouldReturnTrainingById() {
            // Arrange
            when(trainingService.getTrainingById(1L)).thenReturn(sampleResponse);

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.getTrainingById(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTrainingId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit retourner 404 NOT FOUND si la formation n'existe pas")
        void shouldReturn404WhenTrainingNotFound() {
            // Arrange : le service lève une exception pour ID inexistant
            when(trainingService.getTrainingById(99L))
                    .thenThrow(new RuntimeException("Formation non trouvée avec l'ID: 99"));

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.getTrainingById(99L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("Formation non trouvée");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/category/{category} - Filtrage par catégorie
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/category/{category} - Filtrage par catégorie")
    class GetTrainingsByCategory {

        @Test
        @DisplayName("Doit retourner les formations de la catégorie donnée → 200 OK")
        void shouldReturnTrainingsByCategory() {
            // Arrange
            when(trainingService.getTrainingsByCategory("Backend")).thenReturn(List.of(sampleResponse));

            // Act
            ResponseEntity<List<TrainingResponse>> response = trainingController.getTrainingsByCategory("Backend");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getCategory()).isEqualTo("Backend");
        }

        @Test
        @DisplayName("Doit retourner une liste vide pour une catégorie inconnue → 200 OK")
        void shouldReturnEmptyListForUnknownCategory() {
            // Arrange
            when(trainingService.getTrainingsByCategory("Inconnu")).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<TrainingResponse>> response = trainingController.getTrainingsByCategory("Inconnu");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/level/{level} - Filtrage par niveau
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/level/{level} - Filtrage par niveau")
    class GetTrainingsByLevel {

        @Test
        @DisplayName("Doit retourner les formations du niveau donné → 200 OK")
        void shouldReturnTrainingsByLevel() {
            // Arrange
            when(trainingService.getTrainingsByLevel("Intermédiaire")).thenReturn(List.of(sampleResponse));

            // Act
            ResponseEntity<List<TrainingResponse>> response = trainingController.getTrainingsByLevel("Intermédiaire");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getLevel()).isEqualTo("Intermédiaire");
        }

        @Test
        @DisplayName("Doit retourner une liste vide pour un niveau inconnu → 200 OK")
        void shouldReturnEmptyListForUnknownLevel() {
            // Arrange
            when(trainingService.getTrainingsByLevel("Expert")).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<TrainingResponse>> response = trainingController.getTrainingsByLevel("Expert");

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/by-course/{courseId} - Formations par cours
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/by-course/{courseId} - Formations par cours")
    class GetTrainingsByCourse {

        @Test
        @DisplayName("Doit retourner les formations contenant le cours donné → 200 OK")
        void shouldReturnTrainingsByCourseId() {
            // Arrange
            when(trainingService.getTrainingsByCourseId(1L)).thenReturn(List.of(sampleResponse));

            // Act
            ResponseEntity<List<TrainingResponse>> response = trainingController.getTrainingsByCourse(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            verify(trainingService, times(1)).getTrainingsByCourseId(1L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/trainings/{id} - Mise à jour de formation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/trainings/{id} - Mise à jour de formation")
    class UpdateTraining {

        @Test
        @DisplayName("Doit mettre à jour une formation existante → 200 OK")
        void shouldUpdateTrainingSuccessfully() {
            // Arrange
            when(trainingService.updateTraining(eq(1L), any(TrainingRequest.class))).thenReturn(sampleResponse);

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.updateTraining(1L, validRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTrainingId()).isEqualTo(1L);
            verify(trainingService, times(1)).updateTraining(eq(1L), any(TrainingRequest.class));
        }

        @Test
        @DisplayName("Doit retourner 400 BAD REQUEST si la formation à mettre à jour n'existe pas")
        void shouldReturn400WhenTrainingNotFound() {
            // Arrange
            when(trainingService.updateTraining(eq(99L), any(TrainingRequest.class)))
                    .thenThrow(new RuntimeException("Formation non trouvée avec l'ID: 99"));

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.updateTraining(99L, validRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).contains("Formation non trouvée");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/trainings/{trainingId}/courses/{courseId} - Ajout de cours
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/trainings/{trainingId}/courses/{courseId} - Ajout de cours")
    class AddCourseToTraining {

        @Test
        @DisplayName("Doit ajouter un cours à une formation → 200 OK")
        void shouldAddCourseToTraining() {
            // Arrange
            when(trainingService.addCourseToTraining(1L, 3L)).thenReturn(sampleResponse);

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.addCourseToTraining(1L, 3L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(trainingService, times(1)).addCourseToTraining(1L, 3L);
        }

        @Test
        @DisplayName("Doit retourner 400 BAD REQUEST si la formation ou le cours n'existe pas")
        void shouldReturn400WhenTrainingOrCourseNotFound() {
            // Arrange
            when(trainingService.addCourseToTraining(99L, 3L))
                    .thenThrow(new RuntimeException("Formation non trouvée avec l'ID: 99"));

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.addCourseToTraining(99L, 3L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/trainings/{trainingId}/courses/{courseId} - Suppression de cours
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/trainings/{trainingId}/courses/{courseId} - Suppression de cours")
    class RemoveCourseFromTraining {

        @Test
        @DisplayName("Doit supprimer un cours d'une formation → 200 OK")
        void shouldRemoveCourseFromTraining() {
            // Arrange
            when(trainingService.removeCourseFromTraining(1L, 2L)).thenReturn(sampleResponse);

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.removeCourseFromTraining(1L, 2L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(trainingService, times(1)).removeCourseFromTraining(1L, 2L);
        }

        @Test
        @DisplayName("Doit retourner 400 BAD REQUEST si la formation n'existe pas")
        void shouldReturn400WhenTrainingNotFound() {
            // Arrange
            when(trainingService.removeCourseFromTraining(99L, 2L))
                    .thenThrow(new RuntimeException("Formation non trouvée avec l'ID: 99"));

            // Act
            ResponseEntity<TrainingResponse> response = trainingController.removeCourseFromTraining(99L, 2L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/trainings/{id} - Suppression de formation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/trainings/{id} - Suppression de formation")
    class DeleteTraining {

        @Test
        @DisplayName("Doit supprimer une formation existante → 204 NO CONTENT")
        void shouldDeleteTrainingSuccessfully() {
            // Arrange : le service retourne un CompletableFuture<Void> complété normalement
            when(trainingService.deleteTraining(1L))
                    .thenReturn(CompletableFuture.completedFuture(null));

            // Act
            ResponseEntity<Void> response = trainingController.deleteTraining(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(trainingService, times(1)).deleteTraining(1L);
        }

        @Test
        @DisplayName("Doit retourner 500 INTERNAL SERVER ERROR si la formation n'existe pas")
        void shouldReturn500WhenTrainingNotFound() {
            // Arrange : le service lève une exception synchrone (avant le CompletableFuture)
            when(trainingService.deleteTraining(99L))
                    .thenThrow(new RuntimeException("Formation non trouvée avec l'ID: 99"));

            // Act
            ResponseEntity<Void> response = trainingController.deleteTraining(99L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/health - Vérification de santé
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/trainings/health - Doit retourner 200 OK avec message de santé")
    void healthEndpointShouldReturnOk() {
        // Act
        ResponseEntity<String> response = trainingController.health();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Training Service is running");
    }
}
