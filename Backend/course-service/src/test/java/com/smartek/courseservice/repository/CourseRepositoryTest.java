package com.smartek.courseservice.repository;

import com.smartek.courseservice.entity.Course;
import com.smartek.courseservice.entity.DeliveryMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de repository pour CourseRepository.
 * Teste les requêtes @EntityGraph, méthodes dérivées et pagination.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("CourseRepository - Tests @DataJpaTest")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    private Course course1;
    private Course course2;
    private Course course3;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();

        course1 = Course.builder()
                .title("Spring Boot Avancé")
                .content("Microservices avec Spring Boot")
                .duration(LocalDate.of(2026, 12, 31))
                .trainerId(10L)
                .deliveryMode(DeliveryMode.EN_LIGNE)
                .chapters(new ArrayList<>())
                .liveSessions(new HashSet<>())
                .build();

        course2 = Course.builder()
                .title("Angular Fondamentaux")
                .content("Développement Frontend avec Angular")
                .duration(LocalDate.of(2026, 9, 30))
                .trainerId(10L)
                .deliveryMode(DeliveryMode.PRESENTIEL)
                .chapters(new ArrayList<>())
                .liveSessions(new HashSet<>())
                .build();

        course3 = Course.builder()
                .title("DevOps CI/CD")
                .content("Pipeline CI/CD avec GitHub Actions")
                .duration(LocalDate.of(2026, 6, 30))
                .trainerId(20L)
                .deliveryMode(DeliveryMode.EN_LIGNE)
                .chapters(new ArrayList<>())
                .liveSessions(new HashSet<>())
                .build();

        courseRepository.saveAll(List.of(course1, course2, course3));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByTitle
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByTitle()")
    class FindByTitle {

        @Test
        @DisplayName("Retourne le cours pour un titre existant")
        void existingTitle_returnsCourse() {
            Optional<Course> result = courseRepository.findByTitle("Spring Boot Avancé");

            assertThat(result).isPresent();
            assertThat(result.get().getTrainerId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Retourne Optional.empty() pour un titre inexistant")
        void unknownTitle_returnsEmpty() {
            assertThat(courseRepository.findByTitle("Cours Inexistant")).isEmpty();
        }

        @Test
        @DisplayName("Titre exact requis — partiel ne fonctionne pas")
        void partialTitle_returnsEmpty() {
            assertThat(courseRepository.findByTitle("Spring Boot")).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByTrainerId (@EntityGraph)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByTrainerId() - @EntityGraph")
    class FindByTrainerId {

        @Test
        @DisplayName("Retourne les cours du trainer 10 avec chapitres chargés")
        void trainer10_returnsTwoCourses() {
            List<Course> result = courseRepository.findByTrainerId(10L);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Course::getTitle)
                    .containsExactlyInAnyOrder("Spring Boot Avancé", "Angular Fondamentaux");
            // Vérifie que les chapitres sont chargés (pas de LazyInitializationException)
            result.forEach(c -> assertThat(c.getChapters()).isNotNull());
        }

        @Test
        @DisplayName("Retourne liste vide pour un trainer sans cours")
        void unknownTrainer_returnsEmpty() {
            assertThat(courseRepository.findByTrainerId(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByIdWithChapters (@EntityGraph)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByIdWithChapters() - @EntityGraph")
    class FindByIdWithChapters {

        @Test
        @DisplayName("Retourne le cours avec chapitres et sessions chargés")
        void existingId_returnsCourseWithRelations() {
            Optional<Course> result = courseRepository.findByIdWithChapters(course1.getCourseId());

            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Spring Boot Avancé");
            assertThat(result.get().getChapters()).isNotNull();
            assertThat(result.get().getLiveSessions()).isNotNull();
        }

        @Test
        @DisplayName("Retourne Optional.empty() pour un ID inexistant")
        void unknownId_returnsEmpty() {
            assertThat(courseRepository.findByIdWithChapters(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findAllWithChapters (@EntityGraph)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findAllWithChapters() - @EntityGraph")
    class FindAllWithChapters {

        @Test
        @DisplayName("Retourne tous les cours avec relations chargées")
        void returnsAllCoursesWithRelations() {
            List<Course> result = courseRepository.findAllWithChapters();

            assertThat(result).hasSize(3);
            result.forEach(c -> {
                assertThat(c.getChapters()).isNotNull();
                assertThat(c.getLiveSessions()).isNotNull();
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // countByTrainerId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("countByTrainerId()")
    class CountByTrainerId {

        @Test
        @DisplayName("Compte correctement les cours par trainer")
        void countsCorrectly() {
            assertThat(courseRepository.countByTrainerId(10L)).isEqualTo(2L);
            assertThat(courseRepository.countByTrainerId(20L)).isEqualTo(1L);
            assertThat(courseRepository.countByTrainerId(999L)).isEqualTo(0L);
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
            Page<Course> page = courseRepository.findAll(PageRequest.of(0, 2));

            assertThat(page.getTotalElements()).isEqualTo(3L);
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("findByTrainerId paginé")
        void findByTrainerId_paginated() {
            Page<Course> page = courseRepository.findByTrainerId(10L, PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(2L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD de base
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CRUD de base")
    class BasicCrud {

        @Test
        @DisplayName("Sauvegarde et retrouve un cours")
        void saveAndFind() {
            Course newCourse = Course.builder()
                    .title("Kubernetes Avancé")
                    .content("Orchestration de conteneurs")
                    .duration(LocalDate.of(2027, 3, 31))
                    .trainerId(30L)
                    .deliveryMode(DeliveryMode.EN_LIGNE)
                    .chapters(new ArrayList<>())
                    .liveSessions(new HashSet<>())
                    .build();

            Course saved = courseRepository.save(newCourse);
            assertThat(saved.getCourseId()).isNotNull();
            assertThat(courseRepository.findById(saved.getCourseId())).isPresent();
        }

        @Test
        @DisplayName("Supprime un cours")
        void deleteById() {
            Long id = course1.getCourseId();
            courseRepository.deleteById(id);
            assertThat(courseRepository.findById(id)).isEmpty();
        }
    }
}
