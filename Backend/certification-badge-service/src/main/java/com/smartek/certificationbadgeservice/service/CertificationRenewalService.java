package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.EarnedCertificationDTO;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.EarnedCertificationMapper;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for certification renewal.
 *
 * Rules:
 * - A certification can only be renewed if it is expired OR expires within 30 days.
 * - Renewal extends the expiry date by 2 years from today.
 * - A new verificationId is NOT generated (same certificate identity).
 * - Renewal is logged for audit purposes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificationRenewalService {

    private static final int RENEWAL_WINDOW_DAYS = 30;
    private static final int RENEWAL_YEARS = 2;

    private final EarnedCertificationRepository earnedCertificationRepository;
    private final EarnedCertificationMapper earnedCertificationMapper;

    /**
     * Renew a specific certification by ID.
     *
     * @param certificationId the ID of the earned certification to renew
     * @param renewedBy       the user ID performing the renewal
     * @return updated certification DTO
     */
    @Transactional
    public EarnedCertificationDTO renewCertification(Long certificationId, Long renewedBy) {
        log.info("Renewal requested for certificationId={} by userId={}", certificationId, renewedBy);

        EarnedCertification cert = earnedCertificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Earned certification not found with id: " + certificationId));

        // Business rule: only renew if expired or expiring within 30 days
        if (!isEligibleForRenewal(cert)) {
            String msg = String.format(
                    "Certification %d is not eligible for renewal. " +
                    "It must be expired or expiring within %d days.",
                    certificationId, RENEWAL_WINDOW_DAYS);
            log.warn(msg);
            throw new ValidationException(msg);
        }

        LocalDate newExpiry = LocalDate.now().plusYears(RENEWAL_YEARS);
        cert.setExpiryDate(newExpiry);

        EarnedCertification updated = earnedCertificationRepository.save(cert);
        log.info("Certification {} renewed by user {}. New expiry: {}", certificationId, renewedBy, newExpiry);

        return earnedCertificationMapper.toDTO(updated);
    }

    /**
     * Find all certifications for a learner that are expiring within the next 30 days.
     *
     * @param learnerId the learner ID
     * @return list of soon-to-expire certifications
     */
    @Transactional(readOnly = true)
    public List<EarnedCertificationDTO> findExpiringCertifications(Long learnerId) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(RENEWAL_WINDOW_DAYS);

        return earnedCertificationRepository.findByLearnerId(learnerId).stream()
                .filter(cert -> cert.getExpiryDate() != null
                        && !cert.getExpiryDate().isBefore(today)
                        && !cert.getExpiryDate().isAfter(threshold))
                .map(earnedCertificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if a certification is eligible for renewal.
     * Eligible = expired OR expiring within RENEWAL_WINDOW_DAYS.
     */
    public boolean isEligibleForRenewal(EarnedCertification cert) {
        if (cert.getExpiryDate() == null) {
            return false; // No expiry = permanent, no renewal needed
        }
        LocalDate today = LocalDate.now();
        LocalDate renewalThreshold = today.plusDays(RENEWAL_WINDOW_DAYS);
        // Eligible if already expired OR expiry is within the window
        return !cert.getExpiryDate().isAfter(renewalThreshold);
    }
}
