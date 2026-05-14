package com.smartek.examservice.controller;

import com.smartek.examservice.dto.QuestionRequest;
import com.smartek.examservice.entity.Question;
import com.smartek.examservice.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    // ── Endpoints legacy /api/questions ──────────────────────────────────────

    @PostMapping("/api/questions")
    public ResponseEntity<Question> createQuestion(@RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.createQuestion(request));
    }

    @GetMapping("/api/questions/exam/{examId}")
    public ResponseEntity<List<Question>> getQuestionsByExam(@PathVariable Long examId) {
        return ResponseEntity.ok(questionService.getQuestionsByExam(examId));
    }

    @DeleteMapping("/api/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    // ── Endpoints frontend /api/exams/{examId}/questions ─────────────────────

    /**
     * GET /api/exams/{examId}/questions
     * Récupère les questions d'un examen (chemin attendu par le frontend)
     */
    @GetMapping("/api/exams/{examId}/questions")
    public ResponseEntity<List<Question>> getQuestionsByExamPath(@PathVariable Long examId) {
        return ResponseEntity.ok(questionService.getQuestionsByExam(examId));
    }

    /**
     * POST /api/exams/{examId}/questions
     * Crée une question pour un examen (chemin attendu par le frontend)
     */
    @PostMapping("/api/exams/{examId}/questions")
    public ResponseEntity<Question> createQuestionForExam(
            @PathVariable Long examId,
            @RequestBody QuestionRequest request) {
        request.setExamId(examId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.createQuestion(request));
    }

    /**
     * PUT /api/exams/{examId}/questions/{questionId}
     * Met à jour une question (endpoint manquant côté backend)
     */
    @PutMapping("/api/exams/{examId}/questions/{questionId}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable Long examId,
            @PathVariable Long questionId,
            @RequestBody QuestionRequest request) {
        request.setExamId(examId);
        return ResponseEntity.ok(questionService.updateQuestion(questionId, request));
    }

    /**
     * DELETE /api/exams/{examId}/questions/{questionId}
     * Supprime une question (chemin attendu par le frontend)
     */
    @DeleteMapping("/api/exams/{examId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestionForExam(
            @PathVariable Long examId,
            @PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
}
