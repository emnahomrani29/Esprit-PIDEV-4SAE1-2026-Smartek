package com.smartek.certificationbadgeservice.controller;

import com.smartek.certificationbadgeservice.dto.ExamProcessingResultDTO;
import com.smartek.certificationbadgeservice.dto.ExamResultDTO;
import com.smartek.certificationbadgeservice.service.ExamIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for exam service integration.
 * Provides endpoints for the exam service to trigger automatic certification and badge awards.
 */
@RestController
@RequestMapping("/api/certifications-badges/exam-integration")
@RequiredArgsConstructor
@Slf4j
public class ExamIntegrationController {
    
    private final ExamIntegrationService examIntegrationService;
    
    /**
     * Process exam results and automatically award certifications and badges.
     * This endpoint should be called by the exam service after an exam is graded.
     * 
     * Rules:
     * - Score >= 60%: Award certification
     * - Score >= 60%: Award badge (if badge template exists for the course)
     * 
     * @param examResult the exam result data
     * @return processing result with details of what was awarded
     */
    @PostMapping("/process-exam-result")
    public ResponseEntity<ExamProcessingResultDTO> processExamResult(
            @Valid @RequestBody ExamResultDTO examResult) {
        log.info("Received exam result for learner {} on exam {}", 
                examResult.getLearnerId(), examResult.getExamId());
        
        ExamProcessingResultDTO result = examIntegrationService.processExamResult(examResult);
        
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    
    /**
     * Health check endpoint for exam service integration.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Exam integration service is running");
    }
}
