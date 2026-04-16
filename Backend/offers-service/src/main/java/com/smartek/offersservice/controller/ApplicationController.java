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
        return ResponseEntity.ok(applicationService.getApplicationsByOffer(offerId));
    }

    @GetMapping("/offer/{offerId}/ranked")
    public ResponseEntity<List<ApplicationResponse>> getRankedApplications(@PathVariable Long offerId) {
        return ResponseEntity.ok(applicationService.getApplicationsByOfferSortedByScore(offerId));
    }

    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByLearner(@PathVariable Long learnerId) {
        return ResponseEntity.ok(applicationService.getApplicationsByLearner(learnerId));
    }

    @GetMapping("/check/{offerId}/{learnerId}")
    public ResponseEntity<Map<String, Boolean>> hasApplied(@PathVariable Long offerId, @PathVariable Long learnerId) {
        return ResponseEntity.ok(Map.of("hasApplied", applicationService.hasApplied(offerId, learnerId)));
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @RequestParam String status,
            @RequestParam(required = false) String recruiterNote) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(applicationId, status, recruiterNote));
    }

    @PutMapping("/{applicationId}/withdraw")
    public ResponseEntity<ApplicationResponse> withdrawApplication(
            @PathVariable Long applicationId,
            @RequestParam Long learnerId) {
        return ResponseEntity.ok(applicationService.withdrawApplication(applicationId, learnerId));
    }
}
