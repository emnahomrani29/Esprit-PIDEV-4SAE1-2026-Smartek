package com.smartek.skillevidenceservice.controller;

import com.smartek.skillevidenceservice.dto.SkillEvidenceRequest;
import com.smartek.skillevidenceservice.dto.SkillEvidenceResponse;
import com.smartek.skillevidenceservice.service.SkillEvidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skill-evidence")
@RequiredArgsConstructor
public class SkillEvidenceController {

    private final SkillEvidenceService service;

    @PostMapping
    public ResponseEntity<SkillEvidenceResponse> create(
            @Valid @RequestBody SkillEvidenceRequest request) {
        SkillEvidenceResponse response = service.createEvidence(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SkillEvidenceResponse>> getAll() {
        return ResponseEntity.ok(service.getAllEvidence());
    }

    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<SkillEvidenceResponse>> getByLearner(
            @PathVariable Long learnerId) {
        return ResponseEntity.ok(service.getAllEvidenceByLearner(learnerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillEvidenceResponse> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getEvidenceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillEvidenceResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody SkillEvidenceRequest request) {
        return ResponseEntity.ok(service.updateEvidence(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteEvidence(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('RH_SMARTEK')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<SkillEvidenceResponse> approveEvidence(
            @PathVariable Integer id,
            @Valid @RequestBody com.smartek.skillevidenceservice.dto.ApprovalRequest request) {
        // Extraire le reviewerId depuis le token JWT (via SecurityContext)
        Long reviewerId = getAuthenticatedUserId();
        if (reviewerId == null) reviewerId = request.getReviewerId() != null ? request.getReviewerId() : 1L;

        com.smartek.skillevidenceservice.entity.SkillEvidence evidence =
            service.approveEvidence(id, request.getScore(), reviewerId, request.getAdminComment());

        return ResponseEntity.ok(mapToResponse(evidence));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('RH_SMARTEK')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<SkillEvidenceResponse> rejectEvidence(
            @PathVariable Integer id,
            @Valid @RequestBody com.smartek.skillevidenceservice.dto.RejectionRequest request) {
        Long reviewerId = getAuthenticatedUserId();
        if (reviewerId == null) reviewerId = request.getReviewerId() != null ? request.getReviewerId() : 1L;

        com.smartek.skillevidenceservice.entity.SkillEvidence evidence =
            service.rejectEvidence(id, request.getAdminComment(), reviewerId);

        return ResponseEntity.ok(mapToResponse(evidence));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('RH_SMARTEK')")
    @PutMapping("/{id}/review")
    public ResponseEntity<SkillEvidenceResponse> reviewEvidence(
            @PathVariable Integer id,
            @Valid @RequestBody com.smartek.skillevidenceservice.dto.ReviewRequest request) {
        Long reviewerId = getAuthenticatedUserId();
        if (reviewerId == null) reviewerId = request.getReviewerId() != null ? request.getReviewerId() : 1L;

        com.smartek.skillevidenceservice.entity.SkillEvidence evidence =
            service.reviewEvidence(id, request.getStatus(), request.getScore(),
                                 request.getAdminComment(), reviewerId);

        return ResponseEntity.ok(mapToResponse(evidence));
    }

    // ========== ANALYTICS ENDPOINTS ==========

    @GetMapping("/analytics/learner/{learnerId}")
    public ResponseEntity<com.smartek.skillevidenceservice.dto.LearnerAnalyticsDTO> getLearnerAnalytics(
            @PathVariable Long learnerId) {
        return ResponseEntity.ok(service.getLearnerAnalytics(learnerId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('RH_SMARTEK')")
    @GetMapping("/analytics/global")
    public ResponseEntity<com.smartek.skillevidenceservice.dto.GlobalAnalyticsDTO> getGlobalAnalytics() {
        return ResponseEntity.ok(service.getGlobalAnalyticsNew());
    }

    // ========== FILTER ENDPOINTS ==========

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SkillEvidenceResponse>> getEvidenceByStatus(@PathVariable String status) {
        try {
            com.smartek.skillevidenceservice.entity.EvidenceStatus evidenceStatus = 
                com.smartek.skillevidenceservice.entity.EvidenceStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(service.getEvidencesByStatus(evidenceStatus));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<SkillEvidenceResponse>> getEvidenceByCategory(@PathVariable String category) {
        try {
            com.smartek.skillevidenceservice.entity.EvidenceCategory evidenceCategory = 
                com.smartek.skillevidenceservice.entity.EvidenceCategory.valueOf(category.toUpperCase());
            return ResponseEntity.ok(service.getEvidencesByCategory(evidenceCategory));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category value: " + category);
        }
    }

    private SkillEvidenceResponse mapToResponse(com.smartek.skillevidenceservice.entity.SkillEvidence e) {
        return new SkillEvidenceResponse(
                e.getEvidenceId(),
                e.getTitle(),
                e.getFileUrl(),
                e.getDescription(),
                e.getUploadDate(),
                e.getLearnerId(),
                e.getLearnerName(),
                e.getLearnerEmail(),
                e.getStatus(),
                e.getScore(),
                e.getAdminComment(),
                e.getReviewedBy(),
                e.getReviewedAt(),
                e.getCategory()
        );
    }

    // Extraire l'userId depuis le token JWT via le SecurityContext
    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Long) {
            return (Long) auth.getDetails();
        }
        return null;
    }
}