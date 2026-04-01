package com.smartek.certificationbadgeservice.controller;

import com.smartek.certificationbadgeservice.dto.AwardCertificationRequestDTO;
import com.smartek.certificationbadgeservice.dto.BulkAwardCertificationRequestDTO;
import com.smartek.certificationbadgeservice.dto.BulkAwardResponseDTO;
import com.smartek.certificationbadgeservice.dto.EarnedCertificationDTO;
import com.smartek.certificationbadgeservice.security.AuthorizationService;
import com.smartek.certificationbadgeservice.service.EarnedCertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing earned certifications.
 * Provides endpoints for awarding certifications and querying earned certifications.
 */
@RestController
@RequestMapping("/api/certifications-badges/earned-certifications")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Slf4j
public class EarnedCertificationController {
    
    private final EarnedCertificationService earnedCertificationService;
    private final AuthorizationService authorizationService;
    
    /**
     * Award a certification to a single learner.
     * Only accessible by TRAINER and ADMIN roles.
     * The awardedBy field is automatically extracted from the JWT token.
     */
    @PostMapping
    public ResponseEntity<EarnedCertificationDTO> awardCertification(
            @Valid @RequestBody AwardCertificationRequestDTO request) {
        // Extract the current user ID from JWT SecurityContext
        Long awardedBy = authorizationService.getCurrentUserId();
        request.setAwardedBy(awardedBy);
        
        log.info("Awarding certification template {} to learner {} by user {}", 
                request.getCertificationTemplateId(), request.getLearnerId(), awardedBy);
        
        EarnedCertificationDTO earnedCertification = earnedCertificationService.awardCertification(request);
        
        log.info("Certification awarded successfully with id: {}", earnedCertification.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(earnedCertification);
    }
    
    /**
     * Award a certification to multiple learners.
     * Only accessible by TRAINER and ADMIN roles.
     * The awardedBy field is automatically extracted from the JWT token.
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkAwardResponseDTO> bulkAwardCertifications(
            @Valid @RequestBody BulkAwardCertificationRequestDTO request) {
        // Extract the current user ID from JWT SecurityContext
        Long awardedBy = authorizationService.getCurrentUserId();
        request.setAwardedBy(awardedBy);
        
        log.info("Bulk awarding certification template {} to {} learners by user {}", 
                request.getCertificationTemplateId(), request.getLearnerIds().size(), awardedBy);
        
        BulkAwardResponseDTO response = earnedCertificationService.bulkAwardCertifications(request);
        
        log.info("Bulk award completed: {} successful, {} failed", 
                response.getSuccessCount(), response.getFailureCount());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get earned certifications for a specific learner.
     * Accessible by:
     * - The learner themselves (can only access their own data)
     * - TRAINER (can access any learner's data)
     * - ADMIN (can access any learner's data)
     * - RH_COMPANY or RH_SMARTEK (can access any learner's data)
     */
    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<EarnedCertificationDTO>> getEarnedCertificationsByLearner(@PathVariable Long learnerId) {
        log.info("Retrieving earned certifications for learner: {}", learnerId);
        
        // Check if the current user has permission to access this learner's data
        if (!authorizationService.canAccessLearnerData(learnerId)) {
            log.warn("User {} attempted to access certifications for learner {} without permission", 
                    authorizationService.getCurrentUserId(), learnerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<EarnedCertificationDTO> earnedCertifications = earnedCertificationService.findByLearnerId(learnerId);
        log.info("Retrieved {} earned certifications for learner {}", earnedCertifications.size(), learnerId);
        return ResponseEntity.ok(earnedCertifications);
    }
    
    /**
     * Get earned certifications for a specific learner with pagination.
     */
    @GetMapping("/learner/{learnerId}/paginated")
    public ResponseEntity<Page<EarnedCertificationDTO>> getEarnedCertificationsByLearnerPaginated(
            @PathVariable Long learnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "issueDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Retrieving paginated earned certifications for learner: {} - page: {}, size: {}", 
                learnerId, page, size);
        
        // Check if the current user has permission to access this learner's data
        if (!authorizationService.canAccessLearnerData(learnerId)) {
            log.warn("User {} attempted to access certifications for learner {} without permission", 
                    authorizationService.getCurrentUserId(), learnerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<EarnedCertificationDTO> earnedCertifications = earnedCertificationService.findByLearnerIdPaginated(learnerId, pageable);
        log.info("Retrieved page {} with {} earned certifications for learner {}", 
                page, earnedCertifications.getNumberOfElements(), learnerId);
        return ResponseEntity.ok(earnedCertifications);
    }
    
    /**
     * Get a specific earned certification by ID with full details for certificate display.
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<EarnedCertificationDTO> getEarnedCertificationDetails(@PathVariable Long id) {
        log.info("Retrieving earned certification details for id: {}", id);
        EarnedCertificationDTO certification = earnedCertificationService.findByIdWithDetails(id);
        return ResponseEntity.ok(certification);
    }
}
