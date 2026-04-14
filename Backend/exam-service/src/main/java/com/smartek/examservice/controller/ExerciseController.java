package com.smartek.examservice.controller;

import com.smartek.examservice.dto.ExerciseRequest;
import com.smartek.examservice.dto.ExerciseResponse;
import com.smartek.examservice.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseService exerciseService;

    @PostMapping("/{examId}/exercises")
    public ResponseEntity<ExerciseResponse> createExercise(
            @PathVariable Long examId,
            @RequestBody ExerciseRequest request) {
        request.setExamId(examId);
        ExerciseResponse response = exerciseService.createExercise(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{examId}/exercises")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByExam(@PathVariable Long examId) {
        List<ExerciseResponse> exercises = exerciseService.getExercisesByExam(examId);
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/exercises/{id}")
    public ResponseEntity<ExerciseResponse> getExerciseById(@PathVariable Long id) {
        ExerciseResponse exercise = exerciseService.getExerciseById(id);
        return ResponseEntity.ok(exercise);
    }

    @PutMapping("/{examId}/exercises/{id}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @PathVariable Long examId,
            @PathVariable Long id,
            @RequestBody ExerciseRequest request) {
        request.setExamId(examId);
        ExerciseResponse response = exerciseService.updateExercise(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{examId}/exercises/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long examId, @PathVariable Long id) {
        exerciseService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }
}
