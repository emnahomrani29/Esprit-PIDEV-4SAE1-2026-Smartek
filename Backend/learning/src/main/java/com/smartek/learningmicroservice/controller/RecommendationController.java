package com.smartek.learningmicroservice.controller;

import com.smartek.learningmicroservice.client.RecommendationClient;
import com.smartek.learningmicroservice.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * GET /api/recommendations/learner/{learnerId}?topN=5
     * Retourne les parcours recommandés pour un learner via le moteur ML.
     */
    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<RecommendationClient.RecommendationResponse> getRecommendations(
            @PathVariable Long learnerId,
            @RequestParam(defaultValue = "5") int topN) {

        RecommendationClient.RecommendationResponse response =
                recommendationService.getRecommendationsForLearner(learnerId, topN);
        return ResponseEntity.ok(response);
    }
}
