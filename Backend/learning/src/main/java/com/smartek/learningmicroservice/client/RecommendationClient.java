package com.smartek.learningmicroservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Client Feign pour le microservice ML de recommandation (Python/FastAPI).
 */
@FeignClient(name = "recommendation-service", url = "${recommendation.service.url:http://recommendation-service:8095}")
public interface RecommendationClient {

    @PostMapping("/api/recommendations")
    RecommendationResponse getRecommendations(@RequestBody RecommendationRequest request);

    // ─── DTOs ────────────────────────────────────────────────────────────────

    record LearnerProfile(
            Long learnerId,
            List<String> completedPaths,
            List<String> skillCategories,
            String preferredStyle,
            Double averageScore,
            Integer progress
    ) {}

    record AvailablePath(
            Long pathId,
            String title,
            String description,
            List<String> tags
    ) {}

    record RecommendationRequest(
            LearnerProfile learner,
            List<AvailablePath> availablePaths,
            int topN
    ) {}

    record RecommendationItem(
            Long pathId,
            String title,
            Double score,
            String reason
    ) {}

    record RecommendationResponse(
            Long learnerId,
            List<RecommendationItem> recommendations
    ) {}
}
