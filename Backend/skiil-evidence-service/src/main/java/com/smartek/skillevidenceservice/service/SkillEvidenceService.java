package com.smartek.skillevidenceservice.service;

import com.smartek.skillevidenceservice.dto.GlobalAnalyticsDTO;
import com.smartek.skillevidenceservice.dto.LearnerAnalyticsDTO;
import com.smartek.skillevidenceservice.dto.SkillEvidenceRequest;
import com.smartek.skillevidenceservice.dto.SkillEvidenceResponse;
import com.smartek.skillevidenceservice.entity.SkillEvidence;
import com.smartek.skillevidenceservice.repository.SkillEvidenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillEvidenceService {

    private final SkillEvidenceRepository evidenceRepository;
    private final NotificationService notificationService;

    @Transactional
    public SkillEvidenceResponse createEvidence(SkillEvidenceRequest request) {

        if (evidenceRepository.existsByLearnerIdAndTitle(
                request.getLearnerId(), request.getTitle())) {
            throw new RuntimeException("Une preuve avec ce titre existe déjà pour cet apprenant");
        }

        SkillEvidence evidence = SkillEvidence.builder()
                .title(request.getTitle())
                .fileUrl(request.getFileUrl())
                .description(request.getDescription())
                .learnerId(request.getLearnerId())
                .learnerName(request.getLearnerName())
                .learnerEmail(request.getLearnerEmail())
                // uploadDate est géré par @PrePersist
                .build();

        SkillEvidence saved = evidenceRepository.save(evidence);
        return mapToResponse(saved);
    }

    public List<SkillEvidenceResponse> getAllEvidenceByLearner(Long learnerId) {
        return evidenceRepository.findByLearnerIdOrderByUploadDateDesc(learnerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SkillEvidenceResponse> getAllEvidence() {
        return evidenceRepository.findAll()
                .stream()
                .sorted((e1, e2) -> e2.getUploadDate().compareTo(e1.getUploadDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SkillEvidenceResponse getEvidenceById(Integer id) {
        SkillEvidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preuve de compétence non trouvée"));
        return mapToResponse(evidence);
    }

  @Transactional
public SkillEvidenceResponse updateEvidence(Integer id, SkillEvidenceRequest request) {
    SkillEvidence evidence = evidenceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Preuve de compétence non trouvée"));

    // Vérifier les doublons (exclure l'ID actuel)
    if (evidenceRepository.existsByLearnerIdAndTitleAndEvidenceIdNot(
            evidence.getLearnerId(), request.getTitle(), id)) {
        throw new RuntimeException("Une preuve avec ce titre existe déjà pour cet apprenant");
    }

    evidence.setTitle(request.getTitle());
    evidence.setFileUrl(request.getFileUrl());
    evidence.setDescription(request.getDescription());

    SkillEvidence updated = evidenceRepository.save(evidence);
    return mapToResponse(updated);
}


    @Transactional
    public void deleteEvidence(Integer id) {
        if (!evidenceRepository.existsById(id)) {
            throw new RuntimeException("Preuve de compétence non trouvée");
        }
        evidenceRepository.deleteById(id);
    }

    private SkillEvidenceResponse mapToResponse(SkillEvidence e) {
        return new SkillEvidenceResponse(
                e.getEvidenceId(),
                e.getTitle(),
                e.getFileUrl(),
                e.getDescription(),
                e.getUploadDate(),
                e.getLearnerId(),
                e.getLearnerName(),
                e.getLearnerEmail(),
                e.getStatus(),
                e.getScore(),
                e.getAdminComment(),
                e.getReviewedBy(),
                e.getReviewedAt(),
                e.getCategory()
        );
    }

    // ========== VALIDATION METHODS ==========

    @Transactional
    public SkillEvidence reviewEvidence(Integer evidenceId, com.smartek.skillevidenceservice.entity.EvidenceStatus status, 
                                        Integer score, String adminComment, Long reviewerId) {
        SkillEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found with id: " + evidenceId));

        // Validate score required for APPROVED status
        if (status == com.smartek.skillevidenceservice.entity.EvidenceStatus.APPROVED) {
            if (score == null || score < 0 || score > 100) {
                throw new IllegalArgumentException("Score is required when approving evidence and must be between 0 and 100");
            }
        }
        
        // Validate comment required for REJECTED status
        if (status == com.smartek.skillevidenceservice.entity.EvidenceStatus.REJECTED) {
            if (adminComment == null || adminComment.trim().isEmpty()) {
                throw new IllegalArgumentException("Comment is required when rejecting evidence");
            }
        }

        // Update evidence
        evidence.setStatus(status);
        evidence.setScore(score);
        evidence.setAdminComment(adminComment);
        evidence.setReviewedBy(reviewerId);
        evidence.setReviewedAt(java.time.LocalDate.now());

        return evidenceRepository.save(evidence);
    }

    @Transactional
    public SkillEvidence approveEvidence(Integer evidenceId, Integer score, Long reviewerId, String adminComment) {
        // Validate score range
        if (score == null || score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100");
        }
        
        SkillEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found with id: " + evidenceId));

        // Update status to APPROVED
        evidence.setStatus(com.smartek.skillevidenceservice.entity.EvidenceStatus.APPROVED);
        evidence.setScore(score);
        evidence.setAdminComment(adminComment);
        evidence.setReviewedBy(reviewerId);
        evidence.setReviewedAt(java.time.LocalDate.now());

        SkillEvidence saved = evidenceRepository.save(evidence);

        // Créer une notification temporaire avec le commentaire
        if (adminComment != null && !adminComment.trim().isEmpty()) {
            String message = String.format("Your evidence '%s' has been approved with a score of %d/100. Admin comment: %s", 
                evidence.getTitle(), score, adminComment);
            notificationService.createNotification(
                evidence.getLearnerId(), 
                evidenceId, 
                message, 
                com.smartek.skillevidenceservice.entity.NotificationType.APPROVAL
            );
        } else {
            String message = String.format("Your evidence '%s' has been approved with a score of %d/100.", 
                evidence.getTitle(), score);
            notificationService.createNotification(
                evidence.getLearnerId(), 
                evidenceId, 
                message, 
                com.smartek.skillevidenceservice.entity.NotificationType.APPROVAL
            );
        }

        return saved;
    }

    @Transactional
    public SkillEvidence rejectEvidence(Integer evidenceId, String adminComment, Long reviewerId) {
        // Validate non-empty comment
        if (adminComment == null || adminComment.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin comment is required for rejection");
        }
        
        SkillEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found with id: " + evidenceId));

        // Update status to REJECTED
        evidence.setStatus(com.smartek.skillevidenceservice.entity.EvidenceStatus.REJECTED);
        evidence.setScore(null);
        evidence.setAdminComment(adminComment);
        evidence.setReviewedBy(reviewerId);
        evidence.setReviewedAt(java.time.LocalDate.now());

        SkillEvidence saved = evidenceRepository.save(evidence);

        // Créer une notification temporaire avec le commentaire
        String message = String.format("Your evidence '%s' has been rejected. Admin comment: %s", 
            evidence.getTitle(), adminComment);
        notificationService.createNotification(
            evidence.getLearnerId(), 
            evidenceId, 
            message, 
            com.smartek.skillevidenceservice.entity.NotificationType.REJECTION
        );

        return saved;
    }

    // ========== ANALYTICS METHODS ==========

    public LearnerAnalyticsDTO getLearnerAnalytics(Long learnerId) {
        List<SkillEvidence> evidences = evidenceRepository.findByLearnerIdOrderByUploadDateDesc(learnerId);

        // Calculate counts
        int totalCount = evidences.size();
        int approvedCount = (int) evidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.APPROVED)
                .count();
        int pendingCount = (int) evidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.PENDING)
                .count();
        int rejectedCount = (int) evidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.REJECTED)
                .count();

        // Calculate average score (null if no approved evidence)
        Double averageScore = evidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.APPROVED && e.getScore() != null)
                .mapToInt(SkillEvidence::getScore)
                .average()
                .orElse(0.0);
        
        if (approvedCount == 0 || evidences.stream().noneMatch(e -> e.getScore() != null)) {
            averageScore = null;
        }

        // Group by category
        java.util.Map<com.smartek.skillevidenceservice.entity.EvidenceCategory, Integer> categoryDistribution = 
            evidences.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    e -> e.getCategory() != null ? e.getCategory() : com.smartek.skillevidenceservice.entity.EvidenceCategory.OTHER,
                    java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.counting(), Long::intValue)
                ));

        // Build score trend (ordered by uploadDate)
        List<LearnerAnalyticsDTO.ScoreTrendPoint> scoreTrend = evidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.APPROVED && e.getScore() != null)
                .sorted(java.util.Comparator.comparing(SkillEvidence::getUploadDate))
                .map(e -> LearnerAnalyticsDTO.ScoreTrendPoint.builder()
                        .date(e.getUploadDate())
                        .score(e.getScore())
                        .title(e.getTitle())
                        .build())
                .collect(Collectors.toList());

        return LearnerAnalyticsDTO.builder()
                .totalCount(totalCount)
                .approvedCount(approvedCount)
                .pendingCount(pendingCount)
                .rejectedCount(rejectedCount)
                .averageScore(averageScore)
                .categoryDistribution(categoryDistribution)
                .scoreTrend(scoreTrend)
                .build();
    }

    public com.smartek.skillevidenceservice.dto.EvidenceAnalyticsResponse getGlobalAnalytics() {
        List<SkillEvidence> allEvidences = evidenceRepository.findAll();

        if (allEvidences.isEmpty()) {
            return com.smartek.skillevidenceservice.dto.EvidenceAnalyticsResponse.builder()
                    .totalEvidences(0L)
                    .approvedEvidences(0L)
                    .pendingEvidences(0L)
                    .rejectedEvidences(0L)
                    .averageScore(0.0)
                    .approvalRate(0.0)
                    .build();
        }

        long total = allEvidences.size();
        long approved = allEvidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.APPROVED)
                .count();
        long pending = allEvidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.PENDING)
                .count();
        long rejected = allEvidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.REJECTED)
                .count();

        double avgScore = allEvidences.stream()
                .filter(e -> e.getScore() != null)
                .mapToInt(SkillEvidence::getScore)
                .average()
                .orElse(0.0);

        double approvalRate = total > 0 ? (approved * 100.0 / total) : 0.0;

        // Statistics by category
        java.util.Map<String, Long> byCategory = allEvidences.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getCategory() != null ? e.getCategory().name() : "OTHER",
                        java.util.stream.Collectors.counting()
                ));

        java.util.Map<String, Double> avgScoreByCategory = allEvidences.stream()
                .filter(e -> e.getScore() != null && e.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getCategory().name(),
                        java.util.stream.Collectors.averagingInt(SkillEvidence::getScore)
                ));

        return com.smartek.skillevidenceservice.dto.EvidenceAnalyticsResponse.builder()
                .totalEvidences(total)
                .approvedEvidences(approved)
                .pendingEvidences(pending)
                .rejectedEvidences(rejected)
                .averageScore(avgScore)
                .approvalRate(approvalRate)
                .evidencesByCategory(byCategory)
                .averageScoreByCategory(avgScoreByCategory)
                .build();
    }

    public GlobalAnalyticsDTO getGlobalAnalyticsNew() {
        List<SkillEvidence> allEvidences = evidenceRepository.findAll();

        // Calculate counts
        int totalCount = allEvidences.size();
        int approvedCount = (int) allEvidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.APPROVED)
                .count();
        int pendingCount = (int) allEvidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.PENDING)
                .count();
        int rejectedCount = (int) allEvidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.REJECTED)
                .count();

        // Calculate approval rate
        Double approvalRate = (approvedCount + rejectedCount) > 0 
            ? (double) approvedCount / (approvedCount + rejectedCount) 
            : 0.0;

        // Calculate average score (null if no approved evidence)
        Double averageScore = allEvidences.stream()
                .filter(e -> e.getStatus() == com.smartek.skillevidenceservice.entity.EvidenceStatus.APPROVED && e.getScore() != null)
                .mapToInt(SkillEvidence::getScore)
                .average()
                .orElse(0.0);
        
        if (approvedCount == 0 || allEvidences.stream().noneMatch(e -> e.getScore() != null)) {
            averageScore = null;
        }

        // Group by category
        java.util.Map<com.smartek.skillevidenceservice.entity.EvidenceCategory, Integer> categoryDistribution = 
            allEvidences.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    e -> e.getCategory() != null ? e.getCategory() : com.smartek.skillevidenceservice.entity.EvidenceCategory.OTHER,
                    java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.counting(), Long::intValue)
                ));

        // Group by uploadDate for submission trend
        java.util.Map<java.time.LocalDate, Integer> submissionTrend = 
            allEvidences.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    SkillEvidence::getUploadDate,
                    java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.counting(), Long::intValue)
                ));

        // Status distribution
        java.util.Map<com.smartek.skillevidenceservice.entity.EvidenceStatus, Integer> statusDistribution = 
            allEvidences.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    SkillEvidence::getStatus,
                    java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.counting(), Long::intValue)
                ));

        return GlobalAnalyticsDTO.builder()
                .totalCount(totalCount)
                .approvedCount(approvedCount)
                .pendingCount(pendingCount)
                .rejectedCount(rejectedCount)
                .approvalRate(approvalRate)
                .averageScore(averageScore)
                .categoryDistribution(categoryDistribution)
                .submissionTrend(submissionTrend)
                .statusDistribution(statusDistribution)
                .build();
    }

    public List<SkillEvidenceResponse> getEvidencesByStatus(com.smartek.skillevidenceservice.entity.EvidenceStatus status) {
        return evidenceRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SkillEvidenceResponse> getEvidencesByCategory(com.smartek.skillevidenceservice.entity.EvidenceCategory category) {
        return evidenceRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}