package com.smartek.trainingservice.repository;

import com.smartek.trainingservice.entity.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de repository pour TrainingRepository.
 * Teste les requêtes JPQL custom, méthodes dérivées et pagination.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TrainingRepository - Tests @DataJpaTest")
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    private Training springBackend;
    private Training angularFrontend;
    private Training devopsAdvanced;

    @BeforeEach
    void setUp() {
        trainingRepository.deleteAll();

        springBackend = Training.builder()
                .title("Formation Spring Boot")
                .description("Microservices avec Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(List.of(1L, 2L))
                .createdBy(10L)
                .build();

        angularFrontend = Training.builder()
                .title("Formation Angular")
                .description("Développement Frontend")
                .category("Frontend")
                .level("Débutant")
                .duration(LocalDate.now().plusMonths(2))
                .courseIds(List.of(3L))
                .createdBy(10L)
                .build();

        devopsAdvanced = Training.builder()
                .title("Formation DevOps")
                .description("CI/CD et conteneurisation")
                .category("DevOps")
                .level("Avancé")
                .duration(LocalDate.now().plusMonths(4))
                .courseIds(List.of(2L, 4L))
                .createdBy(20L)
                .build();

        trainingRepository.saveAll(List.of(springBackend, angularFrontend, devopsAdvanced));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByTitle
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByTitle()")
    class FindByTitle {

        @Test
        @DisplayName("Retourne la formation pour un titre existant")
        void existingTitle_returnsTraining() {
            Optional<Training> result = trainingRepository.findByTitle("Formation Spring Boot");

            assertThat(result).isPresent();
            assertThat(result.get().getCategory()).isEqualTo("Backend");
        }

        @Test
        @DisplayName("Retourne Optional.empty() pour un titre inexistant")
        void unknownTitle_returnsEmpty() {
            assertThat(trainingRepository.findByTitle("Formation Inconnue")).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByCategory
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByCategory()")
    class FindByCategory {

        @Test
        @DisplayName("Retourne les formations Backend")
        void backendCategory_returnsOne() {
            List<Training> result = trainingRepository.findByCategory("Backend");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Formation Spring Boot");
        }

        @Test
        @DisplayName("Retourne liste vide pour une catégorie inexistante")
        void unknownCategory_returnsEmpty() {
            assertThat(trainingRepository.findByCategory("Mobile")).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByLevel
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByLevel()")
    class FindByLevel {

        @Test
        @DisplayName("Retourne les formations Avancé")
        void advancedLevel_returnsOne() {
            List<Training> result = trainingRepository.findByLevel("Avancé");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Formation DevOps");
        }

        @Test
        @DisplayName("Retourne liste vide pour un niveau inexistant")
        void unknownLevel_returnsEmpty() {
            assertThat(trainingRepository.findByLevel("Expert")).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByCourseId (JPQL custom — MEMBER OF)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByCourseId() - JPQL MEMBER OF")
    class FindByCourseId {

        @Test
        @DisplayName("Retourne les formations contenant le cours 2")
        void course2_returnsTwoTrainings() {
            List<Training> result = trainingRepository.findByCourseId(2L);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Training::getTitle)
                    .containsExactlyInAnyOrder("Formation Spring Boot", "Formation DevOps");
        }

        @Test
        @DisplayName("Retourne les formations contenant le cours 3")
        void course3_returnsOneTraining() {
            List<Training> result = trainingRepository.findByCourseId(3L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Formation Angular");
        }

        @Test
        @DisplayName("Retourne liste vide pour un cours non associé")
        void unknownCourse_returnsEmpty() {
            assertThat(trainingRepository.findByCourseId(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // countByCategory
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("countByCategory()")
    class CountByCategory {

        @Test
        @DisplayName("Compte correctement par catégorie")
        void countsCorrectly() {
            assertThat(trainingRepository.countByCategory("Backend")).isEqualTo(1L);
            assertThat(trainingRepository.countByCategory("Frontend")).isEqualTo(1L);
            assertThat(trainingRepository.countByCategory("DevOps")).isEqualTo(1L);
            assertThat(trainingRepository.countByCategory("Mobile")).isEqualTo(0L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByCreatedBy
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByCreatedBy()")
    class FindByCreatedBy {

        @Test
        @DisplayName("Retourne les formations créées par le trainer 10")
        void trainer10_returnsTwoTrainings() {
            List<Training> result = trainingRepository.findByCreatedBy(10L);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Retourne liste vide pour un trainer sans formation")
        void unknownTrainer_returnsEmpty() {
            assertThat(trainingRepository.findByCreatedBy(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pagination
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Pagination")
    class PaginationTests {

        @Test
        @DisplayName("findAll paginé retourne la bonne page")
        void findAll_paginated() {
            Page<Training> page = trainingRepository.findAll(PageRequest.of(0, 2));

            assertThat(page.getTotalElements()).isEqualTo(3L);
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("findByCategory paginé")
        void findByCategory_paginated() {
            // Ajouter une 2e formation Backend
            Training spring2 = Training.builder()
                    .title("Spring Security")
                    .category("Backend")
                    .level("Avancé")
                    .duration(LocalDate.now().plusMonths(2))
                    .createdBy(10L)
                    .build();
            trainingRepository.save(spring2);

            Page<Training> page = trainingRepository.findByCategory("Backend", PageRequest.of(0, 10));
            assertThat(page.getTotalElements()).isEqualTo(2L);
        }
    }
}
