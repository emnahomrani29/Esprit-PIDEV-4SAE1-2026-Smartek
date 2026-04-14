package com.smartek.examservice.controller;

import com.smartek.examservice.dto.*;
import com.smartek.examservice.service.ExamResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exam-results")
@RequiredArgsConstructor
public class ExamResultController {
    private final ExamResultService examResultService;

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
}
