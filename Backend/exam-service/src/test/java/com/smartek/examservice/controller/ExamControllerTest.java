package com.smartek.examservice.controller;

import com.smartek.examservice.dto.ExamRequest;
import com.smartek.examservice.dto.ExamResponse;
import com.smartek.examservice.service.ExamService;
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
 * Tests unitaires pour ExamController (sans Spring context).
 * Vérifie la logique de délégation au service et les codes HTTP retournés.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExamController - Tests unitaires")
class ExamControllerTest {

    @Mock
    private ExamService examService;

    @InjectMocks
    private ExamController examController;

    private ExamRequest validRequest;
    private ExamResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ExamRequest();
        validRequest.setCourseId(5L);
        validRequest.setExamType("QUIZ");
        validRequest.setTitle("Quiz Spring Boot");
        validRequest.setDuration(60);
        validRequest.setPassingScore(70);
        validRequest.setTotalMarks(100);
        validRequest.setIsActive(true);
        validRequest.setCreatedBy(10L);

        sampleResponse = new ExamResponse();
        sampleResponse.setId(1L);
        sampleResponse.setCourseId(5L);
        sampleResponse.setExamType("QUIZ");
        sampleResponse.setTitle("Quiz Spring Boot");
        sampleResponse.setDuration(60);
        sampleResponse.setPassingScore(70);
        sampleResponse.setTotalMarks(100);
        sampleResponse.setIsActive(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createExam
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createExam()")
    class CreateExam {

        @Test
        @DisplayName("Doit créer un examen et retourner 201")
        void shouldCreateExamAndReturn201() {
            when(examService.createExam(any())).thenReturn(sampleResponse);

            ResponseEntity<ExamResponse> response = examController.createExam(validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getTitle()).isEqualTo("Quiz Spring Boot");
            verify(examService, times(1)).createExam(any());
        }

        @Test
        @DisplayName("Doit déléguer la création au service")
        void shouldDelegateToService() {
            when(examService.createExam(validRequest)).thenReturn(sampleResponse);

            examController.createExam(validRequest);

            verify(examService).createExam(validRequest);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllExams
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllExams()")
    class GetAllExams {

        @Test
        @DisplayName("Doit retourner la liste des examens avec 200")
        void shouldReturnAllExams() {
            when(examService.getAllExams()).thenReturn(List.of(sampleResponse));

            ResponseEntity<List<ExamResponse>> response = examController.getAllExams();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTitle()).isEqualTo("Quiz Spring Boot");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun examen")
        void shouldReturnEmptyList() {
            when(examService.getAllExams()).thenReturn(Collections.emptyList());

            ResponseEntity<List<ExamResponse>> response = examController.getAllExams();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getExamById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getExamById()")
    class GetExamById {

        @Test
        @DisplayName("Doit retourner l'examen par ID avec 200")
        void shouldReturnExamById() {
            when(examService.getExamById(1L)).thenReturn(sampleResponse);

            ResponseEntity<ExamResponse> response = examController.getExamById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit propager l'exception si l'examen n'existe pas")
        void shouldPropagateExceptionWhenNotFound() {
            when(examService.getExamById(99L))
                    .thenThrow(new RuntimeException("Exam not found with id: 99"));

            assertThatThrownBy(() -> examController.getExamById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateExam
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateExam()")
    class UpdateExam {

        @Test
        @DisplayName("Doit mettre à jour un examen et retourner 200")
        void shouldUpdateExamAndReturn200() {
            when(examService.updateExam(eq(1L), any())).thenReturn(sampleResponse);

            ResponseEntity<ExamResponse> response = examController.updateExam(1L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(examService).updateExam(eq(1L), any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteExam
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteExam()")
    class DeleteExam {

        @Test
        @DisplayName("Doit supprimer un examen et retourner 204")
        void shouldDeleteExamAndReturn204() {
            doNothing().when(examService).deleteExam(1L);

            ResponseEntity<Void> response = examController.deleteExam(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(examService).deleteExam(1L);
        }

        @Test
        @DisplayName("Doit propager l'exception si l'examen n'existe pas")
        void shouldPropagateExceptionWhenNotFound() {
            doThrow(new RuntimeException("Examen non trouvé avec l'ID: 99"))
                    .when(examService).deleteExam(99L);

            assertThatThrownBy(() -> examController.deleteExam(99L))
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
        @DisplayName("Doit retourner les examens d'un cours avec 200")
        void shouldReturnExamsByCourse() {
            when(examService.getExamsByCourse(5L)).thenReturn(List.of(sampleResponse));

            ResponseEntity<List<ExamResponse>> response = examController.getExamsByCourse(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getCourseId()).isEqualTo(5L);
        }
    }
}
