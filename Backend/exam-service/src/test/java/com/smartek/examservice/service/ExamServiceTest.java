package com.smartek.examservice.service;

import com.smartek.examservice.client.CourseClient;
import com.smartek.examservice.client.TrainingClient;
import com.smartek.examservice.dto.ExamRequest;
import com.smartek.examservice.dto.ExamResponse;
import com.smartek.examservice.entity.Exam;
import com.smartek.examservice.repository.ExamEnrollmentRepository;
import com.smartek.examservice.repository.ExamRepository;
import com.smartek.examservice.repository.ExamResultRepository;
import com.smartek.examservice.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ExamService.
 * Couvre la logique métier : CRUD, calcul des marks, suppression en cascade.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExamService - Tests unitaires")
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ExamResultRepository examResultRepository;

    @Mock
    private ExamEnrollmentRepository examEnrollmentRepository;

    @Mock
    private CourseClient courseClient;

    @Mock
    private TrainingClient trainingClient;

    @InjectMocks
    private ExamService examService;

    private Exam sampleExam;
    private ExamRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleExam = new Exam();
        sampleExam.setId(1L);
        sampleExam.setCourseId(5L);
        sampleExam.setTrainingId(2L);
        sampleExam.setExamType("QUIZ");
        sampleExam.setTitle("Quiz Spring Boot");
        sampleExam.setDescription("Quiz sur les bases de Spring Boot");
        sampleExam.setDuration(60);
        sampleExam.setPassingScore(70);
        sampleExam.setTotalMarks(100);
        sampleExam.setIsActive(true);
        sampleExam.setCreatedBy(10L);
        sampleExam.setCreatedAt(LocalDateTime.now());
        sampleExam.setQuestions(new ArrayList<>());
        sampleExam.setExercises(new ArrayList<>());

        sampleRequest = new ExamRequest();
        sampleRequest.setCourseId(5L);
        sampleRequest.setTrainingId(2L);
        sampleRequest.setExamType("QUIZ");
        sampleRequest.setTitle("Quiz Spring Boot");
        sampleRequest.setDescription("Quiz sur les bases de Spring Boot");
        sampleRequest.setDuration(60);
        sampleRequest.setPassingScore(70);
        sampleRequest.setTotalMarks(100);
        sampleRequest.setIsActive(true);
        sampleRequest.setCreatedBy(10L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createExam
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createExam()")
    class CreateExam {

        @Test
        @DisplayName("Doit créer un examen sans questions et retourner la réponse")
        void shouldCreateExamWithoutQuestions() {
            when(examRepository.save(any(Exam.class))).thenReturn(sampleExam);

            ExamResponse result = examService.createExam(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Quiz Spring Boot");
            assertThat(result.getExamType()).isEqualTo("QUIZ");
            verify(examRepository).save(any(Exam.class));
        }

        @Test
        @DisplayName("Doit utiliser 'QUIZ' comme type par défaut si non spécifié")
        void shouldDefaultToQuizTypeWhenNotSpecified() {
            sampleRequest.setExamType(null);
            when(examRepository.save(any(Exam.class))).thenAnswer(inv -> {
                Exam e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });

            ExamResponse result = examService.createExam(sampleRequest);

            assertThat(result.getExamType()).isEqualTo("QUIZ");
        }

        @Test
        @DisplayName("Doit calculer totalMarks automatiquement à partir des questions")
        void shouldCalculateTotalMarksFromQuestions() {
            com.smartek.examservice.dto.QuestionRequest q1 = new com.smartek.examservice.dto.QuestionRequest();
            q1.setQuestionText("Question 1");
            q1.setQuestionType("MULTIPLE_CHOICE");
            q1.setMarks(10);
            q1.setCorrectAnswer("A");

            com.smartek.examservice.dto.QuestionRequest q2 = new com.smartek.examservice.dto.QuestionRequest();
            q2.setQuestionText("Question 2");
            q2.setQuestionType("MULTIPLE_CHOICE");
            q2.setMarks(20);
            q2.setCorrectAnswer("B");

            sampleRequest.setQuestions(List.of(q1, q2));
            sampleRequest.setTotalMarks(null); // Doit être calculé automatiquement

            when(examRepository.save(any(Exam.class))).thenAnswer(inv -> {
                Exam e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });
            when(questionRepository.save(any())).thenReturn(null);

            ExamResponse result = examService.createExam(sampleRequest);

            // totalMarks = 10 + 20 = 30
            assertThat(result.getTotalMarks()).isEqualTo(30);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllExams
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllExams()")
    class GetAllExams {

        @Test
        @DisplayName("Doit retourner tous les examens")
        void shouldReturnAllExams() {
            when(examRepository.findAll()).thenReturn(List.of(sampleExam));

            List<ExamResponse> result = examService.getAllExams();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Quiz Spring Boot");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun examen n'existe")
        void shouldReturnEmptyListWhenNoExams() {
            when(examRepository.findAll()).thenReturn(Collections.emptyList());

            List<ExamResponse> result = examService.getAllExams();

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getExamById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getExamById()")
    class GetExamById {

        @Test
        @DisplayName("Doit retourner l'examen correspondant à l'ID")
        void shouldReturnExamById() {
            when(examRepository.findByIdWithQuestions(1L)).thenReturn(Optional.of(sampleExam));

            ExamResponse result = examService.getExamById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit lever RuntimeException si l'examen n'existe pas")
        void shouldThrowExceptionWhenExamNotFound() {
            when(examRepository.findByIdWithQuestions(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> examService.getExamById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getExamsByCourse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getExamsByCourse()")
    class GetExamsByCourse {

        @Test
        @DisplayName("Doit retourner les examens d'un cours donné")
        void shouldReturnExamsByCourse() {
            when(examRepository.findByCourseId(5L)).thenReturn(List.of(sampleExam));

            List<ExamResponse> result = examService.getExamsByCourse(5L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCourseId()).isEqualTo(5L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteExam - logique métier complexe (suppression en cascade)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteExam() - Suppression en cascade")
    class DeleteExam {

        @Test
        @DisplayName("Doit supprimer l'examen et toutes ses dépendances dans le bon ordre")
        void shouldDeleteExamWithAllDependencies() {
            when(examRepository.findById(1L)).thenReturn(Optional.of(sampleExam));

            examService.deleteExam(1L);

            // Vérifier l'ordre de suppression : résultats → enrollments → questions → examen
            var inOrder = inOrder(examResultRepository, examEnrollmentRepository, questionRepository, examRepository);
            inOrder.verify(examResultRepository).deleteByExamId(1L);
            inOrder.verify(examEnrollmentRepository).deleteByExamId(1L);
            inOrder.verify(questionRepository).deleteByExamId(1L);
            inOrder.verify(examRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Doit lever RuntimeException si l'examen à supprimer n'existe pas")
        void shouldThrowExceptionWhenDeletingNonExistentExam() {
            when(examRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> examService.deleteExam(99L))
                    .isInstanceOf(RuntimeException.class);

            verify(examRepository, never()).deleteById(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteExamsByTrainingId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteExamsByTrainingId()")
    class DeleteExamsByTrainingId {

        @Test
        @DisplayName("Doit supprimer les enrollments puis les examens d'une formation")
        void shouldDeleteEnrollmentsBeforeExams() {
            when(examRepository.findByTrainingId(2L)).thenReturn(List.of(sampleExam));

            examService.deleteExamsByTrainingId(2L);

            var inOrder = inOrder(examEnrollmentRepository, examRepository);
            inOrder.verify(examEnrollmentRepository).deleteByTrainingId(2L);
            inOrder.verify(examRepository).deleteAll(List.of(sampleExam));
        }

        @Test
        @DisplayName("Ne doit pas appeler deleteAll si aucun examen n'est trouvé")
        void shouldNotCallDeleteAllWhenNoExamsFound() {
            when(examRepository.findByTrainingId(99L)).thenReturn(Collections.emptyList());

            examService.deleteExamsByTrainingId(99L);

            verify(examRepository, never()).deleteAll(any());
        }
    }
}
