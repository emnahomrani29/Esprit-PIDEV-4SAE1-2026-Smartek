package com.smartek.examservice.controller;

import com.smartek.examservice.dto.*;
import com.smartek.examservice.entity.ExerciseAnswer;
import com.smartek.examservice.repository.ExamResultRepository;
import com.smartek.examservice.repository.ExerciseAnswerRepository;
import com.smartek.examservice.service.ExamResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/exam-results")
@RequiredArgsConstructor
public class ExamResultController {
    private final ExamResultService examResultService;
    private final ExamResultRepository examResultRepository;
    private final ExerciseAnswerRepository exerciseAnswerRepository;

    @PostMapping("/submit")
    public ResponseEntity<ExamResultResponse> submitExam(
            @RequestBody ExamSubmissionDTO request) {
        return ResponseEntity.ok(examResultService.submitExam(request));
    }

    @PostMapping("/submit-old")
    public ResponseEntity<ExamResultResponse> submitExamOld(
            @RequestBody ExamSubmissionRequest request) {
        return ResponseEntity.ok(examResultService.submitExamOld(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExamResultResponse>> getResultsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(examResultService.getResultsByUser(userId));
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<ExamResultResponse>> getResultsByExam(@PathVariable Long examId) {
        return ResponseEntity.ok(examResultService.getResultsByExam(examId));
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<ExamResultResponse> getResultById(@PathVariable Long resultId) {
        return ResponseEntity.ok(examResultService.getResultById(resultId));
    }

    @GetMapping("/{resultId}/answers")
    public ResponseEntity<List<UserAnswerResponse>> getUserAnswers(@PathVariable Long resultId) {
        return ResponseEntity.ok(examResultService.getUserAnswers(resultId));
    }

    @GetMapping("/trainer/{trainerId}/analytics")
    public ResponseEntity<List<TrainerExamAnalyticsResponse>> getTrainerAnalytics(@PathVariable Long trainerId) {
        return ResponseEntity.ok(examResultService.getTrainerExamAnalytics(trainerId));
    }

    // ── Correction manuelle (EXAM type) ──────────────────────────────────────

    /**
     * GET /api/exam-results/pending
     * Récupère tous les résultats d'examens non encore corrigés manuellement.
     * Utilisé par le trainer pour la correction.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ExamResultResponse>> getPendingCorrections() {
        List<ExamResultResponse> pending = examResultRepository
                .findAll()
                .stream()
                .filter(r -> Boolean.FALSE.equals(r.getIsCorrected()))
                .map(examResultService::mapToResultResponsePublic)
                .toList();
        return ResponseEntity.ok(pending);
    }

    /**
     * PUT /api/exam-results/{resultId}/finalize
     * Finalise la correction d'un examen (marque isCorrected = true).
     */
    @PutMapping("/{resultId}/finalize")
    public ResponseEntity<ExamResultResponse> finalizeCorrection(@PathVariable Long resultId) {
        var result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Result not found: " + resultId));
        result.setIsCorrected(true);
        result.setCorrectedAt(LocalDateTime.now());
        examResultRepository.save(result);
        return ResponseEntity.ok(examResultService.mapToResultResponsePublic(result));
    }
}

// ── Correction des réponses d'exercices ──────────────────────────────────────

@RestController
@RequestMapping("/api/exams/exercise-answers")
@RequiredArgsConstructor
class ExerciseAnswerCorrectionController {

    private final ExerciseAnswerRepository exerciseAnswerRepository;

    /**
     * PUT /api/exams/exercise-answers/{answerId}/correct
     * Corrige une réponse d'exercice (attribue des points et un feedback).
     * Attendu par le frontend : correctExercise(answerId, marks, feedback)
     */
    @PutMapping("/{answerId}/correct")
    public ResponseEntity<ExerciseAnswer> correctExercise(
            @PathVariable Long answerId,
            @RequestBody CorrectionRequest request) {
        ExerciseAnswer answer = exerciseAnswerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("ExerciseAnswer not found: " + answerId));
        answer.setMarksObtained(request.getMarksObtained());
        answer.setTrainerFeedback(request.getTrainerFeedback());
        answer.setIsCorrected(true);
        return ResponseEntity.ok(exerciseAnswerRepository.save(answer));
    }
}
