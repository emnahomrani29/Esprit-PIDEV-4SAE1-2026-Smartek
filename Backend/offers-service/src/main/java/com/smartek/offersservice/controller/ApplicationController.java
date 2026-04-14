package com.smartek.offersservice.controller;

import com.smartek.offersservice.dto.ApplicationRequest;
import com.smartek.offersservice.dto.ApplicationResponse;
import com.smartek.offersservice.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    
    private final ApplicationService applicationService;
    
    @PostMapping
    public ResponseEntity<ApplicationResponse> applyToOffer(@Valid @RequestBody ApplicationRequest request) {
        try {
            ApplicationResponse response = applicationService.applyToOffer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/offer/{offerId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByOffer(@PathVariable Long offerId) {
        List<ApplicationResponse> applications = applicationService.getApplicationsByOffer(offerId);
        return ResponseEntity.ok(applications);
    }
    
    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByLearner(@PathVariable Long learnerId) {
        List<ApplicationResponse> applications = applicationService.getApplicationsByLearner(learnerId);
        return ResponseEntity.ok(applications);
    }
    
    @GetMapping("/check/{offerId}/{learnerId}")
    public ResponseEntity<Boolean> hasApplied(@PathVariable Long offerId, @PathVariable Long learnerId) {
        boolean hasApplied = applicationService.hasApplied(offerId, learnerId);
        return ResponseEntity.ok(hasApplied);
    }
    
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @RequestParam String status) {
        ApplicationResponse response = applicationService.updateApplicationStatus(applicationId, status);
        return ResponseEntity.ok(response);
    }
}
