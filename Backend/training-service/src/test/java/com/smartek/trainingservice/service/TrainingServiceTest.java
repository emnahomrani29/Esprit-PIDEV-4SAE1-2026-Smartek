package com.smartek.trainingservice.service;

import com.smartek.trainingservice.client.CourseClient;
import com.smartek.trainingservice.client.ExamClient;
import com.smartek.trainingservice.dto.TrainingRequest;
import com.smartek.trainingservice.dto.TrainingResponse;
import com.smartek.trainingservice.entity.Training;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TrainingService - Tests unitaires")
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainingEnrollmentRepository trainingEnrollmentRepository;

    @Mock
    private CourseClient courseClient;

    @Mock
    private ExamClient examClient;

    @InjectMocks
    private TrainingService trainingService;

    private Training training;
    private TrainingRequest request;

    @BeforeEach
    void setUp() {
        training = Training.builder()
                .trainingId(1L)
                .title("Formation Spring Boot")
                .description("Microservices avec Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(new ArrayList<>(List.of(1L, 2L)))
                .createdBy(1L)
                .build();

        request = TrainingRequest.builder()
                .title("Formation Spring Boot")
                .description("Microservices avec Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(List.of(1L, 2L))
                .createdBy(1L)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createTraining
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createTraining()")
    class CreateTraining {

        @Test
        @DisplayName("Doit créer une formation avec succès")
        void shouldCreateTrainingSuccessfully() {
            when(trainingRepository.findByTitle("Formation Spring Boot")).thenReturn(Optional.empty());
            when(trainingRepository.save(any(Training.class))).thenReturn(training);
            when(courseClient.getCourseById(any())).thenThrow(new RuntimeException("feign"));
            when(courseClient.getChaptersByCourseId(any())).thenThrow(new RuntimeException("feign"));

            TrainingResponse response = trainingService.createTraining(request);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Formation Spring Boot");
            assertThat(response.getCategory()).isEqualTo("Backend");
            verify(trainingRepository, times(1)).save(any(Training.class));
        }

        @Test
        @DisplayName("Doit lever une exception si le titre existe déjà")
        void shouldThrowWhenTitleAlreadyExists() {
            when(trainingRepository.findByTitle("Formation Spring Boot"))
                    .thenReturn(Optional.of(training));

            assertThatThrownBy(() -> trainingService.createTraining(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("existe déjà");

            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit créer une formation sans cours associés")
        void shouldCreateTrainingWithoutCourses() {
            request.setCourseIds(null);
            Training trainingNoCourses = Training.builder()
                    .trainingId(2L)
                    .title("Formation Spring Boot")
                    .category("Backend")
                    .level("Débutant")
                    .duration(LocalDate.now().plusMonths(1))
                    .courseIds(List.of())
                    .build();

            when(trainingRepository.findByTitle("Formation Spring Boot")).thenReturn(Optional.empty());
            when(trainingRepository.save(any(Training.class))).thenReturn(trainingNoCourses);

            TrainingResponse response = trainingService.createTraining(request);

            assertThat(response).isNotNull();
            assertThat(response.getCourseIds()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllTrainings
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllTrainings()")
    class GetAllTrainings {

        @Test
        @DisplayName("Doit retourner toutes les formations")
        void shouldReturnAllTrainings() {
            when(trainingRepository.findAll()).thenReturn(List.of(training));
            when(courseClient.getCourseById(any())).thenThrow(new RuntimeException("feign"));
            when(courseClient.getChaptersByCourseId(any())).thenThrow(new RuntimeException("feign"));

            List<TrainingResponse> result = trainingService.getAllTrainings();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Formation Spring Boot");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune formation n'existe")
        void shouldReturnEmptyListWhenNoTrainings() {
            when(trainingRepository.findAll()).thenReturn(Collections.emptyList());

            List<TrainingResponse> result = trainingService.getAllTrainings();

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTrainingById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTrainingById()")
    class GetTrainingById {

        @Test
        @DisplayName("Doit retourner la formation correspondant à l'ID")
        void shouldReturnTrainingById() {
            when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));
            when(courseClient.getCourseById(any())).thenThrow(new RuntimeException("feign"));
            when(courseClient.getChaptersByCourseId(any())).thenThrow(new RuntimeException("feign"));

            TrainingResponse response = trainingService.getTrainingById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Formation Spring Boot");
        }

        @Test
        @DisplayName("Doit lever RuntimeException si la formation n'existe pas")
        void shouldThrowWhenTrainingNotFound() {
            when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> trainingService.getTrainingById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Formation non trouvée");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTrainingsByCategory
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTrainingsByCategory()")
    class GetTrainingsByCategory {

        @Test
        @DisplayName("Doit retourner les formations filtrées par catégorie")
        void shouldReturnTrainingsByCategory() {
            when(trainingRepository.findByCategory("Backend")).thenReturn(List.of(training));
            when(courseClient.getCourseById(any())).thenThrow(new RuntimeException("feign"));
            when(courseClient.getChaptersByCourseId(any())).thenThrow(new RuntimeException("feign"));

            List<TrainingResponse> result = trainingService.getTrainingsByCategory("Backend");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("Backend");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune formation dans cette catégorie")
        void shouldReturnEmptyListForUnknownCategory() {
            when(trainingRepository.findByCategory("Unknown")).thenReturn(Collections.emptyList());

            List<TrainingResponse> result = trainingService.getTrainingsByCategory("Unknown");

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTrainingsByLevel
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTrainingsByLevel()")
    class GetTrainingsByLevel {

        @Test
        @DisplayName("Doit retourner les formations filtrées par niveau")
        void shouldReturnTrainingsByLevel() {
            when(trainingRepository.findByLevel("Intermédiaire")).thenReturn(List.of(training));
            when(courseClient.getCourseById(any())).thenThrow(new RuntimeException("feign"));
            when(courseClient.getChaptersByCourseId(any())).thenThrow(new RuntimeException("feign"));

            List<TrainingResponse> result = trainingService.getTrainingsByLevel("Intermédiaire");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLevel()).isEqualTo("Intermédiaire");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateTraining
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateTraining()")
    class UpdateTraining {

        @Test
        @DisplayName("Doit mettre à jour une formation existante")
        void shouldUpdateTrainingSuccessfully() {
            when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));
            when(trainingRepository.save(any(Training.class))).thenReturn(training);
            when(courseClient.getCourseById(any())).thenThrow(new RuntimeException("feign"));
            when(courseClient.getChaptersByCourseId(any())).thenThrow(new RuntimeException("feign"));

            TrainingResponse response = trainingService.updateTraining(1L, request);

            assertThat(response).isNotNull();
            verify(trainingRepository, times(1)).save(any(Training.class));
        }

        @Test
        @DisplayName("Doit lever RuntimeException si la formation à mettre à jour n'existe pas")
        void shouldThrowWhenUpdatingNonExistentTraining() {
            when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> trainingService.updateTraining(99L, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Formation non trouvée");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addCourseToTraining / removeCourseFromTraining
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("addCourseToTraining() / removeCourseFromTraining()")
    class CourseManagement {

        @Test
        @DisplayName("Doit ajouter un cours à une formation")
        void shouldAddCourseToTraining() {
            when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));
            when(trainingRepository.save(any(Training.class))).thenReturn(training);
            when(courseClient.getCourseById(any())).thenThrow(new RuntimeException("feign"));
            when(courseClient.getChaptersByCourseId(any())).thenThrow(new RuntimeException("feign"));

            TrainingResponse response = trainingService.addCourseToTraining(1L, 99L);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Ne doit pas dupliquer un cours déjà présent dans la formation")
        void shouldNotDuplicateCourseAlreadyInTraining() {
            when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));
            when(courseClient.getCourseById(any())).thenThrow(new RuntimeException("feign"));
            when(courseClient.getChaptersByCourseId(any())).thenThrow(new RuntimeException("feign"));

            // cours 1L est déjà dans la liste
            trainingService.addCourseToTraining(1L, 1L);

            // save ne doit pas être appelé car le cours est déjà présent
            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit supprimer un cours d'une formation")
        void shouldRemoveCourseFromTraining() {
            when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));
            when(trainingRepository.save(any(Training.class))).thenReturn(training);
            when(courseClient.getCourseById(any())).thenThrow(new RuntimeException("feign"));
            when(courseClient.getChaptersByCourseId(any())).thenThrow(new RuntimeException("feign"));

            TrainingResponse response = trainingService.removeCourseFromTraining(1L, 1L);

            assertThat(response).isNotNull();
            verify(trainingRepository, times(1)).save(any(Training.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteTraining
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteTraining()")
    class DeleteTraining {

        @Test
        @DisplayName("Doit lever RuntimeException si la formation à supprimer n'existe pas")
        void shouldThrowWhenDeletingNonExistentTraining() {
            when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> trainingService.deleteTraining(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Formation non trouvée");
        }

        @Test
        @DisplayName("Doit appeler deleteTraining sans exception si la formation existe")
        void shouldNotThrowWhenDeletingExistingTraining() {
            when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));
            doNothing().when(trainingEnrollmentRepository).deleteByTrainingTrainingId(1L);

            assertThatCode(() -> trainingService.deleteTraining(1L))
                    .doesNotThrowAnyException();
        }
    }
}
