package com.smartek.examservice.service;

import com.smartek.examservice.client.UserClient;
import com.smartek.examservice.dto.ExamResultResponse;
import com.smartek.examservice.dto.ExamSubmissionDTO;
import com.smartek.examservice.entity.*;
import com.smartek.examservice.repository.*;
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
 * Tests unitaires pour ExamResultService.
 * Couvre la logique de correction automatique des QCM, calcul du score,
 * et la gestion des résultats.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExamResultService - Logique de correction")
class ExamResultServiceTest {

    @Mock
    private ExamResultRepository examResultRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ExamEnrollmentRepository examEnrollmentRepository;

    @Mock
    private ExamDraftService examDraftService;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private ExamResultService examResultService;

    private Exam sampleExam;
    private Question mcqQuestion;
    private Question trueFalseQuestion;
    private Question shortAnswerQuestion;

    @BeforeEach
    void setUp() {
        sampleExam = new Exam();
        sampleExam.setId(1L);
        sampleExam.setTitle("Quiz Spring Boot");
        sampleExam.setTotalMarks(30);
        sampleExam.setPassingScore(70); // 70%
        sampleExam.setDuration(60);
        sampleExam.setCreatedAt(LocalDateTime.now());

        // Question QCM avec 4 options (option B est correcte)
        mcqQuestion = new Question();
        mcqQuestion.setId(1L);
        mcqQuestion.setQuestionText("Qu'est-ce que Spring Boot ?");
        mcqQuestion.setQuestionType("MULTIPLE_CHOICE");
        mcqQuestion.setMarks(10);

        QuestionOption optA = new QuestionOption(1L, mcqQuestion, "Un framework CSS", false);
        QuestionOption optB = new QuestionOption(2L, mcqQuestion, "Un framework Java", true);
        QuestionOption optC = new QuestionOption(3L, mcqQuestion, "Un langage de programmation", false);
        QuestionOption optD = new QuestionOption(4L, mcqQuestion, "Un serveur web", false);
        mcqQuestion.setOptions(List.of(optA, optB, optC, optD));

        // Question Vrai/Faux (Vrai est correct)
        trueFalseQuestion = new Question();
        trueFalseQuestion.setId(2L);
        trueFalseQuestion.setQuestionText("Spring Boot simplifie la configuration ?");
        trueFalseQuestion.setQuestionType("TRUE_FALSE");
        trueFalseQuestion.setMarks(10);

        QuestionOption vraiOpt = new QuestionOption(5L, trueFalseQuestion, "Vrai", true);
        QuestionOption fauxOpt = new QuestionOption(6L, trueFalseQuestion, "Faux", false);
        trueFalseQuestion.setOptions(List.of(vraiOpt, fauxOpt));

        // Question réponse courte
        shortAnswerQuestion = new Question();
        shortAnswerQuestion.setId(3L);
        shortAnswerQuestion.setQuestionText("Quel est le port par défaut de Spring Boot ?");
        shortAnswerQuestion.setQuestionType("SHORT_ANSWER");
        shortAnswerQuestion.setMarks(10);
        shortAnswerQuestion.setCorrectAnswer("8080");
        shortAnswerQuestion.setOptions(new ArrayList<>());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // submitExam - Correction automatique
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("submitExam() - Correction automatique")
    class SubmitExam {

        @Test
        @DisplayName("Doit calculer le score correct quand toutes les réponses sont bonnes")
        void shouldCalculatePerfectScoreWhenAllAnswersCorrect() {
            // Réponse correcte : option index 1 (optB = "Un framework Java")
            ExamSubmissionDTO.AnswerDTO answer1 = new ExamSubmissionDTO.AnswerDTO();
            answer1.setQuestionId(1L);
            answer1.setSelectedOptions(List.of(1)); // index 1 = optB (correct)

            ExamSubmissionDTO submission = new ExamSubmissionDTO();
            submission.setExamId(1L);
            submission.setUserId(5L);
            submission.setTimeTaken(30);
            submission.setAnswers(List.of(answer1));

            sampleExam.setTotalMarks(10);

            when(examRepository.findById(1L)).thenReturn(Optional.of(sampleExam));
            when(questionRepository.findById(1L)).thenReturn(Optional.of(mcqQuestion));
            when(examResultRepository.save(any(ExamResult.class))).thenAnswer(inv -> {
                ExamResult r = inv.getArgument(0);
                r.setId(100L);
                return r;
            });
            when(examEnrollmentRepository.findByUserIdAndExamId(5L, 1L)).thenReturn(Optional.empty());

            ExamResultResponse result = examResultService.submitExam(submission);

            assertThat(result.getObtainedMarks()).isEqualTo(10);
            assertThat(result.getPercentage()).isEqualTo(100.0);
            assertThat(result.getPassed()).isTrue();
        }

        @Test
        @DisplayName("Doit calculer un score de 0 quand toutes les réponses sont fausses")
        void shouldCalculateZeroScoreWhenAllAnswersWrong() {
            // Réponse incorrecte : option index 0 (optA = "Un framework CSS")
            ExamSubmissionDTO.AnswerDTO answer1 = new ExamSubmissionDTO.AnswerDTO();
            answer1.setQuestionId(1L);
            answer1.setSelectedOptions(List.of(0)); // index 0 = optA (incorrect)

            ExamSubmissionDTO submission = new ExamSubmissionDTO();
            submission.setExamId(1L);
            submission.setUserId(5L);
            submission.setTimeTaken(30);
            submission.setAnswers(List.of(answer1));

            sampleExam.setTotalMarks(10);

            when(examRepository.findById(1L)).thenReturn(Optional.of(sampleExam));
            when(questionRepository.findById(1L)).thenReturn(Optional.of(mcqQuestion));
            when(examResultRepository.save(any(ExamResult.class))).thenAnswer(inv -> {
                ExamResult r = inv.getArgument(0);
                r.setId(100L);
                return r;
            });
            when(examEnrollmentRepository.findByUserIdAndExamId(5L, 1L)).thenReturn(Optional.empty());

            ExamResultResponse result = examResultService.submitExam(submission);

            assertThat(result.getObtainedMarks()).isZero();
            assertThat(result.getPercentage()).isEqualTo(0.0);
            assertThat(result.getPassed()).isFalse();
        }

        @Test
        @DisplayName("Doit valider une réponse Vrai/Faux correcte via texte")
        void shouldValidateTrueFalseAnswerByText() {
            ExamSubmissionDTO.AnswerDTO answer = new ExamSubmissionDTO.AnswerDTO();
            answer.setQuestionId(2L);
            answer.setSelectedAnswer("Vrai"); // correct

            ExamSubmissionDTO submission = new ExamSubmissionDTO();
            submission.setExamId(1L);
            submission.setUserId(5L);
            submission.setTimeTaken(10);
            submission.setAnswers(List.of(answer));

            sampleExam.setTotalMarks(10);

            when(examRepository.findById(1L)).thenReturn(Optional.of(sampleExam));
            when(questionRepository.findById(2L)).thenReturn(Optional.of(trueFalseQuestion));
            when(examResultRepository.save(any(ExamResult.class))).thenAnswer(inv -> {
                ExamResult r = inv.getArgument(0);
                r.setId(101L);
                return r;
            });
            when(examEnrollmentRepository.findByUserIdAndExamId(5L, 1L)).thenReturn(Optional.empty());

            ExamResultResponse result = examResultService.submitExam(submission);

            assertThat(result.getObtainedMarks()).isEqualTo(10);
            assertThat(result.getPassed()).isTrue();
        }

        @Test
        @DisplayName("Doit normaliser 'true' en 'Vrai' pour les questions Vrai/Faux")
        void shouldNormalizeTrueToVraiForTrueFalseQuestions() {
            ExamSubmissionDTO.AnswerDTO answer = new ExamSubmissionDTO.AnswerDTO();
            answer.setQuestionId(2L);
            answer.setSelectedAnswer("true"); // doit être normalisé en "Vrai"

            ExamSubmissionDTO submission = new ExamSubmissionDTO();
            submission.setExamId(1L);
            submission.setUserId(5L);
            submission.setTimeTaken(10);
            submission.setAnswers(List.of(answer));

            sampleExam.setTotalMarks(10);

            when(examRepository.findById(1L)).thenReturn(Optional.of(sampleExam));
            when(questionRepository.findById(2L)).thenReturn(Optional.of(trueFalseQuestion));
            when(examResultRepository.save(any(ExamResult.class))).thenAnswer(inv -> {
                ExamResult r = inv.getArgument(0);
                r.setId(102L);
                return r;
            });
            when(examEnrollmentRepository.findByUserIdAndExamId(5L, 1L)).thenReturn(Optional.empty());

            ExamResultResponse result = examResultService.submitExam(submission);

            // "true" normalisé en "Vrai" → réponse correcte
            assertThat(result.getObtainedMarks()).isEqualTo(10);
        }

        @Test
        @DisplayName("Doit valider une réponse courte (insensible à la casse)")
        void shouldValidateShortAnswerCaseInsensitive() {
            ExamSubmissionDTO.AnswerDTO answer = new ExamSubmissionDTO.AnswerDTO();
            answer.setQuestionId(3L);
            answer.setSelectedAnswer("8080"); // correct

            ExamSubmissionDTO submission = new ExamSubmissionDTO();
            submission.setExamId(1L);
            submission.setUserId(5L);
            submission.setTimeTaken(5);
            submission.setAnswers(List.of(answer));

            sampleExam.setTotalMarks(10);

            when(examRepository.findById(1L)).thenReturn(Optional.of(sampleExam));
            when(questionRepository.findById(3L)).thenReturn(Optional.of(shortAnswerQuestion));
            when(examResultRepository.save(any(ExamResult.class))).thenAnswer(inv -> {
                ExamResult r = inv.getArgument(0);
                r.setId(103L);
                return r;
            });
            when(examEnrollmentRepository.findByUserIdAndExamId(5L, 1L)).thenReturn(Optional.empty());

            ExamResultResponse result = examResultService.submitExam(submission);

            assertThat(result.getObtainedMarks()).isEqualTo(10);
        }

        @Test
        @DisplayName("Doit lever RuntimeException si l'examen n'existe pas")
        void shouldThrowExceptionWhenExamNotFound() {
            ExamSubmissionDTO submission = new ExamSubmissionDTO();
            submission.setExamId(99L);
            submission.setUserId(5L);
            submission.setAnswers(Collections.emptyList());

            when(examRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> examResultService.submitExam(submission))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Exam not found");
        }

        @Test
        @DisplayName("Doit marquer l'enrollment comme complété après soumission")
        void shouldMarkEnrollmentAsCompletedAfterSubmission() {
            ExamSubmissionDTO.AnswerDTO answer = new ExamSubmissionDTO.AnswerDTO();
            answer.setQuestionId(1L);
            answer.setSelectedOptions(List.of(1));

            ExamSubmissionDTO submission = new ExamSubmissionDTO();
            submission.setExamId(1L);
            submission.setUserId(5L);
            submission.setTimeTaken(30);
            submission.setAnswers(List.of(answer));

            sampleExam.setTotalMarks(10);

            ExamEnrollment enrollment = new ExamEnrollment();
            enrollment.setId(1L);
            enrollment.setUserId(5L);
            enrollment.setIsCompleted(false);

            when(examRepository.findById(1L)).thenReturn(Optional.of(sampleExam));
            when(questionRepository.findById(1L)).thenReturn(Optional.of(mcqQuestion));
            when(examResultRepository.save(any(ExamResult.class))).thenAnswer(inv -> {
                ExamResult r = inv.getArgument(0);
                r.setId(100L);
                return r;
            });
            when(examEnrollmentRepository.findByUserIdAndExamId(5L, 1L))
                    .thenReturn(Optional.of(enrollment));
            when(examEnrollmentRepository.save(any(ExamEnrollment.class))).thenReturn(enrollment);

            examResultService.submitExam(submission);

            verify(examEnrollmentRepository).save(argThat(e -> e.getIsCompleted()));
        }

        @Test
        @DisplayName("Doit supprimer le brouillon après soumission")
        void shouldDeleteDraftAfterSubmission() {
            ExamSubmissionDTO.AnswerDTO answer = new ExamSubmissionDTO.AnswerDTO();
            answer.setQuestionId(1L);
            answer.setSelectedOptions(List.of(1));

            ExamSubmissionDTO submission = new ExamSubmissionDTO();
            submission.setExamId(1L);
            submission.setUserId(5L);
            submission.setTimeTaken(30);
            submission.setAnswers(List.of(answer));

            sampleExam.setTotalMarks(10);

            when(examRepository.findById(1L)).thenReturn(Optional.of(sampleExam));
            when(questionRepository.findById(1L)).thenReturn(Optional.of(mcqQuestion));
            when(examResultRepository.save(any(ExamResult.class))).thenAnswer(inv -> {
                ExamResult r = inv.getArgument(0);
                r.setId(100L);
                return r;
            });
            when(examEnrollmentRepository.findByUserIdAndExamId(5L, 1L)).thenReturn(Optional.empty());

            examResultService.submitExam(submission);

            verify(examDraftService).deleteDraft(1L, 5L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getResultsByUser
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getResultsByUser()")
    class GetResultsByUser {

        @Test
        @DisplayName("Doit retourner les résultats d'un utilisateur")
        void shouldReturnResultsByUser() {
            ExamResult result = new ExamResult();
            result.setId(1L);
            result.setExam(sampleExam);
            result.setUserId(5L);
            result.setObtainedMarks(80);
            result.setTotalMarks(100);
            result.setPercentage(80.0);
            result.setPassed(true);
            result.setSubmittedAt(LocalDateTime.now());
            result.setTimeTaken(45);
            result.setIsCorrected(true);

            when(examResultRepository.findByUserId(5L)).thenReturn(List.of(result));

            List<ExamResultResponse> results = examResultService.getResultsByUser(5L);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getUserId()).isEqualTo(5L);
            assertThat(results.get(0).getPassed()).isTrue();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si l'utilisateur n'a aucun résultat")
        void shouldReturnEmptyListWhenNoResults() {
            when(examResultRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

            List<ExamResultResponse> results = examResultService.getResultsByUser(99L);

            assertThat(results).isEmpty();
        }
    }
}
