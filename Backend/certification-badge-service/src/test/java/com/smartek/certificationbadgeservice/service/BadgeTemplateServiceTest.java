package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.BadgeTemplateDTO;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.BadgeTemplateMapper;
import com.smartek.certificationbadgeservice.repository.BadgeTemplateRepository;
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
class BadgeTemplateServiceTest {

    @Mock private BadgeTemplateRepository badgeTemplateRepository;
    @Mock private BadgeTemplateMapper badgeTemplateMapper;

    @InjectMocks private BadgeTemplateService service;

    private BadgeTemplate entity;
    private BadgeTemplateDTO dto;

    @BeforeEach
    void setUp() {
        entity = new BadgeTemplate();
        entity.setId(1L);
        entity.setName("Gold Badge");
        entity.setDescription("Awarded for 90%+ score");
        entity.setMinimumScore(90.0);
        entity.setExamId(10L);

        dto = new BadgeTemplateDTO();
        dto.setId(1L);
        dto.setName("Gold Badge");
        dto.setDescription("Awarded for 90%+ score");
        dto.setMinimumScore(90.0);
        dto.setExamId(10L);
    }

    // ─── create ───────────────────────────────────────────────────────────────

    /**
     * Valid DTO → entity is saved and mapped DTO is returned.
     */
    @Test
    void shouldCreateBadgeTemplate_whenDtoIsValid() {
        // Given
        when(badgeTemplateMapper.toEntity(dto)).thenReturn(entity);
        when(badgeTemplateRepository.save(entity)).thenReturn(entity);
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When
        BadgeTemplateDTO result = service.create(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Gold Badge");
        verify(badgeTemplateRepository).save(entity);
    }

    /**
     * Null name → ValidationException, nothing saved.
     */
    @Test
    void shouldThrowValidationException_whenNameIsNull() {
        // Given
        dto.setName(null);

        // When / Then
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Badge name is required");

        verify(badgeTemplateRepository, never()).save(any());
    }

    /**
     * Blank name (whitespace only) → ValidationException.
     */
    @Test
    void shouldThrowValidationException_whenNameIsBlank() {
        // Given
        dto.setName("   ");

        // When / Then
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Badge name is required");

        verify(badgeTemplateRepository, never()).save(any());
    }

    /**
     * Name exceeds 100 characters → ValidationException.
     */
    @Test
    void shouldThrowValidationException_whenNameExceeds100Characters() {
        // Given
        dto.setName("B".repeat(101));

        // When / Then
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("100");

        verify(badgeTemplateRepository, never()).save(any());
    }

    /**
     * Description exceeds 1000 characters → ValidationException.
     */
    @Test
    void shouldThrowValidationException_whenDescriptionExceeds1000Characters() {
        // Given
        dto.setDescription("X".repeat(1001));

        // When / Then
        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("1000");

        verify(badgeTemplateRepository, never()).save(any());
    }

    /**
     * Null description is allowed — no exception thrown.
     */
    @Test
    void shouldCreateBadgeTemplate_whenDescriptionIsNull() {
        // Given
        dto.setDescription(null);
        when(badgeTemplateMapper.toEntity(dto)).thenReturn(entity);
        when(badgeTemplateRepository.save(entity)).thenReturn(entity);
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When / Then
        assertThatCode(() -> service.create(dto)).doesNotThrowAnyException();
    }

    /**
     * Exactly 100-character name → valid, no exception.
     */
    @Test
    void shouldCreateBadgeTemplate_whenNameIsExactly100Characters() {
        // Given
        dto.setName("B".repeat(100));
        when(badgeTemplateMapper.toEntity(dto)).thenReturn(entity);
        when(badgeTemplateRepository.save(entity)).thenReturn(entity);
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When / Then
        assertThatCode(() -> service.create(dto)).doesNotThrowAnyException();
    }

    // ─── update ───────────────────────────────────────────────────────────────

    /**
     * Template exists and DTO is valid → entity is updated and saved.
     */
    @Test
    void shouldUpdateBadgeTemplate_whenIdExistsAndDtoIsValid() {
        // Given
        when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(badgeTemplateRepository.save(entity)).thenReturn(entity);
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When
        BadgeTemplateDTO result = service.update(1L, dto);

        // Then
        assertThat(result).isNotNull();
        verify(badgeTemplateMapper).updateEntityFromDTO(dto, entity);
        verify(badgeTemplateRepository).save(entity);
    }

    /**
     * Template not found → ResourceNotFoundException.
     */
    @Test
    void shouldThrowResourceNotFoundException_whenUpdatingNonExistentTemplate() {
        // Given
        when(badgeTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(badgeTemplateRepository, never()).save(any());
    }

    /**
     * Invalid DTO on update → ValidationException before any DB lookup.
     */
    @Test
    void shouldThrowValidationException_whenUpdatingWithInvalidName() {
        // Given
        dto.setName(null);

        // When / Then
        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(ValidationException.class);

        verify(badgeTemplateRepository, never()).findById(any());
        verify(badgeTemplateRepository, never()).save(any());
    }

    // ─── findAll ──────────────────────────────────────────────────────────────

    /**
     * Returns all badge templates mapped to DTOs.
     */
    @Test
    void shouldReturnAllBadgeTemplates_whenRepositoryHasData() {
        // Given
        when(badgeTemplateRepository.findAll()).thenReturn(List.of(entity));
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When
        List<BadgeTemplateDTO> result = service.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Gold Badge");
    }

    /**
     * Returns empty list when no badge templates exist — no exception.
     */
    @Test
    void shouldReturnEmptyList_whenNoBadgeTemplatesExist() {
        // Given
        when(badgeTemplateRepository.findAll()).thenReturn(List.of());

        // When
        List<BadgeTemplateDTO> result = service.findAll();

        // Then
        assertThat(result).isEmpty();
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    /**
     * Returns DTO when badge template exists.
     */
    @Test
    void shouldReturnBadgeTemplate_whenIdExists() {
        // Given
        when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When
        BadgeTemplateDTO result = service.findById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Gold Badge");
    }

    /**
     * Throws ResourceNotFoundException when ID does not exist.
     */
    @Test
    void shouldThrowResourceNotFoundException_whenBadgeTemplateIdDoesNotExist() {
        // Given
        when(badgeTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    /**
     * Template exists → deleted successfully.
     */
    @Test
    void shouldDeleteBadgeTemplate_whenIdExists() {
        // Given
        when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        service.delete(1L);

        // Then
        verify(badgeTemplateRepository).delete(entity);
    }

    /**
     * Template not found → ResourceNotFoundException, delete never called.
     */
    @Test
    void shouldThrowResourceNotFoundException_whenDeletingNonExistentTemplate() {
        // Given
        when(badgeTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(badgeTemplateRepository, never()).delete(any());
    }

    // ─── minimumScore / tiering logic ─────────────────────────────────────────

    /**
     * Default minimumScore is 60.0 when not explicitly set.
     * Verifies the DTO default value matches the expected passing threshold.
     */
    @Test
    void shouldHaveDefaultMinimumScore_whenNotExplicitlySet() {
        // Given
        BadgeTemplateDTO bronzeDto = new BadgeTemplateDTO();
        bronzeDto.setName("Bronze Badge");

        // When / Then
        assertThat(bronzeDto.getMinimumScore()).isEqualTo(60.0);
    }

    /**
     * Badge template with minimumScore = 90.0 represents a Gold-tier badge.
     * Verifies the score is persisted correctly.
     */
    @Test
    void shouldPersistMinimumScore_whenCreatingGoldTierBadge() {
        // Given
        dto.setMinimumScore(90.0);
        when(badgeTemplateMapper.toEntity(dto)).thenReturn(entity);
        when(badgeTemplateRepository.save(entity)).thenReturn(entity);
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        // When
        BadgeTemplateDTO result = service.create(dto);

        // Then
        assertThat(result.getMinimumScore()).isEqualTo(90.0);
    }
}
