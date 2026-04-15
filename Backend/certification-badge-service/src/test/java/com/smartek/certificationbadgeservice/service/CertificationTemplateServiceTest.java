package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.CertificationTemplateDTO;
import com.smartek.certificationbadgeservice.entity.CertificationTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.CertificationTemplateMapper;
import com.smartek.certificationbadgeservice.repository.CertificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificationTemplateServiceTest {

    @Mock private CertificationTemplateRepository certificationTemplateRepository;
    @Mock private CertificationTemplateMapper certificationTemplateMapper;

    @InjectMocks private CertificationTemplateService service;

    private CertificationTemplate entity;
    private CertificationTemplateDTO dto;

    @BeforeEach
    void setUp() {
        entity = new CertificationTemplate();
        entity.setId(1L);
        entity.setTitle("Spring Boot Certification");
        entity.setDescription("A certification for Spring Boot developers");

        dto = new CertificationTemplateDTO();
        dto.setId(1L);
        dto.setTitle("Spring Boot Certification");
        dto.setDescription("A certification for Spring Boot developers");
    }

    // ─── create ───────────────────────────────────────────────────────────────

    /**
     * Valid DTO → entity is saved and mapped DTO is returned.
     */
    @Test
    void shouldCreateTemplate_whenDtoIsValid() {
        // Given
        when(certificationTemplateMapper.toEntity(dto)).thenReturn(entity);
        when(certificationTemplateRepository.save(entity)).thenReturn(entity);
        when(certificationTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When
        CertificationTemplateDTO result = service.create(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Spring Boot Certification");
        verify(certificationTemplateRepository).save(entity);
    }

    /**
     * Null title → ValidationException, nothing saved.
     */
    @Test
    void shouldThrowValidationException_whenTitleIsNull() {
        // Given
        dto.setTitle(null);

        // When / Then
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("title is required");

        verify(certificationTemplateRepository, never()).save(any());
    }

    /**
     * Blank title (whitespace only) → ValidationException.
     */
    @Test
    void shouldThrowValidationException_whenTitleIsBlank() {
        // Given
        dto.setTitle("   ");

        // When / Then
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("title is required");

        verify(certificationTemplateRepository, never()).save(any());
    }

    /**
     * Title exceeds 200 characters → ValidationException.
     */
    @Test
    void shouldThrowValidationException_whenTitleExceeds200Characters() {
        // Given
        dto.setTitle("A".repeat(201));

        // When / Then
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("200");

        verify(certificationTemplateRepository, never()).save(any());
    }

    /**
     * Description exceeds 1000 characters → ValidationException.
     */
    @Test
    void shouldThrowValidationException_whenDescriptionExceeds1000Characters() {
        // Given
        dto.setDescription("D".repeat(1001));

        // When / Then
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("1000");

        verify(certificationTemplateRepository, never()).save(any());
    }

    /**
     * Null description is allowed — no exception thrown.
     */
    @Test
    void shouldCreateTemplate_whenDescriptionIsNull() {
        // Given
        dto.setDescription(null);
        when(certificationTemplateMapper.toEntity(dto)).thenReturn(entity);
        when(certificationTemplateRepository.save(entity)).thenReturn(entity);
        when(certificationTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When / Then
        assertThatCode(() -> service.create(dto)).doesNotThrowAnyException();
    }

    // ─── update ───────────────────────────────────────────────────────────────

    /**
     * Template exists and DTO is valid → entity is updated and saved.
     */
    @Test
    void shouldUpdateTemplate_whenIdExistsAndDtoIsValid() {
        // Given
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(certificationTemplateRepository.save(entity)).thenReturn(entity);
        when(certificationTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When
        CertificationTemplateDTO result = service.update(1L, dto);

        // Then
        assertThat(result).isNotNull();
        verify(certificationTemplateMapper).updateEntityFromDTO(dto, entity);
        verify(certificationTemplateRepository).save(entity);
    }

    /**
     * Template not found → ResourceNotFoundException.
     */
    @Test
    void shouldThrowResourceNotFoundException_whenUpdatingNonExistentTemplate() {
        // Given
        when(certificationTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(certificationTemplateRepository, never()).save(any());
    }

    /**
     * Invalid DTO on update → ValidationException before any DB call.
     */
    @Test
    void shouldThrowValidationException_whenUpdatingWithInvalidTitle() {
        // Given
        dto.setTitle("");

        // When / Then
        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(ValidationException.class);

        verify(certificationTemplateRepository, never()).findById(any());
        verify(certificationTemplateRepository, never()).save(any());
    }

    // ─── findAll ──────────────────────────────────────────────────────────────

    /**
     * Returns all templates mapped to DTOs.
     */
    @Test
    void shouldReturnAllTemplates_whenRepositoryHasData() {
        // Given
        when(certificationTemplateRepository.findAll()).thenReturn(List.of(entity));
        when(certificationTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When
        List<CertificationTemplateDTO> result = service.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Spring Boot Certification");
    }

    /**
     * Returns empty list when no templates exist — no exception.
     */
    @Test
    void shouldReturnEmptyList_whenNoTemplatesExist() {
        // Given
        when(certificationTemplateRepository.findAll()).thenReturn(List.of());

        // When
        List<CertificationTemplateDTO> result = service.findAll();

        // Then
        assertThat(result).isEmpty();
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    /**
     * Returns DTO when template exists.
     */
    @Test
    void shouldReturnTemplate_whenIdExists() {
        // Given
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(certificationTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When
        CertificationTemplateDTO result = service.findById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    /**
     * Throws ResourceNotFoundException when ID does not exist.
     */
    @Test
    void shouldThrowResourceNotFoundException_whenIdDoesNotExist() {
        // Given
        when(certificationTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    /**
     * Template with no earned certifications → deleted successfully.
     */
    @Test
    void shouldDeleteTemplate_whenNoLearnersHaveEarnedIt() {
        // Given
        entity.setEarnedCertifications(List.of()); // empty — safe to delete
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        service.delete(1L);

        // Then
        verify(certificationTemplateRepository).delete(entity);
    }

    /**
     * Template with earned certifications → ValidationException, not deleted.
     * Protects learner achievements from being wiped.
     */
    @Test
    void shouldThrowValidationException_whenLearnersHaveEarnedTheCertification() {
        // Given
        EarnedCertification earned = new EarnedCertification();
        entity.setEarnedCertifications(List.of(earned));
        when(certificationTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));

        // When / Then
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("1 learner");

        verify(certificationTemplateRepository, never()).delete(any());
    }

    /**
     * Template not found → ResourceNotFoundException.
     */
    @Test
    void shouldThrowResourceNotFoundException_whenDeletingNonExistentTemplate() {
        // Given
        when(certificationTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(certificationTemplateRepository, never()).delete(any());
    }
}
