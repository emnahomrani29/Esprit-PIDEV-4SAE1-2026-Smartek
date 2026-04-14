package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.dto.AwardBadgeRequestDTO;
import com.smartek.certificationbadgeservice.dto.BulkAwardBadgeRequestDTO;
import com.smartek.certificationbadgeservice.dto.BulkAwardResponseDTO;
import com.smartek.certificationbadgeservice.dto.EarnedBadgeDTO;
import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import com.smartek.certificationbadgeservice.entity.EarnedBadge;
import com.smartek.certificationbadgeservice.exception.DuplicateAwardException;
import com.smartek.certificationbadgeservice.exception.ResourceNotFoundException;
import com.smartek.certificationbadgeservice.mapper.EarnedBadgeMapper;
import com.smartek.certificationbadgeservice.repository.BadgeTemplateRepository;
import com.smartek.certificationbadgeservice.repository.EarnedBadgeRepository;
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
 * Unit tests for EarnedBadgeService.
 * Covers: award, duplicate prevention, bulk operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EarnedBadgeService Unit Tests")
class EarnedBadgeServiceTest {

    @Mock private EarnedBadgeRepository earnedBadgeRepository;
    @Mock private BadgeTemplateRepository badgeTemplateRepository;
    @Mock private EarnedBadgeMapper earnedBadgeMapper;

    @InjectMocks
    private EarnedBadgeService earnedBadgeService;

    private BadgeTemplate template;
    private EarnedBadge savedBadge;
    private EarnedBadgeDTO badgeDTO;

    @BeforeEach
    void setUp() {
        template = new BadgeTemplate();
        template.setId(1L);
        template.setName("Spring Expert Badge");
        template.setMinimumScore(70.0);

        savedBadge = new EarnedBadge();
        savedBadge.setId(10L);
        savedBadge.setBadgeTemplate(template);
        savedBadge.setLearnerId(2L);
        savedBadge.setAwardDate(LocalDate.now());
        savedBadge.setAwardedBy(5L);

        badgeDTO = new EarnedBadgeDTO();
        badgeDTO.setId(10L);
    }

    // ─── AWARD BADGE ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Award badge — valid request → success")
    void awardBadge_validRequest_success() {
        AwardBadgeRequestDTO request = buildRequest(1L, 2L, 5L);

        when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(earnedBadgeRepository.existsByBadgeTemplateIdAndLearnerId(1L, 2L)).thenReturn(false);
        when(earnedBadgeRepository.save(any())).thenReturn(savedBadge);
        when(earnedBadgeMapper.toDTO(savedBadge)).thenReturn(badgeDTO);

        EarnedBadgeDTO result = earnedBadgeService.awardBadge(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(earnedBadgeRepository).save(any());
    }

    @Test
    @DisplayName("Award badge — template not found → ResourceNotFoundException")
    void awardBadge_templateNotFound_throwsException() {
        AwardBadgeRequestDTO request = buildRequest(99L, 2L, 5L);
        when(badgeTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> earnedBadgeService.awardBadge(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(earnedBadgeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Award badge — duplicate → DuplicateAwardException")
    void awardBadge_duplicate_throwsDuplicateAwardException() {
        AwardBadgeRequestDTO request = buildRequest(1L, 2L, 5L);

        when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(earnedBadgeRepository.existsByBadgeTemplateIdAndLearnerId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> earnedBadgeService.awardBadge(request))
                .isInstanceOf(DuplicateAwardException.class)
                .hasMessageContaining("already awarded");

        verify(earnedBadgeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Award badge — correct data mapped to entity")
    void awardBadge_correctDataMappedToEntity() {
        AwardBadgeRequestDTO request = buildRequest(1L, 42L, 7L);

        when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(earnedBadgeRepository.existsByBadgeTemplateIdAndLearnerId(1L, 42L)).thenReturn(false);
        when(earnedBadgeRepository.save(any())).thenReturn(savedBadge);
        when(earnedBadgeMapper.toDTO(any())).thenReturn(badgeDTO);

        ArgumentCaptor<EarnedBadge> captor = ArgumentCaptor.forClass(EarnedBadge.class);
        earnedBadgeService.awardBadge(request);

        verify(earnedBadgeRepository).save(captor.capture());
        EarnedBadge captured = captor.getValue();
        assertThat(captured.getLearnerId()).isEqualTo(42L);
        assertThat(captured.getAwardedBy()).isEqualTo(7L);
        assertThat(captured.getBadgeTemplate()).isEqualTo(template);
        assertThat(captured.getAwardDate()).isEqualTo(LocalDate.now());
    }

    // ─── BULK AWARD ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Bulk award — template not found → all fail")
    void bulkAward_templateNotFound_allFail() {
        BulkAwardBadgeRequestDTO request = new BulkAwardBadgeRequestDTO();
        request.setBadgeTemplateId(99L);
        request.setLearnerIds(List.of(1L, 2L, 3L));
        request.setAwardedBy(5L);

        when(badgeTemplateRepository.existsById(99L)).thenReturn(false);

        BulkAwardResponseDTO response = earnedBadgeService.bulkAwardBadges(request);

        assertThat(response.getFailureCount()).isEqualTo(3);
        assertThat(response.getSuccessCount()).isEqualTo(0);
    }

    // ─── FIND BY LEARNER ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Find by learner — returns mapped list")
    void findByLearnerId_returnsMappedList() {
        when(earnedBadgeRepository.findByLearnerId(2L)).thenReturn(List.of(savedBadge));
        when(earnedBadgeMapper.toDTO(savedBadge)).thenReturn(badgeDTO);

        List<EarnedBadgeDTO> result = earnedBadgeService.findByLearnerId(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Find by learner — no badges → empty list")
    void findByLearnerId_noBadges_emptyList() {
        when(earnedBadgeRepository.findByLearnerId(99L)).thenReturn(List.of());

        List<EarnedBadgeDTO> result = earnedBadgeService.findByLearnerId(99L);

        assertThat(result).isEmpty();
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private AwardBadgeRequestDTO buildRequest(Long templateId, Long learnerId, Long awardedBy) {
        AwardBadgeRequestDTO request = new AwardBadgeRequestDTO();
        request.setBadgeTemplateId(templateId);
        request.setLearnerId(learnerId);
        request.setAwardedBy(awardedBy);
        return request;
    }
}
