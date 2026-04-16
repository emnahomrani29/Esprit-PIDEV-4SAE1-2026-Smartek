package com.smartek.courseservice.service;

import com.smartek.courseservice.client.TrainingClient;
import com.smartek.courseservice.dto.CourseRequest;
import com.smartek.courseservice.dto.CourseResponse;
import com.smartek.courseservice.dto.CourseStatsResponse;
import com.smartek.courseservice.entity.Course;
import com.smartek.courseservice.entity.CourseCompletion;
import com.smartek.courseservice.entity.DeliveryMode;
import com.smartek.courseservice.exception.DuplicateResourceException;
import com.smartek.courseservice.exception.ResourceNotFoundException;
import com.smartek.courseservice.mapper.CourseMapper;
import com.smartek.courseservice.repository.CourseCompletionRepository;
import com.smartek.courseservice.repository.CourseRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CourseService.
 * Couvre la logique métier : CRUD, gestion des doublons, statistiques.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService - Tests unitaires")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseCompletionRepository courseCompletionRepository;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private TrainingClient trainingClient;

    @InjectMocks
    private CourseService courseService;

    private Course sampleCourse;
    private CourseRequest sampleRequest;
    private CourseResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleCourse = Course.builder()
                .courseId(1L)
                .title("Spring Boot Avancé")
                .content("Contenu du cours")
                .duration(LocalDate.of(2025, 6, 30))
                .trainerId(10L)
                .deliveryMode(DeliveryMode.PRESENTIEL)
                .chapters(new ArrayList<>())
                .build();

        sampleRequest = CourseRequest.builder()
                .title("Spring Boot Avancé")
                .content("Contenu du cours")
                .duration(LocalDate.of(2025, 6, 30))
                .trainerId(10L)
                .deliveryMode(DeliveryMode.PRESENTIEL)
                .build();

        sampleResponse = CourseResponse.builder()
                .courseId(1L)
                .title("Spring Boot Avancé")
                .trainerId(10L)
                .deliveryMode(DeliveryMode.PRESENTIEL)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createCourse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createCourse()")
    class CreateCourse {

        @Test
        @DisplayName("Doit créer un cours avec succès")
        void shouldCreateCourseSuccessfully() {
            when(courseRepository.findByTitle(sampleRequest.getTitle())).thenReturn(Optional.empty());
            when(courseMapper.toEntity(sampleRequest)).thenReturn(sampleCourse);
            when(courseRepository.save(sampleCourse)).thenReturn(sampleCourse);
            when(courseMapper.toResponse(sampleCourse, "Cours créé avec succès")).thenReturn(sampleResponse);

            CourseResponse result = courseService.createCourse(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getCourseId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Spring Boot Avancé");
            verify(courseRepository).save(sampleCourse);
        }

        @Test
        @DisplayName("Doit lever DuplicateResourceException si le titre existe déjà")
        void shouldThrowExceptionWhenTitleAlreadyExists() {
            when(courseRepository.findByTitle(sampleRequest.getTitle()))
                    .thenReturn(Optional.of(sampleCourse));

            assertThatThrownBy(() -> courseService.createCourse(sampleRequest))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(courseRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllCourses
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllCourses()")
    class GetAllCourses {

        @Test
        @DisplayName("Doit retourner la liste de tous les cours")
        void shouldReturnAllCourses() {
            when(courseRepository.findAllWithChapters()).thenReturn(List.of(sampleCourse));
            when(courseMapper.toResponse(sampleCourse)).thenReturn(sampleResponse);

            List<CourseResponse> result = courseService.getAllCourses();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Spring Boot Avancé");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun cours n'existe")
        void shouldReturnEmptyListWhenNoCourses() {
            when(courseRepository.findAllWithChapters()).thenReturn(Collections.emptyList());

            List<CourseResponse> result = courseService.getAllCourses();

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCourseById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getCourseById()")
    class GetCourseById {

        @Test
        @DisplayName("Doit retourner le cours correspondant à l'ID")
        void shouldReturnCourseById() {
            when(courseRepository.findByIdWithChapters(1L)).thenReturn(Optional.of(sampleCourse));
            when(courseMapper.toResponse(sampleCourse)).thenReturn(sampleResponse);

            CourseResponse result = courseService.getCourseById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getCourseId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le cours n'existe pas")
        void shouldThrowExceptionWhenCourseNotFound() {
            when(courseRepository.findByIdWithChapters(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourseById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCoursesByTrainer
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getCoursesByTrainer()")
    class GetCoursesByTrainer {

        @Test
        @DisplayName("Doit retourner les cours d'un trainer donné")
        void shouldReturnCoursesByTrainer() {
            when(courseRepository.findByTrainerId(10L)).thenReturn(List.of(sampleCourse));
            when(courseMapper.toResponse(sampleCourse)).thenReturn(sampleResponse);

            List<CourseResponse> result = courseService.getCoursesByTrainer(10L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTrainerId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si le trainer n'a pas de cours")
        void shouldReturnEmptyListForTrainerWithNoCourses() {
            when(courseRepository.findByTrainerId(99L)).thenReturn(Collections.emptyList());

            List<CourseResponse> result = courseService.getCoursesByTrainer(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateCourse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateCourse()")
    class UpdateCourse {

        @Test
        @DisplayName("Doit mettre à jour un cours existant")
        void shouldUpdateCourseSuccessfully() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
            when(courseRepository.save(sampleCourse)).thenReturn(sampleCourse);
            when(courseMapper.toResponse(sampleCourse, "Cours mis à jour avec succès")).thenReturn(sampleResponse);

            CourseResponse result = courseService.updateCourse(1L, sampleRequest);

            assertThat(result).isNotNull();
            verify(courseMapper).updateEntityFromRequest(sampleCourse, sampleRequest);
            verify(courseRepository).save(sampleCourse);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le cours à mettre à jour n'existe pas")
        void shouldThrowExceptionWhenUpdatingNonExistentCourse() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.updateCourse(99L, sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteCourse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteCourse()")
    class DeleteCourse {

        @Test
        @DisplayName("Doit supprimer un cours existant")
        void shouldDeleteCourseSuccessfully() {
            when(courseRepository.existsById(1L)).thenReturn(true);

            courseService.deleteCourse(1L);

            verify(courseRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le cours à supprimer n'existe pas")
        void shouldThrowExceptionWhenDeletingNonExistentCourse() {
            when(courseRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> courseService.deleteCourse(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(courseRepository, never()).deleteById(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserCourseStats - logique métier complexe
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUserCourseStats() - Logique métier")
    class GetUserCourseStats {

        @Test
        @DisplayName("Doit retourner des stats vides si l'utilisateur n'a aucune complétion")
        void shouldReturnEmptyStatsWhenNoCompletions() {
            when(courseCompletionRepository.findByUserId(5L)).thenReturn(Collections.emptyList());

            CourseStatsResponse result = courseService.getUserCourseStats(5L);

            assertThat(result.getUserId()).isEqualTo(5L);
            assertThat(result.getCompleted()).isZero();
            assertThat(result.getTotalEnrolled()).isZero();
            assertThat(result.getCompletionRate()).isZero();
        }

        @Test
        @DisplayName("Doit calculer correctement le taux de complétion à 100% si tous les cours sont complétés")
        void shouldCalculate100PercentCompletionRate() {
            CourseCompletion completion = new CourseCompletion();
            completion.setCourseId(1L);
            completion.setUserId(5L);

            when(courseCompletionRepository.findByUserId(5L)).thenReturn(List.of(completion));
            when(courseRepository.findAllById(List.of(1L))).thenReturn(List.of(sampleCourse));

            CourseStatsResponse result = courseService.getUserCourseStats(5L);

            assertThat(result.getCompleted()).isEqualTo(1);
            assertThat(result.getCompletionRate()).isEqualTo(100.0);
        }
    }
}
