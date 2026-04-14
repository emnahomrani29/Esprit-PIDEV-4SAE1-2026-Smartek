package com.smartek.examservice.controller;

import com.smartek.examservice.dto.*;
import com.smartek.examservice.dto.LearnerExamResponse;
import com.smartek.examservice.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;

    @PostMapping
    public ResponseEntity<ExamResponse> createExam(@RequestBody ExamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(examService.createExam(request));
    }

    @GetMapping
    public ResponseEntity<List<ExamResponse>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponse> getExamById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ExamResponse>> getExamsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(examService.getExamsByCourse(courseId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamResponse> updateExam(
            @PathVariable Long id,
            @RequestBody ExamRequest request) {
        return ResponseEntity.ok(examService.updateExam(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/by-training/{trainingId}")
    public ResponseEntity<Void> deleteExamsByTrainingId(@PathVariable Long trainingId) {
        examService.deleteExamsByTrainingId(trainingId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/by-course/{courseId}")
    public ResponseEntity<Void> deleteQuizzesByCourseId(@PathVariable Long courseId) {
        examService.deleteQuizzesByCourseId(courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/learner/{userId}")
    public ResponseEntity<List<LearnerExamResponse>> getLearnerExams(@PathVariable Long userId) {
        return ResponseEntity.ok(examService.getLearnerExams(userId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Exam Service is running");
    }
}
