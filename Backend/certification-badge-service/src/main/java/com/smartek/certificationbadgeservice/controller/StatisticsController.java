package com.smartek.certificationbadgeservice.controller;

import com.smartek.certificationbadgeservice.dto.BadgeStatisticsDTO;
import com.smartek.certificationbadgeservice.dto.CertificationStatisticsDTO;
import com.smartek.certificationbadgeservice.dto.LearnerStatisticsDTO;
import com.smartek.certificationbadgeservice.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for statistics endpoints.
 * Provides endpoints for viewing badge, certification, and learner statistics.
 */
@RestController
@RequestMapping("/api/certifications-badges/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * Get badge award statistics.
     * Only accessible by ADMIN, RH_COMPANY, and RH_SMARTEK roles.
     * Returns the total count of each badge type awarded.
     */
    @GetMapping("/badges")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH_COMPANY', 'RH_SMARTEK')")
    public ResponseEntity<List<BadgeStatisticsDTO>> getBadgeStatistics() {
        log.info("Retrieving badge statistics");
        List<BadgeStatisticsDTO> statistics = statisticsService.getBadgeStatistics();
        log.info("Retrieved statistics for {} badge templates", statistics.size());
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get certification award statistics.
     * Only accessible by ADMIN, RH_COMPANY, and RH_SMARTEK roles.
     * Returns the total count of each certification type awarded.
     */
    @GetMapping("/certifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH_COMPANY', 'RH_SMARTEK')")
    public ResponseEntity<List<CertificationStatisticsDTO>> getCertificationStatistics() {
        log.info("Retrieving certification statistics");
        List<CertificationStatisticsDTO> statistics = statisticsService.getCertificationStatistics();
        log.info("Retrieved statistics for {} certification templates", statistics.size());
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get achievement statistics for a specific learner.
     * Only accessible by ADMIN, RH_COMPANY, and RH_SMARTEK roles.
     * Returns the count of active certifications and total badges per learner.
     */
    @GetMapping("/learners/{learnerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH_COMPANY', 'RH_SMARTEK')")
    public ResponseEntity<LearnerStatisticsDTO> getLearnerStatistics(@PathVariable Long learnerId) {
        log.info("Retrieving statistics for learner: {}", learnerId);
        LearnerStatisticsDTO statistics = statisticsService.getLearnerStatistics(learnerId);
        log.info("Retrieved statistics for learner {}: {} badges, {} active certifications, {} expired certifications", 
                learnerId, statistics.getTotalBadges(), statistics.getActiveCertifications(), 
                statistics.getExpiredCertifications());
        return ResponseEntity.ok(statistics);
    }
}
