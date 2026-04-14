package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.BadgeTemplateDTO;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.exception.ValidationException;
import com.smartek.certificationbadgeservice.mapper.BadgeTemplateMapper;
import com.smartek.certificationbadgeservice.repository.BadgeTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BadgeTemplateService.
 * Covers: CRUD operations, validation rules, error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BadgeTemplateService Unit Tests")
class BadgeTemplateServiceTest {

    @Mock private BadgeTemplateRepository badgeTemplateRepository;
    @Mock private BadgeTemplateMapper badgeTemplateMapper;

    @InjectMocks
    private BadgeTemplateService badgeTemplateService;

    private BadgeTemplate entity;
    private BadgeTemplateDTO dto;

    @BeforeEach
    void setUp() {
        entity = new BadgeTemplate();
        entity.setId(1L);
        entity.setName("Java Expert Badge");
        entity.setDescription("Awarded for Java mastery");
        entity.setMinimumScore(75.0);

        dto = new BadgeTemplateDTO();
        dto.setId(1L);
        dto.setName("Java Expert Badge");
        dto.setDescription("Awarded for Java mastery");
        dto.setMinimumScore(75.0);
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Create valid badge template → success")
    void create_validTemplate_success() {
        when(badgeTemplateMapper.toEntity(dto)).thenReturn(entity);
        when(badgeTemplateRepository.save(entity)).thenReturn(entity);
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        BadgeTemplateDTO result = badgeTemplateService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Java Expert Badge");
        verify(badgeTemplateRepository).save(entity);
    }

    @ParameterizedTest(name = "Name ''{0}'' → ValidationException")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Create with blank name → ValidationException")
    void create_blankName_throwsValidationException(String name) {
        dto.setName(name);

        assertThatThrownBy(() -> badgeTemplateService.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Badge name is required");

        verify(badgeTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create with name > 100 chars → ValidationException")
    void create_nameTooLong_throwsValidationException() {
        dto.setName("A".repeat(101));

        assertThatThrownBy(() -> badgeTemplateService.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("100 characters");
    }

    @Test
    @DisplayName("Create with description > 1000 chars → ValidationException")
    void create_descriptionTooLong_throwsValidationException() {
        dto.setDescription("D".repeat(1001));

        assertThatThrownBy(() -> badgeTemplateService.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("1000 characters");
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Update existing template → success")
    void update_existingTemplate_success() {
        when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(badgeTemplateRepository.save(entity)).thenReturn(entity);
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        BadgeTemplateDTO result = badgeTemplateService.update(1L, dto);

        assertThat(result).isNotNull();
        verify(badgeTemplateRepository).save(entity);
    }

    @Test
    @DisplayName("Update non-existing template → ResourceNotFoundException")
    void update_nonExistingTemplate_throwsException() {
        when(badgeTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> badgeTemplateService.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Delete existing template → success")
    void delete_existingTemplate_success() {
        when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatCode(() -> badgeTemplateService.delete(1L)).doesNotThrowAnyException();
        verify(badgeTemplateRepository).delete(entity);
    }

    @Test
    @DisplayName("Delete non-existing template → ResourceNotFoundException")
    void delete_nonExistingTemplate_throwsException() {
        when(badgeTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> badgeTemplateService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(badgeTemplateRepository, never()).delete(any());
    }

    // ─── FIND ALL ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Find all → returns mapped DTOs")
    void findAll_returnsMappedDTOs() {
        when(badgeTemplateRepository.findAll()).thenReturn(List.of(entity));
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        List<BadgeTemplateDTO> result = badgeTemplateService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java Expert Badge");
    }

    @Test
    @DisplayName("Find all with empty repository → returns empty list")
    void findAll_emptyRepository_returnsEmptyList() {
        when(badgeTemplateRepository.findAll()).thenReturn(List.of());

        List<BadgeTemplateDTO> result = badgeTemplateService.findAll();

        assertThat(result).isEmpty();
    }

    // ─── FIND BY ID ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Find by ID — found → returns DTO")
    void findById_found_returnsDTO() {
        when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(badgeTemplateMapper.toDTO(entity)).thenReturn(dto);

        BadgeTemplateDTO result = badgeTemplateService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Find by ID — not found → ResourceNotFoundException")
    void findById_notFound_throwsException() {
        when(badgeTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> badgeTemplateService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
