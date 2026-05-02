package com.smartek.examservice.repository;

import com.smartek.examservice.entity.Exam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de repository pour ExamRepository.
 * Teste les requêtes JPQL custom, @EntityGraph, et méthodes dérivées.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ExamRepository - Tests @DataJpaTest")
class ExamRepositoryTest {

    @Autowired
    private ExamRepository examRepository;

    private Exam quizCourse5;
    private Exam examCourse5;
    private Exam quizTraining2;

    @BeforeEach
    void setUp() {
        examRepository.deleteAll();

        quizCourse5 = new Exam();
        quizCourse5.setCourseId(5L);
        quizCourse5.setTrainingId(null);
        quizCourse5.setExamType("QUIZ");
        quizCourse5.setTitle("Quiz Spring Boot");
        quizCourse5.setDuration(60);
        quizCourse5.setPassingScore(70);
        quizCourse5.setTotalMarks(100);
        quizCourse5.setIsActive(true);
        quizCourse5.setCreatedBy(10L);
        quizCourse5.setQuestions(new ArrayList<>());
        quizCourse5.setExercises(new ArrayList<>());

        examCourse5 = new Exam();
        examCourse5.setCourseId(5L);
        examCourse5.setTrainingId(null);
        examCourse5.setExamType("EXAM");
        examCourse5.setTitle("Examen Final Spring Boot");
        examCourse5.setDuration(120);
        examCourse5.setPassingScore(60);
        examCourse5.setTotalMarks(200);
        examCourse5.setIsActive(true);
        examCourse5.setCreatedBy(10L);
        examCourse5.setQuestions(new ArrayList<>());
        examCourse5.setExercises(new ArrayList<>());

        quizTraining2 = new Exam();
        quizTraining2.setCourseId(null);
        quizTraining2.setTrainingId(2L);
        quizTraining2.setExamType("QUIZ");
        quizTraining2.setTitle("Quiz Formation DevOps");
        quizTraining2.setDuration(45);
        quizTraining2.setPassingScore(75);
        quizTraining2.setTotalMarks(50);
        quizTraining2.setIsActive(false);
        quizTraining2.setCreatedBy(20L);
        quizTraining2.setQuestions(new ArrayList<>());
        quizTraining2.setExercises(new ArrayList<>());

        examRepository.saveAll(List.of(quizCourse5, examCourse5, quizTraining2));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByCourseId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByCourseId()")
    class FindByCourseId {

        @Test
        @DisplayName("Retourne les examens du cours 5")
        void existingCourse_returnsExams() {
            List<Exam> result = examRepository.findByCourseId(5L);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Exam::getTitle)
                    .containsExactlyInAnyOrder("Quiz Spring Boot", "Examen Final Spring Boot");
        }

        @Test
        @DisplayName("Retourne liste vide pour un cours sans examen")
        void unknownCourse_returnsEmpty() {
            assertThat(examRepository.findByCourseId(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByTrainingId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByTrainingId()")
    class FindByTrainingId {

        @Test
        @DisplayName("Retourne les examens de la formation 2")
        void existingTraining_returnsExams() {
            List<Exam> result = examRepository.findByTrainingId(2L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Quiz Formation DevOps");
        }

        @Test
        @DisplayName("Retourne liste vide pour une formation sans examen")
        void unknownTraining_returnsEmpty() {
            assertThat(examRepository.findByTrainingId(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findFirstByCourseId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findFirstByCourseId()")
    class FindFirstByCourseId {

        @Test
        @DisplayName("Retourne le premier examen du cours")
        void existingCourse_returnsFirst() {
            Optional<Exam> result = examRepository.findFirstByCourseId(5L);
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Retourne Optional.empty() si aucun examen")
        void unknownCourse_returnsEmpty() {
            assertThat(examRepository.findFirstByCourseId(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findFirstByCourseIdAndExamType
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findFirstByCourseIdAndExamType()")
    class FindFirstByCourseIdAndExamType {

        @Test
        @DisplayName("Retourne le QUIZ du cours 5")
        void quizForCourse5_found() {
            Optional<Exam> result = examRepository.findFirstByCourseIdAndExamType(5L, "QUIZ");
            assertThat(result).isPresent();
            assertThat(result.get().getExamType()).isEqualTo("QUIZ");
        }

        @Test
        @DisplayName("Retourne Optional.empty() si type inexistant pour ce cours")
        void unknownType_returnsEmpty() {
            assertThat(examRepository.findFirstByCourseIdAndExamType(5L, "UNKNOWN")).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByIsActive
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByIsActive()")
    class FindByIsActive {

        @Test
        @DisplayName("Retourne uniquement les examens actifs")
        void activeExams_returnsTwo() {
            List<Exam> active = examRepository.findByIsActive(true);
            assertThat(active).hasSize(2);
            assertThat(active).allMatch(Exam::getIsActive);
        }

        @Test
        @DisplayName("Retourne uniquement les examens inactifs")
        void inactiveExams_returnsOne() {
            List<Exam> inactive = examRepository.findByIsActive(false);
            assertThat(inactive).hasSize(1);
            assertThat(inactive.get(0).getTitle()).isEqualTo("Quiz Formation DevOps");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByCreatedBy
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByCreatedBy()")
    class FindByCreatedBy {

        @Test
        @DisplayName("Retourne les examens créés par le trainer 10")
        void trainer10_returnsTwoExams() {
            List<Exam> result = examRepository.findByCreatedBy(10L);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Retourne liste vide pour un trainer sans examen")
        void unknownTrainer_returnsEmpty() {
            assertThat(examRepository.findByCreatedBy(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByIdWithQuestions (@EntityGraph)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByIdWithQuestions() - @EntityGraph")
    class FindByIdWithQuestions {

        @Test
        @DisplayName("Retourne l'examen avec ses questions chargées")
        void existingId_returnsExamWithQuestions() {
            Optional<Exam> result = examRepository.findByIdWithQuestions(quizCourse5.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Quiz Spring Boot");
            assertThat(result.get().getQuestions()).isNotNull();
        }

        @Test
        @DisplayName("Retourne Optional.empty() pour un ID inexistant")
        void unknownId_returnsEmpty() {
            assertThat(examRepository.findByIdWithQuestions(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // countByCourseId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("countByCourseId()")
    class CountByCourseId {

        @Test
        @DisplayName("Compte correctement les examens d'un cours")
        void course5_countIsTwo() {
            assertThat(examRepository.countByCourseId(5L)).isEqualTo(2L);
        }

        @Test
        @DisplayName("Retourne 0 pour un cours sans examen")
        void unknownCourse_countIsZero() {
            assertThat(examRepository.countByCourseId(999L)).isEqualTo(0L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pagination
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Pagination")
    class PaginationTests {

        @Test
        @DisplayName("findAll avec pagination retourne la bonne page")
        void findAll_paginated_returnsCorrectPage() {
            Page<Exam> page = examRepository.findAll(PageRequest.of(0, 2));

            assertThat(page.getTotalElements()).isEqualTo(3L);
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("findByCourseId avec pagination")
        void findByCourseId_paginated() {
            Page<Exam> page = examRepository.findByCourseId(5L, PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(2L);
            assertThat(page.getContent()).hasSize(2);
        }
    }
}
