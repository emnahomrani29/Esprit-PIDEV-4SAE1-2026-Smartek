package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.ExamProcessingResultDTO;
import com.smartek.certificationbadgeservice.dto.ExamResultDTO;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedBadge;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.repository.BadgeTemplateRepository;
import com.smartek.certificationbadgeservice.repository.CertificationTemplateRepository;
import com.smartek.certificationbadgeservice.repository.EarnedBadgeRepository;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExamIntegrationService.
 * Tests the core business logic of automatic certification and badge awarding.
 *
 * Coverage targets:
 * - Passing/failing score thresholds
 * - Duplicate prevention
 * - Badge tier selection (Bronze/Silver/Gold)
 * - Missing template handling
 * - Edge cases (exactly 60%, 0%, 100%)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExamIntegrationService Unit Tests")
class ExamIntegrationServiceTest {

    @Mock private CertificationTemplateRepository certificationTemplateRepository;
    @Mock private BadgeTemplateRepository badgeTemplateRepository;
    @Mock private EarnedCertificationRepository earnedCertificationRepository;
    @Mock private EarnedBadgeRepository earnedBadgeRepository;

    @InjectMocks
    private ExamIntegrationService examIntegrationService;

    private static final Long LEARNER_ID = 1L;
    private static final Long EXAM_ID = 100L;

    private CertificationTemplate certTemplate;
    private BadgeTemplate bronzeBadge;
    private BadgeTemplate silverBadge;
    private BadgeTemplate goldBadge;

    @BeforeEach
    void setUp() {
        certTemplate = new CertificationTemplate();
        certTemplate.setId(1L);
        certTemplate.setTitle("Java Developer Certification");
        certTemplate.setDescription("Awarded for passing Java exam");
        certTemplate.setExamId(EXAM_ID);

        bronzeBadge = new BadgeTemplate();
        bronzeBadge.setId(10L);
        bronzeBadge.setName("Bronze Badge");
        bronzeBadge.setExamId(EXAM_ID);
        bronzeBadge.setMinimumScore(60.0);

        silverBadge = new BadgeTemplate();
        silverBadge.setId(11L);
        silverBadge.setName("Silver Badge");
        silverBadge.setExamId(EXAM_ID);
        silverBadge.setMinimumScore(75.0);

        goldBadge = new BadgeTemplate();
        goldBadge.setId(12L);
        goldBadge.setName("Gold Badge");
        goldBadge.setExamId(EXAM_ID);
        goldBadge.setMinimumScore(90.0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FAILING SCORE TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Failing Score Scenarios")
    class FailingScoreTests {

        @Test
        @DisplayName("Score 45% → not passed, nothing awarded")
        void score45_nothingAwarded() {
            ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 45.0, 100.0);

            ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.isCertificationAwarded()).isFalse();
            assertThat(result.isBadgeAwarded()).isFalse();
            assertThat(result.getPercentage()).isEqualTo(45.0);

            // Verify no DB writes
            verify(earnedCertificationRepository, never()).save(any());
            verify(earnedBadgeRepository, never()).save(any());
        }

        @ParameterizedTest(name = "Score {0}% → fails")
        @ValueSource(doubles = {0.0, 10.0, 30.0, 59.9})
        @DisplayName("Scores below 60% always fail")
        void scoresBelowThreshold_alwaysFail(double score) {
            ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, score, 100.0);

            ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.isCertificationAwarded()).isFalse();
            verify(earnedCertificationRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PASSING SCORE TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Passing Score Scenarios")
    class PassingScoreTests {

        @Test
        @DisplayName("Exactly 60% → passes, certification + bronze badge awarded")
        void exactlyPassingScore_certAndBronzeAwarded() {
            // Arrange
            when(certificationTemplateRepository.findByExamId(EXAM_ID))
                    .thenReturn(Optional.of(certTemplate));
            when(earnedCertificationRepository.existsByLearnerIdAndCertificationTemplateId(LEARNER_ID, 1L))
                    .thenReturn(false);
            when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(EXAM_ID, 60.0))
                    .thenReturn(List.of(bronzeBadge));
            when(earnedBadgeRepository.existsByLearnerIdAndBadgeTemplateId(LEARNER_ID, 10L))
                    .thenReturn(false);

            EarnedCertification savedCert = new EarnedCertification();
            savedCert.setId(100L);
            when(earnedCertificationRepository.save(any())).thenReturn(savedCert);

            EarnedBadge savedBadge = new EarnedBadge();
            savedBadge.setId(200L);
            when(earnedBadgeRepository.save(any())).thenReturn(savedBadge);

            // Act
            ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 60.0, 100.0);
            ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

            // Assert
            assertThat(result.isPassed()).isTrue();
            assertThat(result.isCertificationAwarded()).isTrue();
            assertThat(result.isBadgeAwarded()).isTrue();
            assertThat(result.getCertificationId()).isEqualTo(100L);
            assertThat(result.getBadgeId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("Score 75% → silver badge awarded (highest eligible)")
        void score75_silverBadgeAwarded() {
            // Arrange
            when(certificationTemplateRepository.findByExamId(EXAM_ID))
                    .thenReturn(Optional.of(certTemplate));
            when(earnedCertificationRepository.existsByLearnerIdAndCertificationTemplateId(any(), any()))
                    .thenReturn(false);
            // Both bronze and silver are eligible at 75%
            when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(EXAM_ID, 75.0))
                    .thenReturn(List.of(bronzeBadge, silverBadge));
            when(earnedBadgeRepository.existsByLearnerIdAndBadgeTemplateId(LEARNER_ID, 11L))
                    .thenReturn(false);

            EarnedCertification savedCert = new EarnedCertification();
            savedCert.setId(101L);
            when(earnedCertificationRepository.save(any())).thenReturn(savedCert);

            EarnedBadge savedBadge = new EarnedBadge();
            savedBadge.setId(201L);
            when(earnedBadgeRepository.save(any())).thenReturn(savedBadge);

            // Act
            ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 75.0, 100.0);
            ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

            // Assert — verify SILVER badge was saved (highest eligible)
            ArgumentCaptor<EarnedBadge> badgeCaptor = ArgumentCaptor.forClass(EarnedBadge.class);
            verify(earnedBadgeRepository).save(badgeCaptor.capture());
            assertThat(badgeCaptor.getValue().getBadgeTemplate().getId()).isEqualTo(11L); // Silver
            assertThat(result.isBadgeAwarded()).isTrue();
        }

        @Test
        @DisplayName("Score 92% → gold badge awarded (highest eligible)")
        void score92_goldBadgeAwarded() {
            // Arrange
            when(certificationTemplateRepository.findByExamId(EXAM_ID))
                    .thenReturn(Optional.of(certTemplate));
            when(earnedCertificationRepository.existsByLearnerIdAndCertificationTemplateId(any(), any()))
                    .thenReturn(false);
            // All three badges eligible at 92%
            when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(EXAM_ID, 92.0))
                    .thenReturn(List.of(bronzeBadge, silverBadge, goldBadge));
            when(earnedBadgeRepository.existsByLearnerIdAndBadgeTemplateId(LEARNER_ID, 12L))
                    .thenReturn(false);

            EarnedCertification savedCert = new EarnedCertification();
            savedCert.setId(102L);
            when(earnedCertificationRepository.save(any())).thenReturn(savedCert);

            EarnedBadge savedBadge = new EarnedBadge();
            savedBadge.setId(202L);
            when(earnedBadgeRepository.save(any())).thenReturn(savedBadge);

            // Act
            ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 92.0, 100.0);
            ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

            // Assert — verify GOLD badge was saved
            ArgumentCaptor<EarnedBadge> badgeCaptor = ArgumentCaptor.forClass(EarnedBadge.class);
            verify(earnedBadgeRepository).save(badgeCaptor.capture());
            assertThat(badgeCaptor.getValue().getBadgeTemplate().getId()).isEqualTo(12L); // Gold
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DUPLICATE PREVENTION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Duplicate Prevention")
    class DuplicatePreventionTests {

        @Test
        @DisplayName("Learner already has certification → not awarded again")
        void alreadyHasCertification_notAwardedAgain() {
            // Arrange
            when(certificationTemplateRepository.findByExamId(EXAM_ID))
                    .thenReturn(Optional.of(certTemplate));
            when(earnedCertificationRepository.existsByLearnerIdAndCertificationTemplateId(LEARNER_ID, 1L))
                    .thenReturn(true); // Already has it
            when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 80.0, 100.0);
            ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

            // Assert
            assertThat(result.isCertificationAwarded()).isFalse();
            assertThat(result.getCertificationId()).isNull();
            verify(earnedCertificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Learner already has badge → not awarded again")
        void alreadyHasBadge_notAwardedAgain() {
            // Arrange
            when(certificationTemplateRepository.findByExamId(EXAM_ID))
                    .thenReturn(Optional.of(certTemplate));
            when(earnedCertificationRepository.existsByLearnerIdAndCertificationTemplateId(any(), any()))
                    .thenReturn(false);
            when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(EXAM_ID, 80.0))
                    .thenReturn(List.of(bronzeBadge, silverBadge));
            when(earnedBadgeRepository.existsByLearnerIdAndBadgeTemplateId(LEARNER_ID, 11L))
                    .thenReturn(true); // Already has silver

            EarnedCertification savedCert = new EarnedCertification();
            savedCert.setId(103L);
            when(earnedCertificationRepository.save(any())).thenReturn(savedCert);

            // Act
            ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 80.0, 100.0);
            ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

            // Assert
            assertThat(result.isBadgeAwarded()).isFalse();
            assertThat(result.getBadgeId()).isNull();
            verify(earnedBadgeRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MISSING TEMPLATE TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Missing Template Handling")
    class MissingTemplateTests {

        @Test
        @DisplayName("No certification template for exam → graceful handling")
        void noCertificationTemplate_handledGracefully() {
            when(certificationTemplateRepository.findByExamId(EXAM_ID))
                    .thenReturn(Optional.empty());
            when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(any(), any()))
                    .thenReturn(Collections.emptyList());

            ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 85.0, 100.0);
            ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

            assertThat(result.isPassed()).isTrue();
            assertThat(result.isCertificationAwarded()).isFalse();
            assertThat(result.isBadgeAwarded()).isFalse();
            verify(earnedCertificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("No badge templates for exam → certification awarded, no badge")
        void noBadgeTemplates_onlyCertificationAwarded() {
            when(certificationTemplateRepository.findByExamId(EXAM_ID))
                    .thenReturn(Optional.of(certTemplate));
            when(earnedCertificationRepository.existsByLearnerIdAndCertificationTemplateId(any(), any()))
                    .thenReturn(false);
            when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(any(), any()))
                    .thenReturn(Collections.emptyList());

            EarnedCertification savedCert = new EarnedCertification();
            savedCert.setId(104L);
            when(earnedCertificationRepository.save(any())).thenReturn(savedCert);

            ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 75.0, 100.0);
            ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

            assertThat(result.isCertificationAwarded()).isTrue();
            assertThat(result.isBadgeAwarded()).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CERTIFICATION EXPIRY TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Awarded certification has 2-year expiry date")
    void awardedCertification_hasTwoYearExpiry() {
        when(certificationTemplateRepository.findByExamId(EXAM_ID))
                .thenReturn(Optional.of(certTemplate));
        when(earnedCertificationRepository.existsByLearnerIdAndCertificationTemplateId(any(), any()))
                .thenReturn(false);
        when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(any(), any()))
                .thenReturn(Collections.emptyList());

        ArgumentCaptor<EarnedCertification> certCaptor = ArgumentCaptor.forClass(EarnedCertification.class);
        EarnedCertification savedCert = new EarnedCertification();
        savedCert.setId(105L);
        when(earnedCertificationRepository.save(certCaptor.capture())).thenReturn(savedCert);

        ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 70.0, 100.0);
        examIntegrationService.processExamResult(input);

        EarnedCertification captured = certCaptor.getValue();
        assertThat(captured.getExpiryDate()).isNotNull();
        assertThat(captured.getExpiryDate().getYear())
                .isEqualTo(captured.getIssueDate().getYear() + 2);
    }

    @Test
    @DisplayName("System-awarded certification has awardedBy = 0 (SYSTEM)")
    void systemAwardedCertification_awardedByIsZero() {
        when(certificationTemplateRepository.findByExamId(EXAM_ID))
                .thenReturn(Optional.of(certTemplate));
        when(earnedCertificationRepository.existsByLearnerIdAndCertificationTemplateId(any(), any()))
                .thenReturn(false);
        when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(any(), any()))
                .thenReturn(Collections.emptyList());

        ArgumentCaptor<EarnedCertification> certCaptor = ArgumentCaptor.forClass(EarnedCertification.class);
        EarnedCertification savedCert = new EarnedCertification();
        savedCert.setId(106L);
        when(earnedCertificationRepository.save(certCaptor.capture())).thenReturn(savedCert);

        examIntegrationService.processExamResult(new ExamResultDTO(LEARNER_ID, EXAM_ID, 70.0, 100.0));

        assertThat(certCaptor.getValue().getAwardedBy()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Result message contains score percentage")
    void resultMessage_containsScorePercentage() {
        when(certificationTemplateRepository.findByExamId(EXAM_ID))
                .thenReturn(Optional.empty());
        when(badgeTemplateRepository.findByExamIdAndMinimumScoreLessThanEqual(any(), any()))
                .thenReturn(Collections.emptyList());

        ExamResultDTO input = new ExamResultDTO(LEARNER_ID, EXAM_ID, 45.0, 100.0);
        ExamProcessingResultDTO result = examIntegrationService.processExamResult(input);

        assertThat(result.getMessage()).contains("45.00");
    }
}
