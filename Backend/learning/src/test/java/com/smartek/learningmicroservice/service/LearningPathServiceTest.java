package com.smartek.learningmicroservice.service;

import com.smartek.learningmicroservice.dto.LearningPathRequest;
import com.smartek.learningmicroservice.dto.LearningPathResponse;
import com.smartek.learningmicroservice.entity.LearningPath;
import com.smartek.learningmicroservice.entity.LearningPathStatus;
import com.smartek.learningmicroservice.repository.LearningPathRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LearningPathService - Tests unitaires")
class LearningPathServiceTest {

    @Mock private LearningPathRepository pathRepository;

    @InjectMocks private LearningPathService learningPathService;

    private LearningPath samplePath;
    private LearningPathRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePath = LearningPath.builder()
                .pathId(1L)
                .title("Parcours Java Backend")
                .description("Maîtriser Spring Boot et les microservices")
                .learnerId(5L)
                .learnerName("Alice Dupont")
                .status(LearningPathStatus.EN_COURS)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .progress(40)
                .build();

        sampleRequest = new LearningPathRequest(
                "Parcours Java Backend",
                "Maîtriser Spring Boot et les microservices",
                5L,
                "Alice Dupont",
                LearningPathStatus.EN_COURS,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30),
                40
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createPath
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createPath()")
    class CreatePath {

        @Test
        @DisplayName("Doit créer un parcours avec succès")
        void shouldCreatePathSuccessfully() {
            when(pathRepository.existsByLearnerIdAndTitle(5L, "Parcours Java Backend")).thenReturn(false);
            when(pathRepository.save(any(LearningPath.class))).thenReturn(samplePath);

            LearningPathResponse result = learningPathService.createPath(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Parcours Java Backend");
            assertThat(result.getLearnerId()).isEqualTo(5L);
            assertThat(result.getProgress()).isEqualTo(40);
            verify(pathRepository).save(any(LearningPath.class));
        }

        @Test
        @DisplayName("Doit lever RuntimeException si un parcours avec ce titre existe déjà")
        void shouldThrowWhenDuplicateTitle() {
            when(pathRepository.existsByLearnerIdAndTitle(5L, "Parcours Java Backend")).thenReturn(true);

            assertThatThrownBy(() -> learningPathService.createPath(sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("existe déjà");

            verify(pathRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllPathsByLearner
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllPathsByLearner()")
    class GetAllPathsByLearner {

        @Test
        @DisplayName("Doit retourner les parcours d'un apprenant")
        void shouldReturnPathsByLearner() {
            when(pathRepository.findByLearnerIdOrderByStartDateDesc(5L)).thenReturn(List.of(samplePath));

            List<LearningPathResponse> result = learningPathService.getAllPathsByLearner(5L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLearnerId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun parcours")
        void shouldReturnEmptyListWhenNoPaths() {
            when(pathRepository.findByLearnerIdOrderByStartDateDesc(99L)).thenReturn(Collections.emptyList());

            List<LearningPathResponse> result = learningPathService.getAllPathsByLearner(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPathById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPathById()")
    class GetPathById {

        @Test
        @DisplayName("Doit retourner le parcours par ID")
        void shouldReturnPathById() {
            when(pathRepository.findById(1L)).thenReturn(Optional.of(samplePath));

            LearningPathResponse result = learningPathService.getPathById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getPathId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Parcours Java Backend");
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le parcours n'existe pas")
        void shouldThrowWhenPathNotFound() {
            when(pathRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> learningPathService.getPathById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("non trouvé");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPathsByStatus
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPathsByStatus()")
    class GetPathsByStatus {

        @Test
        @DisplayName("Doit retourner les parcours filtrés par statut EN_COURS")
        void shouldReturnPathsByStatus() {
            when(pathRepository.findByStatus(LearningPathStatus.EN_COURS)).thenReturn(List.of(samplePath));

            List<LearningPathResponse> result = learningPathService.getPathsByStatus(LearningPathStatus.EN_COURS);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(LearningPathStatus.EN_COURS);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun parcours avec ce statut")
        void shouldReturnEmptyListForUnknownStatus() {
            when(pathRepository.findByStatus(LearningPathStatus.TERMINE)).thenReturn(Collections.emptyList());

            List<LearningPathResponse> result = learningPathService.getPathsByStatus(LearningPathStatus.TERMINE);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPathsByLearnerAndStatus
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPathsByLearnerAndStatus()")
    class GetPathsByLearnerAndStatus {

        @Test
        @DisplayName("Doit retourner les parcours d'un apprenant filtrés par statut")
        void shouldReturnPathsByLearnerAndStatus() {
            when(pathRepository.findByLearnerIdAndStatus(5L, LearningPathStatus.EN_COURS))
                    .thenReturn(List.of(samplePath));

            List<LearningPathResponse> result = learningPathService.getPathsByLearnerAndStatus(
                    5L, LearningPathStatus.EN_COURS);

            assertThat(result).hasSize(1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updatePath
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updatePath()")
    class UpdatePath {

        @Test
        @DisplayName("Doit mettre à jour un parcours existant")
        void shouldUpdatePathSuccessfully() {
            when(pathRepository.findById(1L)).thenReturn(Optional.of(samplePath));
            when(pathRepository.existsByLearnerIdAndTitleAndPathIdNot(5L, "Parcours Java Backend", 1L))
                    .thenReturn(false);
            when(pathRepository.save(any(LearningPath.class))).thenReturn(samplePath);

            LearningPathResponse result = learningPathService.updatePath(1L, sampleRequest);

            assertThat(result).isNotNull();
            verify(pathRepository).save(any(LearningPath.class));
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le parcours à mettre à jour n'existe pas")
        void shouldThrowWhenPathNotFound() {
            when(pathRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> learningPathService.updatePath(99L, sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("non trouvé");
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le nouveau titre est déjà utilisé par un autre parcours")
        void shouldThrowWhenTitleAlreadyUsedByAnotherPath() {
            when(pathRepository.findById(1L)).thenReturn(Optional.of(samplePath));
            when(pathRepository.existsByLearnerIdAndTitleAndPathIdNot(5L, "Parcours Java Backend", 1L))
                    .thenReturn(true);

            assertThatThrownBy(() -> learningPathService.updatePath(1L, sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("existe déjà");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deletePath
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deletePath()")
    class DeletePath {

        @Test
        @DisplayName("Doit supprimer un parcours existant")
        void shouldDeletePathSuccessfully() {
            when(pathRepository.existsById(1L)).thenReturn(true);

            learningPathService.deletePath(1L);

            verify(pathRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le parcours n'existe pas")
        void shouldThrowWhenPathNotFound() {
            when(pathRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> learningPathService.deletePath(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("non trouvé");

            verify(pathRepository, never()).deleteById(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllPaths
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllPaths()")
    class GetAllPaths {

        @Test
        @DisplayName("Doit retourner tous les parcours")
        void shouldReturnAllPaths() {
            when(pathRepository.findAll()).thenReturn(List.of(samplePath));

            List<LearningPathResponse> result = learningPathService.getAllPaths();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun parcours")
        void shouldReturnEmptyListWhenNoPaths() {
            when(pathRepository.findAll()).thenReturn(Collections.emptyList());

            List<LearningPathResponse> result = learningPathService.getAllPaths();

            assertThat(result).isEmpty();
        }
    }
}
