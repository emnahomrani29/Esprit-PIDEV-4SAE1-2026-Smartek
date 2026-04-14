package com.smartek.skillevidenceservice.service;

import com.smartek.skillevidenceservice.dto.SkillEvidenceRequest;
import com.smartek.skillevidenceservice.dto.SkillEvidenceResponse;
import com.smartek.skillevidenceservice.entity.EvidenceCategory;
import com.smartek.skillevidenceservice.entity.EvidenceStatus;
import com.smartek.skillevidenceservice.entity.SkillEvidence;
import com.smartek.skillevidenceservice.repository.SkillEvidenceRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillEvidenceServiceTest {

    @Mock
    private SkillEvidenceRepository evidenceRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SkillEvidenceService service;

    private SkillEvidence evidence;
    private SkillEvidenceRequest request;

    @BeforeEach
    void setUp() {
        evidence = SkillEvidence.builder()
                .evidenceId(1)
                .title("Java Certification")
                .fileUrl("http://example.com/cert.pdf")
                .description("Oracle Java SE 17")
                .learnerId(10L)
                .learnerName("Alice Dupont")
                .learnerEmail("alice@example.com")
                .uploadDate(LocalDate.now())
                .status(EvidenceStatus.PENDING)
                .category(EvidenceCategory.PROGRAMMING)
                .build();

        request = new SkillEvidenceRequest(
                "Java Certification",
                "http://example.com/cert.pdf",
                "Oracle Java SE 17",
                10L,
                "Alice Dupont",
                "alice@example.com",
                EvidenceCategory.PROGRAMMING
        );
    }

    // ===== createEvidence =====

    @Test
    void createEvidence_success() {
        when(evidenceRepository.existsByLearnerIdAndTitle(10L, "Java Certification")).thenReturn(false);
        when(evidenceRepository.save(any())).thenReturn(evidence);

        SkillEvidenceResponse response = service.createEvidence(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Java Certification");
        assertThat(response.getLearnerId()).isEqualTo(10L);
        verify(evidenceRepository).save(any(SkillEvidence.class));
    }

    @Test
    void createEvidence_duplicateTitle_throwsException() {
        when(evidenceRepository.existsByLearnerIdAndTitle(10L, "Java Certification")).thenReturn(true);

        assertThatThrownBy(() -> service.createEvidence(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("existe déjà");

        verify(evidenceRepository, never()).save(any());
    }

    // ===== getEvidenceById =====

    @Test
    void getEvidenceById_found() {
        when(evidenceRepository.findById(1)).thenReturn(Optional.of(evidence));

        SkillEvidenceResponse response = service.getEvidenceById(1);

        assertThat(response.getEvidenceId()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(EvidenceStatus.PENDING);
    }

    @Test
    void getEvidenceById_notFound_throwsException() {
        when(evidenceRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEvidenceById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("non trouvée");
    }

    // ===== getAllEvidenceByLearner =====

    @Test
    void getAllEvidenceByLearner_returnsList() {
        when(evidenceRepository.findByLearnerIdOrderByUploadDateDesc(10L))
                .thenReturn(List.of(evidence));

        List<SkillEvidenceResponse> result = service.getAllEvidenceByLearner(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLearnerName()).isEqualTo("Alice Dupont");
    }

    // ===== updateEvidence =====

    @Test
    void updateEvidence_success() {
        when(evidenceRepository.findById(1)).thenReturn(Optional.of(evidence));
        when(evidenceRepository.existsByLearnerIdAndTitleAndEvidenceIdNot(10L, "Java Certification", 1)).thenReturn(false);
        when(evidenceRepository.save(any())).thenReturn(evidence);

        SkillEvidenceResponse response = service.updateEvidence(1, request);

        assertThat(response).isNotNull();
        verify(evidenceRepository).save(evidence);
    }

    @Test
    void updateEvidence_notFound_throwsException() {
        when(evidenceRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateEvidence(99, request))
                .isInstanceOf(RuntimeException.class);
    }

    // ===== deleteEvidence =====

    @Test
    void deleteEvidence_success() {
        when(evidenceRepository.existsById(1)).thenReturn(true);

        service.deleteEvidence(1);

        verify(evidenceRepository).deleteById(1);
    }

    @Test
    void deleteEvidence_notFound_throwsException() {
        when(evidenceRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteEvidence(99))
                .isInstanceOf(RuntimeException.class);
    }

    // ===== approveEvidence =====

    @Test
    void approveEvidence_success() {
        when(evidenceRepository.findById(1)).thenReturn(Optional.of(evidence));
        when(evidenceRepository.save(any())).thenReturn(evidence);

        SkillEvidence result = service.approveEvidence(1, 85, 1L, "Bon travail");

        assertThat(result.getStatus()).isEqualTo(EvidenceStatus.APPROVED);
        assertThat(result.getScore()).isEqualTo(85);
        verify(notificationService).createNotification(eq(10L), eq(1), anyString(), any());
    }

    @Test
    void approveEvidence_invalidScore_throwsException() {
        assertThatThrownBy(() -> service.approveEvidence(1, 150, 1L, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Score must be between 0 and 100");
    }

    @Test
    void approveEvidence_nullScore_throwsException() {
        assertThatThrownBy(() -> service.approveEvidence(1, null, 1L, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== rejectEvidence =====

    @Test
    void rejectEvidence_success() {
        when(evidenceRepository.findById(1)).thenReturn(Optional.of(evidence));
        when(evidenceRepository.save(any())).thenReturn(evidence);

        SkillEvidence result = service.rejectEvidence(1, "Preuve insuffisante", 1L);

        assertThat(result.getStatus()).isEqualTo(EvidenceStatus.REJECTED);
        verify(notificationService).createNotification(eq(10L), eq(1), anyString(), any());
    }

    @Test
    void rejectEvidence_emptyComment_throwsException() {
        assertThatThrownBy(() -> service.rejectEvidence(1, "", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");
    }

    @Test
    void rejectEvidence_nullComment_throwsException() {
        assertThatThrownBy(() -> service.rejectEvidence(1, null, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== reviewEvidence =====

    @Test
    void reviewEvidence_approve_requiresScore() {
        when(evidenceRepository.findById(1)).thenReturn(Optional.of(evidence));

        assertThatThrownBy(() -> service.reviewEvidence(1, EvidenceStatus.APPROVED, null, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Score is required");
    }

    @Test
    void reviewEvidence_reject_requiresComment() {
        when(evidenceRepository.findById(1)).thenReturn(Optional.of(evidence));

        assertThatThrownBy(() -> service.reviewEvidence(1, EvidenceStatus.REJECTED, null, "", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment is required");
    }

    // ===== getLearnerAnalytics =====

    @Test
    void getLearnerAnalytics_emptyList_returnsZeroCounts() {
        when(evidenceRepository.findByLearnerIdOrderByUploadDateDesc(10L)).thenReturn(List.of());

        var analytics = service.getLearnerAnalytics(10L);

        assertThat(analytics.getTotalCount()).isEqualTo(0);
        assertThat(analytics.getApprovedCount()).isEqualTo(0);
        assertThat(analytics.getAverageScore()).isNull();
    }

    @Test
    void getLearnerAnalytics_withApprovedEvidence_calculatesAverage() {
        SkillEvidence approved = SkillEvidence.builder()
                .evidenceId(2).title("Test").learnerId(10L).learnerName("Alice")
                .learnerEmail("alice@example.com").uploadDate(LocalDate.now())
                .status(EvidenceStatus.APPROVED).score(80).category(EvidenceCategory.OTHER)
                .build();

        when(evidenceRepository.findByLearnerIdOrderByUploadDateDesc(10L)).thenReturn(List.of(approved));

        var analytics = service.getLearnerAnalytics(10L);

        assertThat(analytics.getApprovedCount()).isEqualTo(1);
        assertThat(analytics.getAverageScore()).isEqualTo(80.0);
    }
}
