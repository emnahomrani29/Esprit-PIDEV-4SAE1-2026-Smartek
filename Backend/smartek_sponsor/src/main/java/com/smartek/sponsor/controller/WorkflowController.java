package com.smartek.sponsor.controller;

import com.smartek.sponsor.dto.ApprovalRequest;
import com.smartek.sponsor.dto.StatusSummaryDTO;
import com.smartek.sponsor.dto.SponsorshipResponseDTO;
import com.smartek.sponsor.dto.ExpiringContractDTO;
import com.smartek.sponsor.entity.Sponsorship;
import com.smartek.sponsor.mapper.SponsorshipMapper;
import com.smartek.sponsor.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/workflow")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WorkflowController {
    
    private final WorkflowService workflowService;
    private final SponsorshipMapper sponsorshipMapper;
    
    /**
     * Admin approves a sponsorship
     */
    @PostMapping("/sponsorships/{id}/approve")
    public ResponseEntity<SponsorshipResponseDTO> approveSponsorship(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        Sponsorship approved = workflowService.approveSponsorship(id, adminId);
        return ResponseEntity.ok(sponsorshipMapper.toResponseDTO(approved));
    }
    
    /**
     * Admin rejects a sponsorship with reason
     */
    @PostMapping("/sponsorships/{id}/reject")
    public ResponseEntity<SponsorshipResponseDTO> rejectSponsorship(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        Sponsorship rejected = workflowService.rejectSponsorship(id, request.getReason(), request.getAdminId());
        return ResponseEntity.ok(sponsorshipMapper.toResponseDTO(rejected));
    }
    
    /**
     * Sponsor cancels a pending sponsorship
     */
    @PostMapping("/sponsorships/{id}/cancel")
    public ResponseEntity<SponsorshipResponseDTO> cancelSponsorship(@PathVariable Long id) {
        Sponsorship cancelled = workflowService.cancelSponsorship(id);
        return ResponseEntity.ok(sponsorshipMapper.toResponseDTO(cancelled));
    }
    
    /**
     * Get all pending sponsorships (for admin)
     */
    @GetMapping("/sponsorships/pending")
    public ResponseEntity<List<SponsorshipResponseDTO>> getPendingSponsorships() {
        List<Sponsorship> pending = workflowService.getPendingSponsorships();
        List<SponsorshipResponseDTO> dtos = pending.stream()
                .map(sponsorshipMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Get count of pending sponsorships (for badge)
     */
    @GetMapping("/sponsorships/pending/count")
    public ResponseEntity<Long> getPendingCount() {
        Long count = workflowService.getPendingCount();
        return ResponseEntity.ok(count);
    }
    
    /**
     * Get count of urgent pending sponsorships (pending for more than 24 hours)
     */
    @GetMapping("/sponsorships/urgent-pending/count")
    public ResponseEntity<Long> getUrgentPendingCount() {
        Long count = workflowService.getUrgentPendingCount();
        return ResponseEntity.ok(count);
    }
    
    /**
     * Get status summary for a sponsor
     */
    @GetMapping("/sponsorships/status-summary/{sponsorId}")
    public ResponseEntity<StatusSummaryDTO> getStatusSummary(@PathVariable Long sponsorId) {
        StatusSummaryDTO summary = workflowService.getStatusSummary(sponsorId);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get recent pending sponsorships (last 24 hours)
     */
    @GetMapping("/sponsorships/recent-pending")
    public ResponseEntity<List<SponsorshipResponseDTO>> getRecentPending() {
        List<Sponsorship> recent = workflowService.getRecentPending();
        List<SponsorshipResponseDTO> dtos = recent.stream()
                .map(sponsorshipMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Get contracts expiring within 30 days
     */
    @GetMapping("/contracts/expiring")
    public ResponseEntity<List<ExpiringContractDTO>> getExpiringContracts() {
        List<ExpiringContractDTO> expiring = workflowService.getExpiringContracts();
        return ResponseEntity.ok(expiring);
    }
    
    /**
     * Get count of expiring contracts for a specific sponsor
     */
    @GetMapping("/contracts/expiring/count/{sponsorId}")
    public ResponseEntity<Long> getSponsorExpiringContractsCount(@PathVariable Long sponsorId) {
        Long count = workflowService.getSponsorExpiringContractsCount(sponsorId);
        return ResponseEntity.ok(count);
    }
}
