package com.smartek.examservice.controller;

import com.smartek.examservice.dto.QuestionRequest;
import com.smartek.examservice.entity.Question;
import com.smartek.examservice.service.QuestionService;
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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour QuestionController (sans Spring context).
 * Verifie la delegation au service et les codes HTTP retournes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionController - Tests unitaires")
class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private QuestionController questionController;

    private QuestionRequest validRequest;
    private Question sampleQuestion;

    @BeforeEach
    void setUp() {
        validRequest = new QuestionRequest();
        validRequest.setExamId(10L);
        validRequest.setQuestionText("Qu'est-ce que Spring Boot ?");
        validRequest.setQuestionType("MULTIPLE_CHOICE");
        validRequest.setMarks(5);
        validRequest.setCorrectAnswer("Un framework Java");

        sampleQuestion = new Question();
        sampleQuestion.setId(1L);
        sampleQuestion.setQuestionText("Qu'est-ce que Spring Boot ?");
        sampleQuestion.setQuestionType("MULTIPLE_CHOICE");
        sampleQuestion.setMarks(5);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createQuestion (legacy /api/questions)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/questions - Creation de question (legacy)")
    class CreateQuestion {

        @Test
        @DisplayName("Doit creer une question et retourner 201")
        void shouldCreateQuestionAndReturn201() {
            when(questionService.createQuestion(any(QuestionRequest.class))).thenReturn(sampleQuestion);

            ResponseEntity<Question> response = questionController.createQuestion(validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getQuestionText()).isEqualTo("Qu'est-ce que Spring Boot ?");
            verify(questionService, times(1)).createQuestion(any(QuestionRequest.class));
        }

        @Test
        @DisplayName("Doit deleguer la creation au service")
        void shouldDelegateToService() {
            when(questionService.createQuestion(validRequest)).thenReturn(sampleQuestion);

            questionController.createQuestion(validRequest);

            verify(questionService).createQuestion(validRequest);
        }

        @Test
        @DisplayName("Doit propager l'exception si l'examen n'existe pas")
        void shouldPropagateExceptionWhenExamNotFound() {
            when(questionService.createQuestion(any()))
                    .thenThrow(new RuntimeException("Exam not found"));

            assertThatThrownBy(() -> questionController.createQuestion(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Exam not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getQuestionsByExam (legacy /api/questions/exam/{examId})
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/questions/exam/{examId} - Recuperation des questions (legacy)")
    class GetQuestionsByExam {

        @Test
        @DisplayName("Doit retourner les questions d'un examen avec 200")
        void shouldReturnQuestionsByExamWith200() {
            when(questionService.getQuestionsByExam(10L)).thenReturn(List.of(sampleQuestion));

            ResponseEntity<List<Question>> response = questionController.getQuestionsByExam(10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getId()).isEqualTo(1L);
            verify(questionService).getQuestionsByExam(10L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune question")
        void shouldReturnEmptyListWhenNoQuestions() {
            when(questionService.getQuestionsByExam(999L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<Question>> response = questionController.getQuestionsByExam(999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner plusieurs questions pour un examen")
        void shouldReturnMultipleQuestions() {
            Question q2 = new Question();
            q2.setId(2L);
            q2.setQuestionText("Qu'est-ce que JPA ?");
            when(questionService.getQuestionsByExam(10L)).thenReturn(List.of(sampleQuestion, q2));

            ResponseEntity<List<Question>> response = questionController.getQuestionsByExam(10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteQuestion (legacy /api/questions/{id})
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/questions/{id} - Suppression de question (legacy)")
    class DeleteQuestion {

        @Test
        @DisplayName("Doit supprimer une question et retourner 204")
        void shouldDeleteQuestionAndReturn204() {
            doNothing().when(questionService).deleteQuestion(1L);

            ResponseEntity<Void> response = questionController.deleteQuestion(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(questionService).deleteQuestion(1L);
        }

        @Test
        @DisplayName("Doit deleguer la suppression au service")
        void shouldDelegateToService() {
            doNothing().when(questionService).deleteQuestion(1L);

            questionController.deleteQuestion(1L);

            verify(questionService, times(1)).deleteQuestion(1L);
        }

        @Test
        @DisplayName("Doit propager l'exception si la question n'existe pas")
        void shouldPropagateExceptionWhenQuestionNotFound() {
            doThrow(new RuntimeException("Question not found: 999"))
                    .when(questionService).deleteQuestion(999L);

            assertThatThrownBy(() -> questionController.deleteQuestion(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getQuestionsByExamPath (/api/exams/{examId}/questions)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exams/{examId}/questions - Recuperation des questions (frontend)")
    class GetQuestionsByExamPath {

        @Test
        @DisplayName("Doit retourner les questions via le chemin frontend avec 200")
        void shouldReturnQuestionsViaFrontendPath() {
            when(questionService.getQuestionsByExam(10L)).thenReturn(List.of(sampleQuestion));

            ResponseEntity<List<Question>> response = questionController.getQuestionsByExamPath(10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            verify(questionService).getQuestionsByExam(10L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune question pour cet examen")
        void shouldReturnEmptyListWhenNoQuestions() {
            when(questionService.getQuestionsByExam(10L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<Question>> response = questionController.getQuestionsByExamPath(10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit deleguer au service avec l'examId correct")
        void shouldDelegateWithCorrectExamId() {
            when(questionService.getQuestionsByExam(10L)).thenReturn(Collections.emptyList());

            questionController.getQuestionsByExamPath(10L);

            verify(questionService).getQuestionsByExam(10L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createQuestionForExam (/api/exams/{examId}/questions)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exams/{examId}/questions - Creation de question (frontend)")
    class CreateQuestionForExam {

        @Test
        @DisplayName("Doit creer une question pour un examen et retourner 201")
        void shouldCreateQuestionForExamAndReturn201() {
            when(questionService.createQuestion(any(QuestionRequest.class))).thenReturn(sampleQuestion);

            ResponseEntity<Question> response = questionController.createQuestionForExam(10L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            verify(questionService).createQuestion(any(QuestionRequest.class));
        }

        @Test
        @DisplayName("Doit injecter l'examId dans la requete avant de deleguer au service")
        void shouldInjectExamIdIntoRequest() {
            QuestionRequest requestWithoutExamId = new QuestionRequest();
            requestWithoutExamId.setQuestionText("Question test");
            when(questionService.createQuestion(any())).thenReturn(sampleQuestion);

            questionController.createQuestionForExam(10L, requestWithoutExamId);

            assertThat(requestWithoutExamId.getExamId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Doit propager l'exception si l'examen n'existe pas")
        void shouldPropagateExceptionWhenExamNotFound() {
            when(questionService.createQuestion(any()))
                    .thenThrow(new RuntimeException("Exam not found"));

            assertThatThrownBy(() -> questionController.createQuestionForExam(999L, validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Exam not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateQuestion (/api/exams/{examId}/questions/{questionId})
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/exams/{examId}/questions/{questionId} - Mise a jour de question")
    class UpdateQuestion {

        @Test
        @DisplayName("Doit mettre a jour une question et retourner 200")
        void shouldUpdateQuestionAndReturn200() {
            when(questionService.updateQuestion(eq(1L), any(QuestionRequest.class))).thenReturn(sampleQuestion);

            ResponseEntity<Question> response = questionController.updateQuestion(10L, 1L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(questionService).updateQuestion(eq(1L), any(QuestionRequest.class));
        }

        @Test
        @DisplayName("Doit injecter l'examId dans la requete avant la mise a jour")
        void shouldInjectExamIdBeforeUpdate() {
            QuestionRequest req = new QuestionRequest();
            req.setQuestionText("Question modifiee");
            when(questionService.updateQuestion(any(), any())).thenReturn(sampleQuestion);

            questionController.updateQuestion(10L, 1L, req);

            assertThat(req.getExamId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Doit propager l'exception si la question n'existe pas")
        void shouldPropagateExceptionWhenQuestionNotFound() {
            when(questionService.updateQuestion(eq(999L), any()))
                    .thenThrow(new RuntimeException("Question not found: 999"));

            assertThatThrownBy(() -> questionController.updateQuestion(10L, 999L, validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteQuestionForExam (/api/exams/{examId}/questions/{questionId})
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/exams/{examId}/questions/{questionId} - Suppression (frontend)")
    class DeleteQuestionForExam {

        @Test
        @DisplayName("Doit supprimer une question via le chemin frontend et retourner 204")
        void shouldDeleteQuestionViaFrontendPathAndReturn204() {
            doNothing().when(questionService).deleteQuestion(1L);

            ResponseEntity<Void> response = questionController.deleteQuestionForExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(questionService).deleteQuestion(1L);
        }

        @Test
        @DisplayName("Doit utiliser le questionId (pas l'examId) pour la suppression")
        void shouldUseQuestionIdForDeletion() {
            doNothing().when(questionService).deleteQuestion(5L);

            questionController.deleteQuestionForExam(10L, 5L);

            verify(questionService).deleteQuestion(5L);
            verify(questionService, never()).deleteQuestion(10L);
        }

        @Test
        @DisplayName("Doit propager l'exception si la question n'existe pas")
        void shouldPropagateExceptionWhenQuestionNotFound() {
            doThrow(new RuntimeException("Question not found"))
                    .when(questionService).deleteQuestion(999L);

            assertThatThrownBy(() -> questionController.deleteQuestionForExam(10L, 999L))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
