package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.client.AuthServiceClient;
import com.smartek.certificationbadgeservice.dto.AwardCertificationRequestDTO;
import com.smartek.certificationbadgeservice.dto.BulkAwardCertificationRequestDTO;
import com.smartek.certificationbadgeservice.dto.BulkAwardResponseDTO;
import com.smartek.certificationbadgeservice.dto.EarnedCertificationDTO;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.EarnedCertificationMapper;
import com.smartek.certificationbadgeservice.repository.CertificationTemplateRepository;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EarnedCertificationService.
 * Covers: award validation, date rules, bulk operations, duplicate prevention.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EarnedCertificationService Unit Tests")
class EarnedCertificationServiceTest {

    @Mock private EarnedCertificationRepository earnedCertificationRepository;
    @Mock private CertificationTemplateRepository certificationTemplateRepository;
    @Mock private EarnedCertificationMapper earnedCertificationMapper;
    @Mock private PdfGenerationService pdfGenerationService;
    @Mock private EmailService emailService;
    @Mock private AuthServiceClient authServiceClient;

    @InjectMocks
    private EarnedCertificationService earnedCertificationService;

    private CertificationTemplate template;
    private EarnedCertification savedCertification;
    private EarnedCertificationDTO certificationDTO;

    @BeforeEach
    void setUp() {
        template = new CertificationTemplate();
        template.setId(1L);
        template.setTitle("Spring Boot Expert");
        template.setDescription("Advanced Spring Boot certification");

        savedCertification = new EarnedCertification();
        savedCertification.setId(10L);
        savedCertification.setCertificationTemplate(template);
        savedCertification.setLearnerId(2L);
        savedCertification.setIssueDate(LocalDate.now());
        savedCertification.setAwardedBy(5L);
        savedCertification.setVerificationId("test-uuid-1234");

        certificationDTO = new EarnedCertificationDTO();
        certificationDTO.setId(10L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AWARD CERTIFICATION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Award Certification")
    class AwardCertificationTests {

        @Test
        @DisplayName("Valid request → certification awarded successfully")
        void validRequest_certificationAwarded() {
            // Arrange
            AwardCertificationRequestDTO request = buildValidRequest();
            when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(earnedCertificationRepository.saveAndFlush(any())).thenReturn(savedCertification);
            when(earnedCertificationMapper.toDTO(any())).thenReturn(certificationDTO);

            // Act
            EarnedCertificationDTO result = earnedCertificationService.awardCertification(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            verify(earnedCertificationRepository).saveAndFlush(any());
        }

        @Test
        @DisplayName("Template not found → ResourceNotFoundException thrown")
        void templateNotFound_throwsResourceNotFoundException() {
            AwardCertificationRequestDTO request = buildValidRequest();
            when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> earnedCertificationService.awardCertification(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Certification template not found");

            verify(earnedCertificationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Expiry date before issue date → ValidationException thrown")
        void expiryBeforeIssue_throwsValidationException() {
            AwardCertificationRequestDTO request = buildValidRequest();
            request.setIssueDate(LocalDate.now());
            request.setExpiryDate(LocalDate.now().minusDays(1)); // Invalid: expiry before issue

            when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

            assertThatThrownBy(() -> earnedCertificationService.awardCertification(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Expiry date cannot be before issue date");

            verify(earnedCertificationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Invalid certificate URL → ValidationException thrown")
        void invalidCertificateUrl_throwsValidationException() {
            AwardCertificationRequestDTO request = buildValidRequest();
            request.setCertificateUrl("not-a-valid-url");

            when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

            assertThatThrownBy(() -> earnedCertificationService.awardCertification(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid certificate URL");
        }

        @Test
        @DisplayName("Valid URL → accepted without exception")
        void validCertificateUrl_accepted() {
            AwardCertificationRequestDTO request = buildValidRequest();
            request.setCertificateUrl("https://smartek.com/certs/123");

            when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(earnedCertificationRepository.saveAndFlush(any())).thenReturn(savedCertification);
            when(earnedCertificationMapper.toDTO(any())).thenReturn(certificationDTO);

            assertThatCode(() -> earnedCertificationService.awardCertification(request))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Certification data is correctly mapped to entity")
        void certificationData_correctlyMappedToEntity() {
            AwardCertificationRequestDTO request = buildValidRequest();
            request.setLearnerId(42L);
            request.setAwardedBy(7L);

            when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(earnedCertificationRepository.saveAndFlush(any())).thenReturn(savedCertification);
            when(earnedCertificationMapper.toDTO(any())).thenReturn(certificationDTO);

            ArgumentCaptor<EarnedCertification> captor = ArgumentCaptor.forClass(EarnedCertification.class);
            earnedCertificationService.awardCertification(request);

            verify(earnedCertificationRepository).saveAndFlush(captor.capture());
            EarnedCertification captured = captor.getValue();
            assertThat(captured.getLearnerId()).isEqualTo(42L);
            assertThat(captured.getAwardedBy()).isEqualTo(7L);
            assertThat(captured.getCertificationTemplate()).isEqualTo(template);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BULK AWARD TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Bulk Award Certifications")
    class BulkAwardTests {

        @Test
        @DisplayName("Template not found → all learners fail")
        void templateNotFound_allLearnersFail() {
            BulkAwardCertificationRequestDTO request = new BulkAwardCertificationRequestDTO();
            request.setCertificationTemplateId(99L);
            request.setLearnerIds(List.of(1L, 2L, 3L));
            request.setIssueDate(LocalDate.now());
            request.setAwardedBy(5L);

            when(certificationTemplateRepository.existsById(99L)).thenReturn(false);

            BulkAwardResponseDTO response = earnedCertificationService.bulkAwardCertifications(request);

            assertThat(response.getFailureCount()).isEqualTo(3);
            assertThat(response.getSuccessCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Invalid date range → all learners fail")
        void invalidDateRange_allLearnersFail() {
            BulkAwardCertificationRequestDTO request = new BulkAwardCertificationRequestDTO();
            request.setCertificationTemplateId(1L);
            request.setLearnerIds(List.of(1L, 2L));
            request.setIssueDate(LocalDate.now());
            request.setExpiryDate(LocalDate.now().minusDays(5)); // Invalid
            request.setAwardedBy(5L);

            when(certificationTemplateRepository.existsById(1L)).thenReturn(true);

            BulkAwardResponseDTO response = earnedCertificationService.bulkAwardCertifications(request);

            assertThat(response.getFailureCount()).isEqualTo(2);
            assertThat(response.getSuccessCount()).isEqualTo(0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIND BY LEARNER TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Find Certifications by Learner")
    class FindByLearnerTests {

        @Test
        @DisplayName("Learner with certifications → returns list")
        void learnerWithCertifications_returnsList() {
            when(earnedCertificationRepository.findByLearnerId(2L))
                    .thenReturn(List.of(savedCertification));
            when(earnedCertificationMapper.toDTO(any())).thenReturn(certificationDTO);

            List<EarnedCertificationDTO> result = earnedCertificationService.findByLearnerId(2L);

            assertThat(result).hasSize(1);
            verify(earnedCertificationRepository).findByLearnerId(2L);
        }

        @Test
        @DisplayName("Learner with no certifications → returns empty list")
        void learnerWithNoCertifications_returnsEmptyList() {
            when(earnedCertificationRepository.findByLearnerId(99L))
                    .thenReturn(List.of());

            List<EarnedCertificationDTO> result = earnedCertificationService.findByLearnerId(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIND BY ID TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Find by ID — existing certification → returns DTO")
    void findById_existingCertification_returnsDTO() {
        when(earnedCertificationRepository.findById(10L)).thenReturn(Optional.of(savedCertification));
        when(earnedCertificationMapper.toDTO(savedCertification)).thenReturn(certificationDTO);

        EarnedCertificationDTO result = earnedCertificationService.findByIdWithDetails(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Find by ID — not found → ResourceNotFoundException")
    void findById_notFound_throwsException() {
        when(earnedCertificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> earnedCertificationService.findByIdWithDetails(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private AwardCertificationRequestDTO buildValidRequest() {
        AwardCertificationRequestDTO request = new AwardCertificationRequestDTO();
        request.setCertificationTemplateId(1L);
        request.setLearnerId(2L);
        request.setIssueDate(LocalDate.now());
        request.setAwardedBy(5L);
        return request;
    }
}
