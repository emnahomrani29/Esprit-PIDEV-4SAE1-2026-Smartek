package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.AwardCertificationRequestDTO;
import com.smartek.certificationbadgeservice.dto.EarnedCertificationDTO;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.EarnedCertificationMapper;
import com.smartek.certificationbadgeservice.repository.CertificationTemplateRepository;
import com.smartek.certificationbadgeservice.repository.EarnedCertificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EarnedCertificationServiceTest {

    @Mock private EarnedCertificationRepository earnedCertificationRepository;
    @Mock private CertificationTemplateRepository certificationTemplateRepository;
    @Mock private EarnedCertificationMapper earnedCertificationMapper;
    @Mock private CertificatePdfService certificatePdfService;
    @Mock private EmailService emailService;

    @InjectMocks private EarnedCertificationService service;

    private CertificationTemplate template;
    private EarnedCertification savedEntity;
    private EarnedCertificationDTO expectedDTO;

    @BeforeEach
    void setUp() {
        template = new CertificationTemplate();
        template.setId(1L);
        template.setTitle("Spring Boot Certification");

        savedEntity = new EarnedCertification();
        savedEntity.setId(10L);
        savedEntity.setCertificationTemplate(template);
        savedEntity.setLearnerId(42L);
        savedEntity.setIssueDate(LocalDate.now());
        savedEntity.setAwardedBy(1L);

        expectedDTO = new EarnedCertificationDTO();
        expectedDTO.setId(10L);
        expectedDTO.setLearnerId(42L);
    }

    // ─── awardCertification ───────────────────────────────────────────────────

    /**
     * Happy path: template exists, no prior award → certification is saved and DTO returned.
     */
    @Test
    void shouldAwardCertification_whenTemplateExistsAndNotAlreadyAwarded() {
        // Given
        AwardCertificationRequestDTO request = buildRequest(1L, 42L,
                LocalDate.now(), LocalDate.now().plusYears(2));

        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(earnedCertificationRepository.save(any())).thenReturn(savedEntity);
        when(earnedCertificationMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        EarnedCertificationDTO result = service.awardCertification(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(earnedCertificationRepository).save(any(EarnedCertification.class));
    }

    /**
     * Template not found → ResourceNotFoundException is thrown, nothing is saved.
     */
    @Test
    void shouldThrowResourceNotFoundException_whenTemplateDoesNotExist() {
        // Given
        AwardCertificationRequestDTO request = buildRequest(99L, 42L, LocalDate.now(), null);
        when(certificationTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.awardCertification(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(earnedCertificationRepository, never()).save(any());
    }

    /**
     * Expiry date is before issue date → ValidationException is thrown.
     */
    @Test
    void shouldThrowValidationException_whenExpiryDateIsBeforeIssueDate() {
        // Given
        AwardCertificationRequestDTO request = buildRequest(1L, 42L,
                LocalDate.now(), LocalDate.now().minusDays(1));
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        // When / Then
        assertThatThrownBy(() -> service.awardCertification(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Expiry date cannot be before issue date");

        verify(earnedCertificationRepository, never()).save(any());
    }

    /**
     * Certificate URL is malformed → ValidationException is thrown.
     */
    @Test
    void shouldThrowValidationException_whenCertificateUrlIsMalformed() {
        // Given
        AwardCertificationRequestDTO request = buildRequest(1L, 42L, LocalDate.now(), null);
        request.setCertificateUrl("not-a-valid-url");
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        // When / Then
        assertThatThrownBy(() -> service.awardCertification(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid certificate URL");

        verify(earnedCertificationRepository, never()).save(any());
    }

    /**
     * Valid URL → no exception, save is called.
     */
    @Test
    void shouldAwardCertification_whenCertificateUrlIsValid() {
        // Given
        AwardCertificationRequestDTO request = buildRequest(1L, 42L, LocalDate.now(), null);
        request.setCertificateUrl("https://smartek.com/certs/42.pdf");
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(earnedCertificationRepository.save(any())).thenReturn(savedEntity);
        when(earnedCertificationMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        EarnedCertificationDTO result = service.awardCertification(request);

        // Then
        assertThat(result).isNotNull();
        verify(earnedCertificationRepository).save(any());
    }

    /**
     * Email is sent only when learnerEmail is provided.
     */
    @Test
    void shouldSendEmail_whenLearnerEmailIsProvided() {
        // Given
        AwardCertificationRequestDTO request = buildRequest(1L, 42L, LocalDate.now(), null);
        request.setLearnerEmail("learner@test.com");
        request.setLearnerName("Alice");
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(earnedCertificationRepository.save(any())).thenReturn(savedEntity);
        when(earnedCertificationMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        service.awardCertification(request);

        // Then
        verify(emailService).sendCertificationAwardEmail(eq(savedEntity), eq("Alice"), eq("learner@test.com"));
    }

    /**
     * No email is sent when learnerEmail is null.
     */
    @Test
    void shouldNotSendEmail_whenLearnerEmailIsNull() {
        // Given
        AwardCertificationRequestDTO request = buildRequest(1L, 42L, LocalDate.now(), null);
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(earnedCertificationRepository.save(any())).thenReturn(savedEntity);
        when(earnedCertificationMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        service.awardCertification(request);

        // Then
        verify(emailService, never()).sendCertificationAwardEmail(any(), any(), any());
    }

    // ─── autoAwardCertification ───────────────────────────────────────────────

    /**
     * Duplicate prevention: learner already has this certification → ValidationException.
     */
    @Test
    void shouldThrowValidationException_whenCertificationAlreadyAwardedToLearner() {
        // Given
        when(earnedCertificationRepository.existsByCertificationTemplate_IdAndLearnerId(1L, 42L))
                .thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.autoAwardCertification(1L, 42L, LocalDate.now(), "EXAM-001"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already awarded");

        verify(earnedCertificationRepository, never()).save(any());
    }

    /**
     * Auto-award: no duplicate, template exists → certification saved with awardedBy = 0 (SYSTEM).
     */
    @Test
    void shouldAutoAwardCertification_whenNoDuplicateAndTemplateExists() {
        // Given
        when(earnedCertificationRepository.existsByCertificationTemplate_IdAndLearnerId(1L, 42L))
                .thenReturn(false);
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(earnedCertificationRepository.save(any())).thenReturn(savedEntity);
        when(earnedCertificationMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        EarnedCertificationDTO result = service.autoAwardCertification(1L, 42L, LocalDate.now(), "EXAM-001");

        // Then
        assertThat(result).isNotNull();
        verify(earnedCertificationRepository).save(argThat(cert ->
                cert.getAwardedBy().equals(0L) // SYSTEM award
        ));
    }

    /**
     * Auto-award: template not found → ResourceNotFoundException.
     */
    @Test
    void shouldThrowResourceNotFoundException_whenAutoAwardTemplateDoesNotExist() {
        // Given
        when(earnedCertificationRepository.existsByCertificationTemplate_IdAndLearnerId(99L, 42L))
                .thenReturn(false);
        when(certificationTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.autoAwardCertification(99L, 42L, LocalDate.now(), "EXAM-001"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(earnedCertificationRepository, never()).save(any());
    }

    /**
     * Auto-award stores the examId on the earned certification.
     */
    @Test
    void shouldPersistExamId_whenAutoAwarding() {
        // Given
        when(earnedCertificationRepository.existsByCertificationTemplate_IdAndLearnerId(1L, 42L))
                .thenReturn(false);
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(earnedCertificationRepository.save(any())).thenReturn(savedEntity);
        when(earnedCertificationMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        service.autoAwardCertification(1L, 42L, LocalDate.now(), "EXAM-007");

        // Then
        verify(earnedCertificationRepository).save(argThat(cert ->
                "EXAM-007".equals(cert.getExamId())
        ));
    }

    // ─── findByLearnerId ──────────────────────────────────────────────────────

    /**
     * Returns mapped DTOs for a learner with certifications.
     */
    @Test
    void shouldReturnCertifications_whenLearnerHasEarnedCertifications() {
        // Given
        EarnedCertificationDTO dto = new EarnedCertificationDTO();
        dto.setLearnerId(42L);
        when(earnedCertificationRepository.findByLearnerId(42L)).thenReturn(List.of(savedEntity));
        when(earnedCertificationMapper.toDTO(savedEntity)).thenReturn(dto);

        // When
        List<EarnedCertificationDTO> result = service.findByLearnerId(42L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLearnerId()).isEqualTo(42L);
    }

    /**
     * Returns empty list when learner has no certifications — no exception thrown.
     */
    @Test
    void shouldReturnEmptyList_whenLearnerHasNoCertifications() {
        // Given
        when(earnedCertificationRepository.findByLearnerId(99L)).thenReturn(List.of());

        // When
        List<EarnedCertificationDTO> result = service.findByLearnerId(99L);

        // Then
        assertThat(result).isEmpty();
    }

    // ─── findByIdWithDetails ──────────────────────────────────────────────────

    /**
     * Returns DTO when certification exists.
     */
    @Test
    void shouldReturnCertificationDetails_whenIdExists() {
        // Given
        when(earnedCertificationRepository.findById(10L)).thenReturn(Optional.of(savedEntity));
        when(earnedCertificationMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        EarnedCertificationDTO result = service.findByIdWithDetails(10L);

        // Then
        assertThat(result.getId()).isEqualTo(10L);
    }

    /**
     * Throws ResourceNotFoundException when ID does not exist.
     */
    @Test
    void shouldThrowResourceNotFoundException_whenCertificationIdDoesNotExist() {
        // Given
        when(earnedCertificationRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.findByIdWithDetails(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private AwardCertificationRequestDTO buildRequest(Long templateId, Long learnerId,
                                                       LocalDate issueDate, LocalDate expiryDate) {
        AwardCertificationRequestDTO req = new AwardCertificationRequestDTO();
        req.setCertificationTemplateId(templateId);
        req.setLearnerId(learnerId);
        req.setIssueDate(issueDate);
        req.setExpiryDate(expiryDate);
        req.setAwardedBy(1L);
        return req;
    }
}
