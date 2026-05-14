package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.EarnedCertificationDTO;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.EarnedCertificationMapper;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CertificationRenewalService.
 * Covers: renewal eligibility rules, expiry extension, edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CertificationRenewalService Unit Tests")
class CertificationRenewalServiceTest {

    @Mock private EarnedCertificationRepository earnedCertificationRepository;
    @Mock private EarnedCertificationMapper earnedCertificationMapper;

    @InjectMocks
    private CertificationRenewalService renewalService;

    private EarnedCertification expiredCert;
    private EarnedCertification activeCert;
    private EarnedCertification soonToExpireCert;
    private EarnedCertificationDTO certDTO;

    @BeforeEach
    void setUp() {
        CertificationTemplate template = new CertificationTemplate();
        template.setId(1L);
        template.setTitle("Java Expert");

        expiredCert = new EarnedCertification();
        expiredCert.setId(1L);
        expiredCert.setLearnerId(10L);
        expiredCert.setCertificationTemplate(template);
        expiredCert.setIssueDate(LocalDate.now().minusYears(3));
        expiredCert.setExpiryDate(LocalDate.now().minusDays(1)); // Expired yesterday

        activeCert = new EarnedCertification();
        activeCert.setId(2L);
        activeCert.setLearnerId(10L);
        activeCert.setCertificationTemplate(template);
        activeCert.setIssueDate(LocalDate.now().minusMonths(6));
        activeCert.setExpiryDate(LocalDate.now().plusMonths(18)); // Expires in 18 months

        soonToExpireCert = new EarnedCertification();
        soonToExpireCert.setId(3L);
        soonToExpireCert.setLearnerId(10L);
        soonToExpireCert.setCertificationTemplate(template);
        soonToExpireCert.setIssueDate(LocalDate.now().minusYears(2));
        soonToExpireCert.setExpiryDate(LocalDate.now().plusDays(15)); // Expires in 15 days

        certDTO = new EarnedCertificationDTO();
        certDTO.setId(1L);
    }

    // ─── RENEWAL ELIGIBILITY ──────────────────────────────────────────────────

    @Test
    @DisplayName("Expired certification → eligible for renewal")
    void expiredCert_eligibleForRenewal() {
        assertThat(renewalService.isEligibleForRenewal(expiredCert)).isTrue();
    }

    @Test
    @DisplayName("Certification expiring in 15 days → eligible for renewal")
    void soonToExpireCert_eligibleForRenewal() {
        assertThat(renewalService.isEligibleForRenewal(soonToExpireCert)).isTrue();
    }

    @Test
    @DisplayName("Active certification expiring in 18 months → NOT eligible")
    void activeCert_notEligibleForRenewal() {
        assertThat(renewalService.isEligibleForRenewal(activeCert)).isFalse();
    }

    @Test
    @DisplayName("Certification with no expiry date → NOT eligible (permanent)")
    void noExpiryCert_notEligible() {
        EarnedCertification permanent = new EarnedCertification();
        permanent.setId(99L);
        permanent.setExpiryDate(null);

        assertThat(renewalService.isEligibleForRenewal(permanent)).isFalse();
    }

    @Test
    @DisplayName("Certification expiring exactly in 30 days → eligible (boundary)")
    void certExpiringExactly30Days_eligible() {
        EarnedCertification boundary = new EarnedCertification();
        boundary.setId(50L);
        boundary.setExpiryDate(LocalDate.now().plusDays(30));

        assertThat(renewalService.isEligibleForRenewal(boundary)).isTrue();
    }

    @Test
    @DisplayName("Certification expiring in 31 days → NOT eligible")
    void certExpiringIn31Days_notEligible() {
        EarnedCertification notYet = new EarnedCertification();
        notYet.setId(51L);
        notYet.setExpiryDate(LocalDate.now().plusDays(31));

        assertThat(renewalService.isEligibleForRenewal(notYet)).isFalse();
    }

    // ─── RENEW CERTIFICATION ──────────────────────────────────────────────────

    @Test
    @DisplayName("Renew expired certification → expiry extended by 2 years")
    void renewExpiredCert_expiryExtendedBy2Years() {
        when(earnedCertificationRepository.findById(1L)).thenReturn(Optional.of(expiredCert));
        when(earnedCertificationRepository.save(any())).thenReturn(expiredCert);
        when(earnedCertificationMapper.toDTO(any())).thenReturn(certDTO);

        renewalService.renewCertification(1L, 5L);

        ArgumentCaptor<EarnedCertification> captor = ArgumentCaptor.forClass(EarnedCertification.class);
        verify(earnedCertificationRepository).save(captor.capture());

        LocalDate newExpiry = captor.getValue().getExpiryDate();
        assertThat(newExpiry).isEqualTo(LocalDate.now().plusYears(2));
    }

    @Test
    @DisplayName("Renew active certification (not expiring soon) → ValidationException")
    void renewActiveCert_throwsValidationException() {
        when(earnedCertificationRepository.findById(2L)).thenReturn(Optional.of(activeCert));

        assertThatThrownBy(() -> renewalService.renewCertification(2L, 5L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("not eligible for renewal");

        verify(earnedCertificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Renew non-existing certification → ResourceNotFoundException")
    void renewNonExistingCert_throwsResourceNotFoundException() {
        when(earnedCertificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> renewalService.renewCertification(999L, 5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ─── FIND EXPIRING CERTIFICATIONS ─────────────────────────────────────────

    @Test
    @DisplayName("Find expiring certifications → returns only those within 30 days")
    void findExpiringCertifications_returnsOnlyWithin30Days() {
        when(earnedCertificationRepository.findByLearnerId(10L))
                .thenReturn(List.of(expiredCert, activeCert, soonToExpireCert));
        when(earnedCertificationMapper.toDTO(soonToExpireCert)).thenReturn(certDTO);

        List<EarnedCertificationDTO> result = renewalService.findExpiringCertifications(10L);

        // Only soonToExpireCert (15 days) should be returned
        // expiredCert is already expired (past), activeCert is too far in future
        assertThat(result).hasSize(1);
        verify(earnedCertificationMapper, times(1)).toDTO(any());
    }

    @Test
    @DisplayName("No expiring certifications → empty list")
    void noExpiringCertifications_emptyList() {
        when(earnedCertificationRepository.findByLearnerId(10L))
                .thenReturn(List.of(activeCert)); // Only active, not expiring soon

        List<EarnedCertificationDTO> result = renewalService.findExpiringCertifications(10L);

        assertThat(result).isEmpty();
    }
}
