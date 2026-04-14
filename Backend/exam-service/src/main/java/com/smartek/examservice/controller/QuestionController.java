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
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.createQuestion(request));
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<Question>> getQuestionsByExam(@PathVariable Long examId) {
        return ResponseEntity.ok(questionService.getQuestionsByExam(examId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
