package com.smartek.certificationbadgeservice.controller;

import com.smartek.certificationbadgeservice.dto.EarnedCertificationDTO;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.mapper.EarnedCertificationMapper;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoint for verifying certificate authenticity via verificationId.
 * No authentication required — configured as permitAll in SecurityConfig.
 */
@RestController
@RequestMapping("/api/certifications-badges/verify")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final EarnedCertificationRepository earnedCertificationRepository;
    private final EarnedCertificationMapper earnedCertificationMapper;

    /**
     * GET /api/certifications-badges/verify/{verificationId}
     * Returns 200 + certificate details if valid, 404 if not found.
     */
    @GetMapping("/{verificationId}")
    public ResponseEntity<EarnedCertificationDTO> verifyCertificate(
            @PathVariable String verificationId) {
        log.info("Verifying certificate with verificationId: {}", verificationId);

        return earnedCertificationRepository.findByVerificationId(verificationId)
                .map(cert -> {
                    log.info("Certificate verified successfully: id={}", cert.getId());
                    return ResponseEntity.ok(earnedCertificationMapper.toDTO(cert));
                })
                .orElseGet(() -> {
                    log.warn("Certificate not found for verificationId: {}", verificationId);
                    return ResponseEntity.notFound().build();
                });
    }
}
