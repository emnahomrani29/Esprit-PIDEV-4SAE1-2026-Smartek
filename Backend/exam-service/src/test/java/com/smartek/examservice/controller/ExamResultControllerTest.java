package com.smartek.examservice.controller;

import com.smartek.examservice.dto.*;
import com.smartek.examservice.entity.ExamResult;
import com.smartek.examservice.entity.ExerciseAnswer;
import com.smartek.examservice.repository.ExamResultRepository;
import com.smartek.examservice.repository.ExerciseAnswerRepository;
import com.smartek.examservice.service.ExamResultService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ExamResultController (sans Spring context).
 * Verifie la delegation au service et les codes HTTP retournes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExamResultController - Tests unitaires")
class ExamResultControllerTest {

    @Mock
    private ExamResultService examResultService;

    @Mock
    private ExamResultRepository examResultRepository;

    @Mock
    private ExerciseAnswerRepository exerciseAnswerRepository;

    @InjectMocks
    private ExamResultController examResultController;

    private ExamResultResponse sampleResultResponse;
    private ExamSubmissionDTO sampleSubmissionDTO;
    private ExamSubmissionRequest sampleSubmissionRequest;

    @BeforeEach
    void setUp() {
        sampleResultResponse = new ExamResultResponse();
        sampleResultResponse.setId(1L);
        sampleResultResponse.setExamId(10L);
        sampleResultResponse.setExamTitle("Quiz Spring Boot");
        sampleResultResponse.setUserId(5L);
        sampleResultResponse.setObtainedMarks(80);
        sampleResultResponse.setTotalMarks(100);
        sampleResultResponse.setPercentage(80.0);
        sampleResultResponse.setPassed(true);
        sampleResultResponse.setSubmittedAt(LocalDateTime.now());
        sampleResultResponse.setIsCorrected(true);

        sampleSubmissionDTO = new ExamSubmissionDTO();
        sampleSubmissionDTO.setExamId(10L);
        sampleSubmissionDTO.setUserId(5L);
        sampleSubmissionDTO.setTimeTaken(45);
        sampleSubmissionDTO.setAnswers(Collections.emptyList());

        sampleSubmissionRequest = new ExamSubmissionRequest();
        sampleSubmissionRequest.setExamId(10L);
        sampleSubmissionRequest.setUserId(5L);
        sampleSubmissionRequest.setTimeTaken(45);
        sampleSubmissionRequest.setAnswers(Collections.emptyList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // submitExam
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-results/submit")
    class SubmitExam {

        @Test
        @DisplayName("Doit soumettre l'examen et retourner 200 avec le resultat")
        void shouldSubmitExamAndReturn200() {
            when(examResultService.submitExam(any(ExamSubmissionDTO.class))).thenReturn(sampleResultResponse);

            ResponseEntity<ExamResultResponse> response = examResultController.submitExam(sampleSubmissionDTO);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getPassed()).isTrue();
            verify(examResultService).submitExam(sampleSubmissionDTO);
        }

        @Test
        @DisplayName("Doit deleguer la soumission au service")
        void shouldDelegateToService() {
            when(examResultService.submitExam(any())).thenReturn(sampleResultResponse);

            examResultController.submitExam(sampleSubmissionDTO);

            verify(examResultService, times(1)).submitExam(any(ExamSubmissionDTO.class));
        }

        @Test
        @DisplayName("Doit propager l'exception si l'examen n'existe pas")
        void shouldPropagateExceptionWhenExamNotFound() {
            when(examResultService.submitExam(any()))
                    .thenThrow(new RuntimeException("Exam not found"));

            assertThatThrownBy(() -> examResultController.submitExam(sampleSubmissionDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Exam not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // submitExamOld
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exam-results/submit-old")
    class SubmitExamOld {

        @Test
        @DisplayName("Doit soumettre l'examen (ancien format) et retourner 200")
        void shouldSubmitExamOldAndReturn200() {
            when(examResultService.submitExamOld(any(ExamSubmissionRequest.class))).thenReturn(sampleResultResponse);

            ResponseEntity<ExamResultResponse> response = examResultController.submitExamOld(sampleSubmissionRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getExamId()).isEqualTo(10L);
            verify(examResultService).submitExamOld(sampleSubmissionRequest);
        }

        @Test
        @DisplayName("Doit deleguer au service avec l'ancien format de requete")
        void shouldDelegateToServiceWithOldFormat() {
            when(examResultService.submitExamOld(any())).thenReturn(sampleResultResponse);

            examResultController.submitExamOld(sampleSubmissionRequest);

            verify(examResultService).submitExamOld(any(ExamSubmissionRequest.class));
        }

        @Test
        @DisplayName("Doit propager l'exception si la question n'existe pas")
        void shouldPropagateExceptionWhenQuestionNotFound() {
            when(examResultService.submitExamOld(any()))
                    .thenThrow(new RuntimeException("Question not found"));

            assertThatThrownBy(() -> examResultController.submitExamOld(sampleSubmissionRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Question not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getResultsByUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exam-results/user/{userId}")
    class GetResultsByUser {

        @Test
        @DisplayName("Doit retourner les resultats d'un utilisateur avec 200")
        void shouldReturnResultsByUserWith200() {
            when(examResultService.getResultsByUser(5L)).thenReturn(List.of(sampleResultResponse));

            ResponseEntity<List<ExamResultResponse>> response = examResultController.getResultsByUser(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getUserId()).isEqualTo(5L);
            verify(examResultService).getResultsByUser(5L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun resultat")
        void shouldReturnEmptyListWhenNoResults() {
            when(examResultService.getResultsByUser(99L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<ExamResultResponse>> response = examResultController.getResultsByUser(99L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner plusieurs resultats pour un utilisateur")
        void shouldReturnMultipleResults() {
            ExamResultResponse result2 = new ExamResultResponse();
            result2.setId(2L);
            result2.setUserId(5L);
            result2.setPassed(false);
            when(examResultService.getResultsByUser(5L)).thenReturn(List.of(sampleResultResponse, result2));

            ResponseEntity<List<ExamResultResponse>> response = examResultController.getResultsByUser(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getResultsByExam
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exam-results/exam/{examId}")
    class GetResultsByExam {

        @Test
        @DisplayName("Doit retourner les resultats d'un examen avec 200")
        void shouldReturnResultsByExamWith200() {
            when(examResultService.getResultsByExam(10L)).thenReturn(List.of(sampleResultResponse));

            ResponseEntity<List<ExamResultResponse>> response = examResultController.getResultsByExam(10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getExamId()).isEqualTo(10L);
            verify(examResultService).getResultsByExam(10L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun resultat pour cet examen")
        void shouldReturnEmptyListWhenNoResultsForExam() {
            when(examResultService.getResultsByExam(999L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<ExamResultResponse>> response = examResultController.getResultsByExam(999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit deleguer au service avec l'examId correct")
        void shouldDelegateWithCorrectExamId() {
            when(examResultService.getResultsByExam(10L)).thenReturn(List.of(sampleResultResponse));

            examResultController.getResultsByExam(10L);

            verify(examResultService).getResultsByExam(10L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getResultById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exam-results/{resultId}")
    class GetResultById {

        @Test
        @DisplayName("Doit retourner le resultat par ID avec 200")
        void shouldReturnResultByIdWith200() {
            when(examResultService.getResultById(1L)).thenReturn(sampleResultResponse);

            ResponseEntity<ExamResultResponse> response = examResultController.getResultById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            verify(examResultService).getResultById(1L);
        }

        @Test
        @DisplayName("Doit propager l'exception si le resultat n'existe pas")
        void shouldPropagateExceptionWhenResultNotFound() {
            when(examResultService.getResultById(999L))
                    .thenThrow(new RuntimeException("Result not found"));

            assertThatThrownBy(() -> examResultController.getResultById(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Result not found");
        }

        @Test
        @DisplayName("Doit deleguer au service avec le resultId correct")
        void shouldDelegateWithCorrectResultId() {
            when(examResultService.getResultById(1L)).thenReturn(sampleResultResponse);

            examResultController.getResultById(1L);

            verify(examResultService).getResultById(1L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserAnswers
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exam-results/{resultId}/answers")
    class GetUserAnswers {

        @Test
        @DisplayName("Doit retourner les reponses de l'utilisateur avec 200")
        void shouldReturnUserAnswersWith200() {
            UserAnswerResponse answer = new UserAnswerResponse();
            answer.setId(1L);
            answer.setQuestionId(100L);
            answer.setIsCorrect(true);
            answer.setMarksObtained(5);
            when(examResultService.getUserAnswers(1L)).thenReturn(List.of(answer));

            ResponseEntity<List<UserAnswerResponse>> response = examResultController.getUserAnswers(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getIsCorrect()).isTrue();
            verify(examResultService).getUserAnswers(1L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune reponse")
        void shouldReturnEmptyListWhenNoAnswers() {
            when(examResultService.getUserAnswers(1L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<UserAnswerResponse>> response = examResultController.getUserAnswers(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit propager l'exception si le resultat n'existe pas")
        void shouldPropagateExceptionWhenResultNotFound() {
            when(examResultService.getUserAnswers(999L))
                    .thenThrow(new RuntimeException("Result not found"));

            assertThatThrownBy(() -> examResultController.getUserAnswers(999L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTrainerAnalytics
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exam-results/trainer/{trainerId}/analytics")
    class GetTrainerAnalytics {

        @Test
        @DisplayName("Doit retourner les analytics du trainer avec 200")
        void shouldReturnTrainerAnalyticsWith200() {
            TrainerExamAnalyticsResponse analytics = new TrainerExamAnalyticsResponse();
            analytics.setExamId(10L);
            analytics.setExamTitle("Quiz Spring Boot");
            analytics.setLearnerId(5L);
            analytics.setScore(80);
            analytics.setStatus("passed");
            when(examResultService.getTrainerExamAnalytics(20L)).thenReturn(List.of(analytics));

            ResponseEntity<List<TrainerExamAnalyticsResponse>> response =
                    examResultController.getTrainerAnalytics(20L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getStatus()).isEqualTo("passed");
            verify(examResultService).getTrainerExamAnalytics(20L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun resultat pour ce trainer")
        void shouldReturnEmptyListWhenNoAnalytics() {
            when(examResultService.getTrainerExamAnalytics(99L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<TrainerExamAnalyticsResponse>> response =
                    examResultController.getTrainerAnalytics(99L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit deleguer au service avec le trainerId correct")
        void shouldDelegateWithCorrectTrainerId() {
            when(examResultService.getTrainerExamAnalytics(20L)).thenReturn(Collections.emptyList());

            examResultController.getTrainerAnalytics(20L);

            verify(examResultService).getTrainerExamAnalytics(20L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPendingCorrections
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exam-results/pending")
    class GetPendingCorrections {

        @Test
        @DisplayName("Doit retourner les resultats non corriges avec 200")
        void shouldReturnPendingCorrectionsWithStatus200() {
            ExamResult pendingResult = new ExamResult();
            pendingResult.setId(1L);
            pendingResult.setIsCorrected(false);
            when(examResultRepository.findAll()).thenReturn(List.of(pendingResult));
            when(examResultService.mapToResultResponsePublic(pendingResult)).thenReturn(sampleResultResponse);
            sampleResultResponse.setIsCorrected(false);

            ResponseEntity<List<ExamResultResponse>> response = examResultController.getPendingCorrections();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(examResultRepository).findAll();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si tous les resultats sont corriges")
        void shouldReturnEmptyListWhenAllCorrected() {
            ExamResult correctedResult = new ExamResult();
            correctedResult.setId(1L);
            correctedResult.setIsCorrected(true);
            when(examResultRepository.findAll()).thenReturn(List.of(correctedResult));

            ResponseEntity<List<ExamResultResponse>> response = examResultController.getPendingCorrections();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun resultat en base")
        void shouldReturnEmptyListWhenNoResults() {
            when(examResultRepository.findAll()).thenReturn(Collections.emptyList());

            ResponseEntity<List<ExamResultResponse>> response = examResultController.getPendingCorrections();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // finalizeCorrection
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/exam-results/{resultId}/finalize")
    class FinalizeCorrection {

        @Test
        @DisplayName("Doit finaliser la correction et retourner 200")
        void shouldFinalizeCorrectionAndReturn200() {
            ExamResult result = new ExamResult();
            result.setId(1L);
            result.setIsCorrected(false);
            when(examResultRepository.findById(1L)).thenReturn(Optional.of(result));
            when(examResultRepository.save(any(ExamResult.class))).thenReturn(result);
            when(examResultService.mapToResultResponsePublic(any())).thenReturn(sampleResultResponse);

            ResponseEntity<ExamResultResponse> response = examResultController.finalizeCorrection(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getIsCorrected()).isTrue();
            verify(examResultRepository).save(result);
        }

        @Test
        @DisplayName("Doit propager l'exception si le resultat n'existe pas")
        void shouldPropagateExceptionWhenResultNotFound() {
            when(examResultRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> examResultController.finalizeCorrection(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Result not found: 999");
        }

        @Test
        @DisplayName("Doit mettre a jour isCorrected et correctedAt lors de la finalisation")
        void shouldSetIsCorrectedAndCorrectedAt() {
            ExamResult result = new ExamResult();
            result.setId(1L);
            result.setIsCorrected(false);
            when(examResultRepository.findById(1L)).thenReturn(Optional.of(result));
            when(examResultRepository.save(any())).thenReturn(result);
            when(examResultService.mapToResultResponsePublic(any())).thenReturn(sampleResultResponse);

            examResultController.finalizeCorrection(1L);

            assertThat(result.getIsCorrected()).isTrue();
            assertThat(result.getCorrectedAt()).isNotNull();
        }
    }
}
