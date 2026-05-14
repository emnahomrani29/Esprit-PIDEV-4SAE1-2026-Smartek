package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.BadgeStatisticsDTO;
import com.smartek.certificationbadgeservice.dto.CertificationStatisticsDTO;
import com.smartek.certificationbadgeservice.dto.LearnerStatisticsDTO;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.repository.BadgeTemplateRepository;
import com.smartek.certificationbadgeservice.repository.CertificationTemplateRepository;
import com.smartek.certificationbadgeservice.repository.EarnedBadgeRepository;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StatisticsService.
 * Covers: badge stats, certification stats, learner stats, expiry logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService Unit Tests")
class StatisticsServiceTest {

    @Mock private BadgeTemplateRepository badgeTemplateRepository;
    @Mock private CertificationTemplateRepository certificationTemplateRepository;
    @Mock private EarnedBadgeRepository earnedBadgeRepository;
    @Mock private EarnedCertificationRepository earnedCertificationRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private BadgeTemplate badge1, badge2;
    private CertificationTemplate cert1;

    @BeforeEach
    void setUp() {
        badge1 = new BadgeTemplate();
        badge1.setId(1L);
        badge1.setName("Bronze Badge");

        badge2 = new BadgeTemplate();
        badge2.setId(2L);
        badge2.setName("Gold Badge");

        cert1 = new CertificationTemplate();
        cert1.setId(10L);
        cert1.setTitle("Java Expert");
    }

    // ─── BADGE STATISTICS ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Badge statistics → correct counts per template")
    void getBadgeStatistics_correctCounts() {
        when(badgeTemplateRepository.findAll()).thenReturn(List.of(badge1, badge2));
        when(earnedBadgeRepository.countByBadgeTemplateId(1L)).thenReturn(5L);
        when(earnedBadgeRepository.countByBadgeTemplateId(2L)).thenReturn(2L);

        List<BadgeStatisticsDTO> stats = statisticsService.getBadgeStatistics();

        assertThat(stats).hasSize(2);
        assertThat(stats.get(0).getTotalAwarded()).isEqualTo(5L);
        assertThat(stats.get(1).getTotalAwarded()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Badge statistics with no templates → empty list")
    void getBadgeStatistics_noTemplates_emptyList() {
        when(badgeTemplateRepository.findAll()).thenReturn(List.of());

        List<BadgeStatisticsDTO> stats = statisticsService.getBadgeStatistics();

        assertThat(stats).isEmpty();
    }

    // ─── CERTIFICATION STATISTICS ─────────────────────────────────────────────

    @Test
    @DisplayName("Certification statistics → correct counts per template")
    void getCertificationStatistics_correctCounts() {
        when(certificationTemplateRepository.findAll()).thenReturn(List.of(cert1));
        when(earnedCertificationRepository.countByCertificationTemplateId(10L)).thenReturn(12L);

        List<CertificationStatisticsDTO> stats = statisticsService.getCertificationStatistics();

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getTotalAwarded()).isEqualTo(12L);
        assertThat(stats.get(0).getCertificationTitle()).isEqualTo("Java Expert");
    }

    // ─── LEARNER STATISTICS ───────────────────────────────────────────────────

    @Test
    @DisplayName("Learner statistics → active and expired certifications counted correctly")
    void getLearnerStatistics_activeAndExpiredCounted() {
        Long learnerId = 42L;

        // Active certification (no expiry)
        EarnedCertification active = new EarnedCertification();
        active.setId(1L);
        active.setLearnerId(learnerId);
        active.setIssueDate(LocalDate.now().minusMonths(6));
        // expiryDate = null → not expired

        // Expired certification
        EarnedCertification expired = new EarnedCertification();
        expired.setId(2L);
        expired.setLearnerId(learnerId);
        expired.setIssueDate(LocalDate.now().minusYears(3));
        expired.setExpiryDate(LocalDate.now().minusDays(1)); // Expired yesterday

        when(earnedBadgeRepository.findByLearnerId(learnerId)).thenReturn(List.of());
        when(earnedCertificationRepository.findByLearnerId(learnerId))
                .thenReturn(List.of(active, expired));

        LearnerStatisticsDTO stats = statisticsService.getLearnerStatistics(learnerId);

        assertThat(stats.getLearnerId()).isEqualTo(learnerId);
        assertThat(stats.getActiveCertifications()).isEqualTo(1L);
        assertThat(stats.getExpiredCertifications()).isEqualTo(1L);
        assertThat(stats.getTotalBadges()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Learner with badges → total badge count correct")
    void getLearnerStatistics_withBadges_correctCount() {
        Long learnerId = 5L;

        when(earnedBadgeRepository.findByLearnerId(learnerId))
                .thenReturn(List.of(
                        new com.smartek.certificationbadgeservice.entity.EarnedBadge(),
                        new com.smartek.certificationbadgeservice.entity.EarnedBadge(),
                        new com.smartek.certificationbadgeservice.entity.EarnedBadge()
                ));
        when(earnedCertificationRepository.findByLearnerId(learnerId)).thenReturn(List.of());

        LearnerStatisticsDTO stats = statisticsService.getLearnerStatistics(learnerId);

        assertThat(stats.getTotalBadges()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Certification with future expiry → counted as active")
    void certificationWithFutureExpiry_countedAsActive() {
        Long learnerId = 7L;

        EarnedCertification futureExpiry = new EarnedCertification();
        futureExpiry.setId(3L);
        futureExpiry.setLearnerId(learnerId);
        futureExpiry.setIssueDate(LocalDate.now().minusMonths(1));
        futureExpiry.setExpiryDate(LocalDate.now().plusYears(1)); // Expires next year

        when(earnedBadgeRepository.findByLearnerId(learnerId)).thenReturn(List.of());
        when(earnedCertificationRepository.findByLearnerId(learnerId))
                .thenReturn(List.of(futureExpiry));

        LearnerStatisticsDTO stats = statisticsService.getLearnerStatistics(learnerId);

        assertThat(stats.getActiveCertifications()).isEqualTo(1L);
        assertThat(stats.getExpiredCertifications()).isEqualTo(0L);
    }
}
