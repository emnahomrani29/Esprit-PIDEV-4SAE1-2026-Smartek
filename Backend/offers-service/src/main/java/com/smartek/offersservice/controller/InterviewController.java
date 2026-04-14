package com.smartek.offersservice.controller;

import com.smartek.offersservice.dto.InterviewRequest;
import com.smartek.offersservice.dto.InterviewResponse;
import com.smartek.offersservice.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Slf4j
public class InterviewController {
    
    private final InterviewService interviewService;
    
    @PostMapping
    public ResponseEntity<InterviewResponse> createInterview(@RequestBody InterviewRequest request) {
        log.info("POST /api/interviews - Creating interview for application: {}", request.getApplicationId());
        try {
            InterviewResponse response = interviewService.createInterview(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error creating interview: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<InterviewResponse>> getAllInterviews() {
        log.info("GET /api/interviews - Fetching all interviews");
        List<InterviewResponse> interviews = interviewService.getAllInterviews();
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByCompany(@PathVariable Long companyId) {
        log.info("GET /api/interviews/company/{} - Fetching interviews for company", companyId);
        return ResponseEntity.ok(interviewService.getInterviewsByCompany(companyId));
    }

    @GetMapping("/offer/{offerId}")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByOffer(@PathVariable Long offerId) {
        log.info("GET /api/interviews/offer/{} - Fetching interviews for offer", offerId);
        List<InterviewResponse> interviews = interviewService.getInterviewsByOffer(offerId);
        return ResponseEntity.ok(interviews);
    }
    
    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByLearner(@PathVariable Long learnerId) {
        log.info("GET /api/interviews/learner/{} - Fetching interviews for learner", learnerId);
        List<InterviewResponse> interviews = interviewService.getInterviewsByLearner(learnerId);
        return ResponseEntity.ok(interviews);
    }
    
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByApplication(@PathVariable Long applicationId) {
        log.info("GET /api/interviews/application/{} - Fetching interviews for application", applicationId);
        List<InterviewResponse> interviews = interviewService.getInterviewsByApplication(applicationId);
        return ResponseEntity.ok(interviews);
    }
    
    @PutMapping("/{interviewId}/status")
    public ResponseEntity<InterviewResponse> updateInterviewStatus(
            @PathVariable Long interviewId,
            @RequestParam String status) {
        log.info("PUT /api/interviews/{}/status - Updating status to: {}", interviewId, status);
        try {
            InterviewResponse response = interviewService.updateInterviewStatus(interviewId, status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating interview status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{interviewId}")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable Long interviewId,
            @RequestBody InterviewRequest request) {
        log.info("PUT /api/interviews/{} - Updating interview", interviewId);
        try {
            InterviewResponse response = interviewService.updateInterview(interviewId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating interview: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{interviewId}")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long interviewId) {
        log.info("DELETE /api/interviews/{} - Deleting interview", interviewId);
        try {
            interviewService.deleteInterview(interviewId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting interview: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
