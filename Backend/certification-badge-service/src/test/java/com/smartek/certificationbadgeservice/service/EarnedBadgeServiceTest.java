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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EarnedBadgeService - Tests unitaires")
class EarnedBadgeServiceTest {

    @Mock private EarnedBadgeRepository earnedBadgeRepository;
    @Mock private BadgeTemplateRepository badgeTemplateRepository;
    @Mock private EarnedBadgeMapper earnedBadgeMapper;

    @InjectMocks private EarnedBadgeService earnedBadgeService;

    private BadgeTemplate sampleTemplate;
    private EarnedBadge sampleEarnedBadge;
    private EarnedBadgeDTO sampleDTO;
    private AwardBadgeRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleTemplate = new BadgeTemplate();
        sampleTemplate.setId(1L);
        sampleTemplate.setName("Spring Boot Expert");
        sampleTemplate.setDescription("Badge pour les experts Spring Boot");
        sampleTemplate.setMinimumScore(70.0);

        sampleEarnedBadge = new EarnedBadge();
        sampleEarnedBadge.setId(1L);
        sampleEarnedBadge.setBadgeTemplate(sampleTemplate);
        sampleEarnedBadge.setLearnerId(5L);
        sampleEarnedBadge.setAwardDate(LocalDate.now());
        sampleEarnedBadge.setAwardedBy(10L);
        sampleEarnedBadge.setVerificationId("abc-123");

        sampleDTO = new EarnedBadgeDTO();
        sampleDTO.setId(1L);
        sampleDTO.setLearnerId(5L);
        sampleDTO.setAwardDate(LocalDate.now());
        sampleDTO.setAwardedBy(10L);
        sampleDTO.setVerificationId("abc-123");

        sampleRequest = new AwardBadgeRequestDTO(1L, 5L, 10L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // awardBadge
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("awardBadge()")
    class AwardBadge {

        @Test
        @DisplayName("Doit attribuer un badge avec succès")
        void shouldAwardBadgeSuccessfully() {
            when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(sampleTemplate));
            when(earnedBadgeRepository.existsByBadgeTemplateIdAndLearnerId(1L, 5L)).thenReturn(false);
            when(earnedBadgeRepository.save(any(EarnedBadge.class))).thenReturn(sampleEarnedBadge);
            when(earnedBadgeMapper.toDTO(sampleEarnedBadge)).thenReturn(sampleDTO);

            EarnedBadgeDTO result = earnedBadgeService.awardBadge(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getLearnerId()).isEqualTo(5L);
            assertThat(result.getVerificationId()).isEqualTo("abc-123");
            verify(earnedBadgeRepository).save(any(EarnedBadge.class));
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le template n'existe pas")
        void shouldThrowWhenTemplateNotFound() {
            when(badgeTemplateRepository.findById(99L)).thenReturn(Optional.empty());
            sampleRequest.setBadgeTemplateId(99L);

            assertThatThrownBy(() -> earnedBadgeService.awardBadge(sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(earnedBadgeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever DuplicateAwardException si le badge a déjà été attribué")
        void shouldThrowWhenBadgeAlreadyAwarded() {
            when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(sampleTemplate));
            when(earnedBadgeRepository.existsByBadgeTemplateIdAndLearnerId(1L, 5L)).thenReturn(true);

            assertThatThrownBy(() -> earnedBadgeService.awardBadge(sampleRequest))
                    .isInstanceOf(DuplicateAwardException.class)
                    .hasMessageContaining("already awarded");

            verify(earnedBadgeRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Doit retourner le badge gagné par ID")
        void shouldReturnEarnedBadgeById() {
            when(earnedBadgeRepository.findById(1L)).thenReturn(Optional.of(sampleEarnedBadge));
            when(earnedBadgeMapper.toDTO(sampleEarnedBadge)).thenReturn(sampleDTO);

            EarnedBadgeDTO result = earnedBadgeService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si le badge n'existe pas")
        void shouldThrowWhenNotFound() {
            when(earnedBadgeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> earnedBadgeService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByLearnerId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByLearnerId()")
    class FindByLearnerId {

        @Test
        @DisplayName("Doit retourner les badges d'un apprenant")
        void shouldReturnBadgesByLearner() {
            when(earnedBadgeRepository.findByLearnerId(5L)).thenReturn(List.of(sampleEarnedBadge));
            when(earnedBadgeMapper.toDTO(sampleEarnedBadge)).thenReturn(sampleDTO);

            List<EarnedBadgeDTO> result = earnedBadgeService.findByLearnerId(5L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLearnerId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si l'apprenant n'a aucun badge")
        void shouldReturnEmptyListWhenNoBadges() {
            when(earnedBadgeRepository.findByLearnerId(99L)).thenReturn(List.of());

            List<EarnedBadgeDTO> result = earnedBadgeService.findByLearnerId(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // bulkAwardBadges
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("bulkAwardBadges() - Attribution en masse")
    class BulkAwardBadges {

        @Test
        @DisplayName("Doit retourner des échecs pour tous si le template n'existe pas")
        void shouldReturnAllFailuresWhenTemplateNotFound() {
            when(badgeTemplateRepository.existsById(99L)).thenReturn(false);

            BulkAwardBadgeRequestDTO request = new BulkAwardBadgeRequestDTO(99L, List.of(1L, 2L, 3L), 10L);

            BulkAwardResponseDTO result = earnedBadgeService.bulkAwardBadges(request);

            assertThat(result.getSuccessCount()).isZero();
            assertThat(result.getFailureCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Doit traiter chaque apprenant indépendamment")
        void shouldProcessEachLearnerIndependently() {
            when(badgeTemplateRepository.existsById(1L)).thenReturn(true);
            // Learner 5 : succès
            when(badgeTemplateRepository.findById(1L)).thenReturn(Optional.of(sampleTemplate));
            when(earnedBadgeRepository.existsByBadgeTemplateIdAndLearnerId(1L, 5L)).thenReturn(false);
            when(earnedBadgeRepository.save(any(EarnedBadge.class))).thenReturn(sampleEarnedBadge);
            when(earnedBadgeMapper.toDTO(any(EarnedBadge.class))).thenReturn(sampleDTO);
            // Learner 6 : déjà attribué
            when(earnedBadgeRepository.existsByBadgeTemplateIdAndLearnerId(1L, 6L)).thenReturn(true);

            BulkAwardBadgeRequestDTO request = new BulkAwardBadgeRequestDTO(1L, List.of(5L, 6L), 10L);

            BulkAwardResponseDTO result = earnedBadgeService.bulkAwardBadges(request);

            assertThat(result.getSuccessCount()).isEqualTo(1);
            assertThat(result.getFailureCount()).isEqualTo(1);
        }
    }
}
