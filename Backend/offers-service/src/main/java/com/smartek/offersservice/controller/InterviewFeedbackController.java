package com.smartek.offersservice.controller;

import com.smartek.offersservice.dto.InterviewFeedbackRequest;
import com.smartek.offersservice.dto.InterviewFeedbackResponse;
import com.smartek.offersservice.service.InterviewFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview-feedbacks")
@RequiredArgsConstructor
public class InterviewFeedbackController {

    private final InterviewFeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<InterviewFeedbackResponse> submitFeedback(
            @Valid @RequestBody InterviewFeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.submitFeedback(request));
    }

    @GetMapping("/interview/{interviewId}")
    public ResponseEntity<InterviewFeedbackResponse> getFeedbackByInterview(@PathVariable Long interviewId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByInterview(interviewId));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<InterviewFeedbackResponse>> getFeedbacksByApplication(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByApplication(applicationId));
    }
}
