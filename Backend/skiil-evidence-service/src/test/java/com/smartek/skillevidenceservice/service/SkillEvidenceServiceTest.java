package com.smartek.skillevidenceservice.service;

import com.smartek.skillevidenceservice.dto.SkillEvidenceRequest;
import com.smartek.skillevidenceservice.dto.SkillEvidenceResponse;
import com.smartek.skillevidenceservice.entity.EvidenceCategory;
import com.smartek.skillevidenceservice.entity.EvidenceStatus;
import com.smartek.skillevidenceservice.entity.NotificationType;
import com.smartek.skillevidenceservice.entity.SkillEvidence;
import com.smartek.skillevidenceservice.repository.SkillEvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillEvidenceService - Tests unitaires")
class SkillEvidenceServiceTest {

    @Mock private SkillEvidenceRepository evidenceRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private SkillEvidenceService skillEvidenceService;

    private SkillEvidence sampleEvidence;
    private SkillEvidenceRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleEvidence = SkillEvidence.builder()
                .evidenceId(1)
                .title("Certificat Spring Boot")
                .fileUrl("https://example.com/cert.pdf")
                .description("Certification obtenue")
                .learnerId(5L)
                .learnerName("Alice Dupont")
                .learnerEmail("alice@smartek.com")
                .uploadDate(LocalDate.now())
                .status(EvidenceStatus.PENDING)
                .category(EvidenceCategory.PROGRAMMING)
                .build();

        sampleRequest = new SkillEvidenceRequest(
                "Certificat Spring Boot",
                "https://example.com/cert.pdf",
                "Certification obtenue",
                5L,
                "Alice Dupont",
                "alice@smartek.com",
                EvidenceCategory.PROGRAMMING
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createEvidence
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createEvidence()")
    class CreateEvidence {

        @Test
        @DisplayName("Doit créer une preuve avec succès")
        void shouldCreateEvidenceSuccessfully() {
            when(evidenceRepository.existsByLearnerIdAndTitle(5L, "Certificat Spring Boot")).thenReturn(false);
            when(evidenceRepository.save(any(SkillEvidence.class))).thenReturn(sampleEvidence);

            SkillEvidenceResponse result = skillEvidenceService.createEvidence(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Certificat Spring Boot");
            assertThat(result.getLearnerId()).isEqualTo(5L);
            verify(evidenceRepository).save(any(SkillEvidence.class));
        }

        @Test
        @DisplayName("Doit lever RuntimeException si une preuve avec ce titre existe déjà")
        void shouldThrowWhenDuplicateTitle() {
            when(evidenceRepository.existsByLearnerIdAndTitle(5L, "Certificat Spring Boot")).thenReturn(true);

            assertThatThrownBy(() -> skillEvidenceService.createEvidence(sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("existe déjà");

            verify(evidenceRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllEvidenceByLearner
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllEvidenceByLearner()")
    class GetAllEvidenceByLearner {

        @Test
        @DisplayName("Doit retourner les preuves d'un apprenant")
        void shouldReturnEvidenceByLearner() {
            when(evidenceRepository.findByLearnerIdOrderByUploadDateDesc(5L))
                    .thenReturn(List.of(sampleEvidence));

            List<SkillEvidenceResponse> result = skillEvidenceService.getAllEvidenceByLearner(5L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLearnerId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune preuve")
        void shouldReturnEmptyListWhenNoEvidence() {
            when(evidenceRepository.findByLearnerIdOrderByUploadDateDesc(99L))
                    .thenReturn(Collections.emptyList());

            List<SkillEvidenceResponse> result = skillEvidenceService.getAllEvidenceByLearner(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getEvidenceById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getEvidenceById()")
    class GetEvidenceById {

        @Test
        @DisplayName("Doit retourner la preuve par ID")
        void shouldReturnEvidenceById() {
            when(evidenceRepository.findById(1)).thenReturn(Optional.of(sampleEvidence));

            SkillEvidenceResponse result = skillEvidenceService.getEvidenceById(1);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Certificat Spring Boot");
        }

        @Test
        @DisplayName("Doit lever RuntimeException si la preuve n'existe pas")
        void shouldThrowWhenEvidenceNotFound() {
            when(evidenceRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> skillEvidenceService.getEvidenceById(99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("non trouvée");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteEvidence
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteEvidence()")
    class DeleteEvidence {

        @Test
        @DisplayName("Doit supprimer une preuve existante")
        void shouldDeleteEvidenceSuccessfully() {
            when(evidenceRepository.existsById(1)).thenReturn(true);

            skillEvidenceService.deleteEvidence(1);

            verify(evidenceRepository).deleteById(1);
        }

        @Test
        @DisplayName("Doit lever RuntimeException si la preuve n'existe pas")
        void shouldThrowWhenEvidenceNotFound() {
            when(evidenceRepository.existsById(99)).thenReturn(false);

            assertThatThrownBy(() -> skillEvidenceService.deleteEvidence(99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("non trouvée");

            verify(evidenceRepository, never()).deleteById(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // approveEvidence - logique métier
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("approveEvidence() - Validation métier")
    class ApproveEvidence {

        @Test
        @DisplayName("Doit approuver une preuve avec un score valide")
        void shouldApproveEvidenceWithValidScore() {
            when(evidenceRepository.findById(1)).thenReturn(Optional.of(sampleEvidence));
            when(evidenceRepository.save(any(SkillEvidence.class))).thenReturn(sampleEvidence);

            SkillEvidence result = skillEvidenceService.approveEvidence(1, 85, 10L, "Excellent travail");

            assertThat(result).isNotNull();
            verify(evidenceRepository).save(any(SkillEvidence.class));
            verify(notificationService).createNotification(
                    eq(5L), eq(1), anyString(), eq(NotificationType.APPROVAL));
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException si le score est null")
        void shouldThrowWhenScoreIsNull() {
            assertThatThrownBy(() -> skillEvidenceService.approveEvidence(1, null, 10L, "Commentaire"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Score");
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException si le score est hors limites (> 100)")
        void shouldThrowWhenScoreExceedsMax() {
            assertThatThrownBy(() -> skillEvidenceService.approveEvidence(1, 101, 10L, "Commentaire"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Score");
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException si le score est négatif")
        void shouldThrowWhenScoreIsNegative() {
            assertThatThrownBy(() -> skillEvidenceService.approveEvidence(1, -1, 10L, "Commentaire"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Score");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // rejectEvidence - logique métier
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("rejectEvidence() - Validation métier")
    class RejectEvidence {

        @Test
        @DisplayName("Doit rejeter une preuve avec un commentaire")
        void shouldRejectEvidenceWithComment() {
            when(evidenceRepository.findById(1)).thenReturn(Optional.of(sampleEvidence));
            when(evidenceRepository.save(any(SkillEvidence.class))).thenReturn(sampleEvidence);

            SkillEvidence result = skillEvidenceService.rejectEvidence(1, "Preuve insuffisante", 10L);

            assertThat(result).isNotNull();
            verify(notificationService).createNotification(
                    eq(5L), eq(1), anyString(), eq(NotificationType.REJECTION));
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException si le commentaire est vide")
        void shouldThrowWhenCommentIsEmpty() {
            assertThatThrownBy(() -> skillEvidenceService.rejectEvidence(1, "", 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("comment");
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException si le commentaire est null")
        void shouldThrowWhenCommentIsNull() {
            assertThatThrownBy(() -> skillEvidenceService.rejectEvidence(1, null, 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("comment");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // reviewEvidence - validation du score pour APPROVED
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("reviewEvidence() - Validation selon statut")
    class ReviewEvidence {

        @Test
        @DisplayName("Doit lever IllegalArgumentException si APPROVED sans score")
        void shouldThrowWhenApprovingWithoutScore() {
            when(evidenceRepository.findById(1)).thenReturn(Optional.of(sampleEvidence));

            assertThatThrownBy(() ->
                    skillEvidenceService.reviewEvidence(1, EvidenceStatus.APPROVED, null, "Commentaire", 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Score");
        }

        @Test
        @DisplayName("Doit lever IllegalArgumentException si REJECTED sans commentaire")
        void shouldThrowWhenRejectingWithoutComment() {
            when(evidenceRepository.findById(1)).thenReturn(Optional.of(sampleEvidence));

            assertThatThrownBy(() ->
                    skillEvidenceService.reviewEvidence(1, EvidenceStatus.REJECTED, null, "", 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Comment");
        }

        @Test
        @DisplayName("Doit mettre à jour le statut PENDING sans contrainte")
        void shouldUpdateToPendingWithoutConstraints() {
            when(evidenceRepository.findById(1)).thenReturn(Optional.of(sampleEvidence));
            when(evidenceRepository.save(any(SkillEvidence.class))).thenReturn(sampleEvidence);

            SkillEvidence result = skillEvidenceService.reviewEvidence(
                    1, EvidenceStatus.PENDING, null, null, 10L);

            assertThat(result).isNotNull();
            verify(evidenceRepository).save(any(SkillEvidence.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getEvidencesByStatus
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getEvidencesByStatus()")
    class GetEvidencesByStatus {

        @Test
        @DisplayName("Doit retourner les preuves filtrées par statut PENDING")
        void shouldReturnEvidencesByStatus() {
            when(evidenceRepository.findByStatus(EvidenceStatus.PENDING))
                    .thenReturn(List.of(sampleEvidence));

            List<SkillEvidenceResponse> result = skillEvidenceService.getEvidencesByStatus(EvidenceStatus.PENDING);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(EvidenceStatus.PENDING);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune preuve avec ce statut")
        void shouldReturnEmptyListWhenNoEvidenceWithStatus() {
            when(evidenceRepository.findByStatus(EvidenceStatus.APPROVED))
                    .thenReturn(Collections.emptyList());

            List<SkillEvidenceResponse> result = skillEvidenceService.getEvidencesByStatus(EvidenceStatus.APPROVED);

            assertThat(result).isEmpty();
        }
    }
}
