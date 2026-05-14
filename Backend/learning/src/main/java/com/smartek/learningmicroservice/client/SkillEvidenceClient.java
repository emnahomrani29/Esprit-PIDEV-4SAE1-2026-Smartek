package com.smartek.learningmicroservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Client Feign pour communiquer avec le skill-evidence-service.
 * Permet au learning-service de récupérer les preuves de compétences d'un apprenant.
 */
@FeignClient(name = "skill-evidence-service", path = "/api/skill-evidence")
public interface SkillEvidenceClient {

    @GetMapping("/learner/{learnerId}")
    List<SkillEvidenceSummary> getEvidencesByLearner(@PathVariable("learnerId") Long learnerId);

    @GetMapping("/analytics/learner/{learnerId}")
    LearnerAnalyticsSummary getLearnerAnalytics(@PathVariable("learnerId") Long learnerId);

    // DTO interne minimal (évite la dépendance circulaire)
    record SkillEvidenceSummary(
            Integer evidenceId,
            String title,
            String status,
            Integer score,
            String category,
            String uploadDate
    ) {}

    record LearnerAnalyticsSummary(
            int totalCount,
            int approvedCount,
            int pendingCount,
            int rejectedCount,
            Double averageScore
    ) {}
}
