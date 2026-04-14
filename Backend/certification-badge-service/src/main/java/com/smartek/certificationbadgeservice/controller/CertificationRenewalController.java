package com.smartek.certificationbadgeservice.controller;

import com.smartek.certificationbadgeservice.dto.EarnedCertificationDTO;
import com.smartek.certificationbadgeservice.security.AuthorizationService;
import com.smartek.certificationbadgeservice.service.CertificationRenewalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for certification renewal operations.
 *
 * Endpoints:
 * - POST /api/certifications-badges/renewals/{id}  → renew a certification
 * - GET  /api/certifications-badges/renewals/expiring/learner/{learnerId} → list expiring certs
 */
@RestController
@RequestMapping("/api/certifications-badges/renewals")
@RequiredArgsConstructor
@Slf4j
public class CertificationRenewalController {

    private final CertificationRenewalService renewalService;
    private final AuthorizationService authorizationService;

    /**
     * Renew a certification.
     * Eligible if expired or expiring within 30 days.
     */
    @PostMapping("/{id}")
    public ResponseEntity<EarnedCertificationDTO> renewCertification(@PathVariable Long id) {
        Long renewedBy = authorizationService.getCurrentUserId();
        log.info("Renewal request for certificationId={} by userId={}", id, renewedBy);

        EarnedCertificationDTO renewed = renewalService.renewCertification(id, renewedBy);
        return ResponseEntity.ok(renewed);
    }

    /**
     * Get certifications expiring within the next 30 days for a learner.
     */
    @GetMapping("/expiring/learner/{learnerId}")
    public ResponseEntity<List<EarnedCertificationDTO>> getExpiringCertifications(
            @PathVariable Long learnerId) {
        log.info("Fetching expiring certifications for learnerId={}", learnerId);

        if (!authorizationService.canAccessLearnerData(learnerId)) {
            return ResponseEntity.status(403).build();
        }

        List<EarnedCertificationDTO> expiring = renewalService.findExpiringCertifications(learnerId);
        log.info("Found {} expiring certifications for learnerId={}", expiring.size(), learnerId);
        return ResponseEntity.ok(expiring);
    }
}
