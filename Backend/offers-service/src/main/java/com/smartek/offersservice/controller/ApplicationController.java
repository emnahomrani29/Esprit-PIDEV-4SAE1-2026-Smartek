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
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    
    private final ApplicationService applicationService;
    
    @PostMapping
    public ResponseEntity<ApplicationResponse> applyToOffer(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.applyToOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<Map<String, Boolean>> hasApplied(@PathVariable Long offerId, @PathVariable Long learnerId) {
        boolean hasApplied = applicationService.hasApplied(offerId, learnerId);
        return ResponseEntity.ok(Map.of("hasApplied", hasApplied));
    }
    
    @GetMapping("/offer/{offerId}/ranked")
    public ResponseEntity<List<ApplicationResponse>> getRankedApplications(@PathVariable Long offerId) {
        return ResponseEntity.ok(applicationService.getApplicationsByOfferSortedByScore(offerId));
    }

    @PutMapping("/{applicationId}/withdraw")
    public ResponseEntity<ApplicationResponse> withdrawApplication(
            @PathVariable Long applicationId,
            @RequestParam Long learnerId) {
        return ResponseEntity.ok(applicationService.withdrawApplication(applicationId, learnerId));
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @RequestParam String status,
            @RequestParam(required = false) String recruiterNote) {
        ApplicationResponse response = applicationService.updateApplicationStatus(applicationId, status, recruiterNote);
        return ResponseEntity.ok(response);
    }
}
