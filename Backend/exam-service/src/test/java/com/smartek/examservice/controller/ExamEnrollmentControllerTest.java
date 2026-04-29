package com.smartek.examservice.controller;

import com.smartek.examservice.dto.LearnerExamResponse;
import com.smartek.examservice.service.ExamEnrollmentService;
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
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ExamEnrollmentController (sans Spring context).
 * Verifie la delegation au service et les codes HTTP retournes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExamEnrollmentController - Tests unitaires")
class ExamEnrollmentControllerTest {

    @Mock
    private ExamEnrollmentService examEnrollmentService;

    @InjectMocks
    private ExamEnrollmentController examEnrollmentController;

    private LearnerExamResponse sampleExamResponse;

    @BeforeEach
    void setUp() {
        sampleExamResponse = new LearnerExamResponse();
        sampleExamResponse.setId(1L);
        sampleExamResponse.setCourseId(5L);
        sampleExamResponse.setExamType("QUIZ");
        sampleExamResponse.setTitle("Quiz Spring Boot");
        sampleExamResponse.setIsLocked(false);
        sampleExamResponse.setHasAttempted(false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // unlockQuizForCourse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-enrollments/unlock-quiz")
    class UnlockQuizForCourse {

        @Test
        @DisplayName("Doit deverrouiller le quiz et retourner 200 avec message")
        void shouldUnlockQuizAndReturn200() {
            doNothing().when(examEnrollmentService).unlockQuizForCourse(1L, 5L);

            ResponseEntity<String> response = examEnrollmentController.unlockQuizForCourse(1L, 5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(examEnrollmentService, times(1)).unlockQuizForCourse(1L, 5L);
        }

        @Test
        @DisplayName("Doit deleguer l'appel au service avec les bons parametres")
        void shouldDelegateToServiceWithCorrectParams() {
            doNothing().when(examEnrollmentService).unlockQuizForCourse(42L, 10L);

            examEnrollmentController.unlockQuizForCourse(42L, 10L);

            verify(examEnrollmentService).unlockQuizForCourse(42L, 10L);
        }

        @Test
        @DisplayName("Doit propager l'exception si le quiz n'existe pas")
        void shouldPropagateExceptionWhenQuizNotFound() {
            doThrow(new RuntimeException("Aucun quiz trouve pour ce cours"))
                    .when(examEnrollmentService).unlockQuizForCourse(1L, 999L);

            assertThatThrownBy(() -> examEnrollmentController.unlockQuizForCourse(1L, 999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Aucun quiz");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // lockQuizForCourse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-enrollments/lock-quiz")
    class LockQuizForCourse {

        @Test
        @DisplayName("Doit reverrouiller le quiz et retourner 200")
        void shouldLockQuizAndReturn200() {
            doNothing().when(examEnrollmentService).lockQuizForCourse(1L, 5L);

            ResponseEntity<String> response = examEnrollmentController.lockQuizForCourse(1L, 5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(examEnrollmentService).lockQuizForCourse(1L, 5L);
        }

        @Test
        @DisplayName("Doit deleguer au service avec les bons parametres")
        void shouldDelegateToService() {
            doNothing().when(examEnrollmentService).lockQuizForCourse(2L, 7L);

            examEnrollmentController.lockQuizForCourse(2L, 7L);

            verify(examEnrollmentService).lockQuizForCourse(2L, 7L);
        }

        @Test
        @DisplayName("Doit propager l'exception si le service echoue")
        void shouldPropagateServiceException() {
            doThrow(new RuntimeException("Erreur interne"))
                    .when(examEnrollmentService).lockQuizForCourse(1L, 5L);

            assertThatThrownBy(() -> examEnrollmentController.lockQuizForCourse(1L, 5L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // enrollQuizForCourse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-enrollments/enroll-quiz")
    class EnrollQuizForCourse {

        @Test
        @DisplayName("Doit creer l'enrollment quiz et retourner 200")
        void shouldEnrollQuizAndReturn200() {
            doNothing().when(examEnrollmentService).createQuizEnrollmentForCourse(1L, 5L);

            ResponseEntity<String> response = examEnrollmentController.enrollQuizForCourse(1L, 5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(examEnrollmentService).createQuizEnrollmentForCourse(1L, 5L);
        }

        @Test
        @DisplayName("Doit deleguer la creation d'enrollment au service")
        void shouldDelegateEnrollmentCreation() {
            doNothing().when(examEnrollmentService).createQuizEnrollmentForCourse(3L, 8L);

            examEnrollmentController.enrollQuizForCourse(3L, 8L);

            verify(examEnrollmentService).createQuizEnrollmentForCourse(3L, 8L);
        }

        @Test
        @DisplayName("Doit propager l'exception si l'enrollment echoue")
        void shouldPropagateExceptionOnEnrollmentFailure() {
            doThrow(new RuntimeException("Enrollment deja existant"))
                    .when(examEnrollmentService).createQuizEnrollmentForCourse(1L, 5L);

            assertThatThrownBy(() -> examEnrollmentController.enrollQuizForCourse(1L, 5L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // unlockExamForTraining
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-enrollments/unlock-exam")
    class UnlockExamForTraining {

        @Test
        @DisplayName("Doit deverrouiller l'examen de formation et retourner 200")
        void shouldUnlockExamAndReturn200() {
            doNothing().when(examEnrollmentService).unlockExamForTraining(1L, 10L);

            ResponseEntity<String> response = examEnrollmentController.unlockExamForTraining(1L, 10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(examEnrollmentService).unlockExamForTraining(1L, 10L);
        }

        @Test
        @DisplayName("Doit deleguer au service avec userId et trainingId corrects")
        void shouldDelegateWithCorrectParams() {
            doNothing().when(examEnrollmentService).unlockExamForTraining(5L, 20L);

            examEnrollmentController.unlockExamForTraining(5L, 20L);

            verify(examEnrollmentService).unlockExamForTraining(5L, 20L);
        }

        @Test
        @DisplayName("Doit propager l'exception si aucun examen trouve pour la formation")
        void shouldPropagateExceptionWhenExamNotFound() {
            doThrow(new RuntimeException("Aucun examen trouve pour cette formation"))
                    .when(examEnrollmentService).unlockExamForTraining(1L, 999L);

            assertThatThrownBy(() -> examEnrollmentController.unlockExamForTraining(1L, 999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Aucun examen");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // enrollExamForTraining
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-enrollments/enroll-exam")
    class EnrollExamForTraining {

        @Test
        @DisplayName("Doit creer l'enrollment examen et retourner 200")
        void shouldEnrollExamAndReturn200() {
            doNothing().when(examEnrollmentService).createExamEnrollmentForTraining(1L, 10L);

            ResponseEntity<String> response = examEnrollmentController.enrollExamForTraining(1L, 10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(examEnrollmentService).createExamEnrollmentForTraining(1L, 10L);
        }

        @Test
        @DisplayName("Doit deleguer la creation d'enrollment examen au service")
        void shouldDelegateToService() {
            doNothing().when(examEnrollmentService).createExamEnrollmentForTraining(2L, 15L);

            examEnrollmentController.enrollExamForTraining(2L, 15L);

            verify(examEnrollmentService).createExamEnrollmentForTraining(2L, 15L);
        }

        @Test
        @DisplayName("Doit propager l'exception si la formation n'existe pas")
        void shouldPropagateExceptionWhenTrainingNotFound() {
            doThrow(new RuntimeException("Formation non trouvee"))
                    .when(examEnrollmentService).createExamEnrollmentForTraining(1L, 999L);

            assertThatThrownBy(() -> examEnrollmentController.enrollExamForTraining(1L, 999L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getMyExams
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exam-enrollments/my-exams")
    class GetMyExams {

        @Test
        @DisplayName("Doit retourner la liste des examens du learner avec 200")
        void shouldReturnMyExamsWithStatus200() {
            when(examEnrollmentService.getMyExams(1L)).thenReturn(List.of(sampleExamResponse));

            ResponseEntity<List<LearnerExamResponse>> response = examEnrollmentController.getMyExams(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTitle()).isEqualTo("Quiz Spring Boot");
            verify(examEnrollmentService).getMyExams(1L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun examen disponible")
        void shouldReturnEmptyListWhenNoExams() {
            when(examEnrollmentService.getMyExams(99L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<LearnerExamResponse>> response = examEnrollmentController.getMyExams(99L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner plusieurs examens pour un learner")
        void shouldReturnMultipleExams() {
            LearnerExamResponse exam2 = new LearnerExamResponse();
            exam2.setId(2L);
            exam2.setExamType("EXAM");
            exam2.setTitle("Examen Final Java");
            when(examEnrollmentService.getMyExams(1L)).thenReturn(List.of(sampleExamResponse, exam2));

            ResponseEntity<List<LearnerExamResponse>> response = examEnrollmentController.getMyExams(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // markExamAsCompleted
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-enrollments/complete")
    class MarkExamAsCompleted {

        @Test
        @DisplayName("Doit marquer l'examen comme complete et retourner 200")
        void shouldMarkExamAsCompletedAndReturn200() {
            doNothing().when(examEnrollmentService).markExamAsCompleted(1L, 10L);

            ResponseEntity<String> response = examEnrollmentController.markExamAsCompleted(1L, 10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(examEnrollmentService).markExamAsCompleted(1L, 10L);
        }

        @Test
        @DisplayName("Doit deleguer au service avec userId et examId corrects")
        void shouldDelegateWithCorrectParams() {
            doNothing().when(examEnrollmentService).markExamAsCompleted(5L, 20L);

            examEnrollmentController.markExamAsCompleted(5L, 20L);

            verify(examEnrollmentService).markExamAsCompleted(5L, 20L);
        }

        @Test
        @DisplayName("Doit propager l'exception si l'enrollment n'existe pas")
        void shouldPropagateExceptionWhenEnrollmentNotFound() {
            doThrow(new RuntimeException("Enrollment non trouve"))
                    .when(examEnrollmentService).markExamAsCompleted(1L, 999L);

            assertThatThrownBy(() -> examEnrollmentController.markExamAsCompleted(1L, 999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Enrollment non trouve");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // canStartExam
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exam-enrollments/can-start/{examId}")
    class CanStartExam {

        @Test
        @DisplayName("Doit retourner 200 si le learner peut commencer l'examen")
        void shouldReturn200WhenCanStart() {
            when(examEnrollmentService.canStartExam(1L, 10L)).thenReturn(true);

            ResponseEntity<?> response = examEnrollmentController.canStartExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examEnrollmentService).canStartExam(1L, 10L);
        }

        @Test
        @DisplayName("Doit retourner 403 si le learner ne peut pas commencer l'examen")
        void shouldReturn403WhenCannotStart() {
            when(examEnrollmentService.canStartExam(1L, 10L)).thenReturn(false);

            ResponseEntity<?> response = examEnrollmentController.canStartExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Doit retourner 403 si une exception est levee")
        void shouldReturn403WhenExceptionThrown() {
            when(examEnrollmentService.canStartExam(1L, 10L))
                    .thenThrow(new RuntimeException("Prerequis non remplis"));

            ResponseEntity<?> response = examEnrollmentController.canStartExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // startExam
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-enrollments/{examId}/start")
    class StartExam {

        @Test
        @DisplayName("Doit demarrer l'examen et retourner 200")
        void shouldStartExamAndReturn200() {
            doNothing().when(examEnrollmentService).startExam(1L, 10L);

            ResponseEntity<?> response = examEnrollmentController.startExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examEnrollmentService).startExam(1L, 10L);
        }

        @Test
        @DisplayName("Doit retourner 400 si l'examen est verrouille")
        void shouldReturn400WhenExamLocked() {
            doThrow(new RuntimeException("Cet examen est verrouille"))
                    .when(examEnrollmentService).startExam(1L, 10L);

            ResponseEntity<?> response = examEnrollmentController.startExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Doit retourner 400 si l'enrollment n'existe pas")
        void shouldReturn400WhenEnrollmentNotFound() {
            doThrow(new RuntimeException("Enrollment non trouve"))
                    .when(examEnrollmentService).startExam(1L, 999L);

            ResponseEntity<?> response = examEnrollmentController.startExam(999L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTimeRemaining
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exam-enrollments/{examId}/time-remaining")
    class GetTimeRemaining {

        @Test
        @DisplayName("Doit retourner le temps restant avec 200")
        void shouldReturnTimeRemainingWith200() {
            when(examEnrollmentService.getTimeRemaining(1L, 10L)).thenReturn(45);

            ResponseEntity<?> response = examEnrollmentController.getTimeRemaining(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examEnrollmentService).getTimeRemaining(1L, 10L);
        }

        @Test
        @DisplayName("Doit retourner 400 si l'enrollment n'existe pas")
        void shouldReturn400WhenEnrollmentNotFound() {
            when(examEnrollmentService.getTimeRemaining(1L, 999L))
                    .thenThrow(new RuntimeException("Enrollment non trouve"));

            ResponseEntity<?> response = examEnrollmentController.getTimeRemaining(999L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Doit retourner 0 si le temps est ecoule")
        void shouldReturnZeroWhenTimeExpired() {
            when(examEnrollmentService.getTimeRemaining(1L, 10L)).thenReturn(0);

            ResponseEntity<?> response = examEnrollmentController.getTimeRemaining(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // pauseExam / resumeExam
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-enrollments/{examId}/pause et /resume")
    class PauseResumeExam {

        @Test
        @DisplayName("Doit mettre en pause l'examen et retourner 200")
        void shouldPauseExamAndReturn200() {
            doNothing().when(examEnrollmentService).pauseExam(1L, 10L);

            ResponseEntity<?> response = examEnrollmentController.pauseExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examEnrollmentService).pauseExam(1L, 10L);
        }

        @Test
        @DisplayName("Doit retourner 400 si la mise en pause echoue")
        void shouldReturn400WhenPauseFails() {
            doThrow(new RuntimeException("Impossible de mettre en pause"))
                    .when(examEnrollmentService).pauseExam(1L, 10L);

            ResponseEntity<?> response = examEnrollmentController.pauseExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Doit reprendre l'examen et retourner 200")
        void shouldResumeExamAndReturn200() {
            doNothing().when(examEnrollmentService).resumeExam(1L, 10L);

            ResponseEntity<?> response = examEnrollmentController.resumeExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examEnrollmentService).resumeExam(1L, 10L);
        }

        @Test
        @DisplayName("Doit retourner 400 si la reprise echoue")
        void shouldReturn400WhenResumeFails() {
            doThrow(new RuntimeException("Impossible de reprendre"))
                    .when(examEnrollmentService).resumeExam(1L, 10L);

            ResponseEntity<?> response = examEnrollmentController.resumeExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Endpoints path-variable
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Endpoints path-variable (training et course)")
    class PathVariableEndpoints {

        @Test
        @DisplayName("GET /training/{trainingId}/user/{userId} - Doit retourner 200")
        void shouldReturnOkForGetEnrollmentForTraining() {
            ResponseEntity<String> response = examEnrollmentController.getEnrollmentForTraining(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("OK");
        }

        @Test
        @DisplayName("POST /training/{trainingId}/user/{userId} - Doit creer l'enrollment et retourner 200")
        void shouldEnrollForTrainingAndReturn200() {
            doNothing().when(examEnrollmentService).createExamEnrollmentForTraining(1L, 10L);

            ResponseEntity<String> response = examEnrollmentController.enrollForTraining(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examEnrollmentService).createExamEnrollmentForTraining(1L, 10L);
        }

        @Test
        @DisplayName("PUT /training/{trainingId}/user/{userId}/unlock - Doit deverrouiller et retourner 200")
        void shouldUnlockTrainingExamAndReturn200() {
            doNothing().when(examEnrollmentService).unlockExamForTraining(1L, 10L);

            ResponseEntity<String> response = examEnrollmentController.unlockTrainingExam(10L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examEnrollmentService).unlockExamForTraining(1L, 10L);
        }

        @Test
        @DisplayName("POST /course/{courseId}/user/{userId} - Doit creer l'enrollment quiz et retourner 200")
        void shouldEnrollForCourseAndReturn200() {
            doNothing().when(examEnrollmentService).createQuizEnrollmentForCourse(1L, 5L);

            ResponseEntity<String> response = examEnrollmentController.enrollForCourse(5L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examEnrollmentService).createQuizEnrollmentForCourse(1L, 5L);
        }

        @Test
        @DisplayName("PUT /course/{courseId}/user/{userId}/unlock - Doit deverrouiller le quiz et retourner 200")
        void shouldUnlockCourseQuizAndReturn200() {
            doNothing().when(examEnrollmentService).unlockQuizForCourse(1L, 5L);

            ResponseEntity<String> response = examEnrollmentController.unlockCourseQuiz(5L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examEnrollmentService).unlockQuizForCourse(1L, 5L);
        }
    }
}
