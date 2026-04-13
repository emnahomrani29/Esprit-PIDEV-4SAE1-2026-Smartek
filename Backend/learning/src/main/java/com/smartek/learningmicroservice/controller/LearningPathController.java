package com.smartek.learningmicroservice.controller;

import com.smartek.learningmicroservice.client.SkillEvidenceClient;
import com.smartek.learningmicroservice.dto.LearningPathRequest;
import com.smartek.learningmicroservice.dto.LearningPathResponse;
import com.smartek.learningmicroservice.entity.LearningPathStatus;
import com.smartek.learningmicroservice.service.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService pathService;
    private final SkillEvidenceClient skillEvidenceClient;

    @PostMapping
    public ResponseEntity<LearningPathResponse> createPath(@Valid @RequestBody LearningPathRequest request) {
        LearningPathResponse response = pathService.createPath(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<LearningPathResponse>> getPathsByLearner(@PathVariable Long learnerId) {
        return ResponseEntity.ok(pathService.getAllPathsByLearner(learnerId));
    }

    @GetMapping
    public ResponseEntity<List<LearningPathResponse>> getAllPaths() {
        return ResponseEntity.ok(pathService.getAllPaths());
    }

    @GetMapping("/{pathId}")
    public ResponseEntity<LearningPathResponse> getPathById(@PathVariable Long pathId) {
        return ResponseEntity.ok(pathService.getPathById(pathId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LearningPathResponse>> getPathsByStatus(@PathVariable LearningPathStatus status) {
        return ResponseEntity.ok(pathService.getPathsByStatus(status));
    }

    @GetMapping("/learner/{learnerId}/status/{status}")
    public ResponseEntity<List<LearningPathResponse>> getPathsByLearnerAndStatus(
            @PathVariable Long learnerId,
            @PathVariable LearningPathStatus status) {
        return ResponseEntity.ok(pathService.getPathsByLearnerAndStatus(learnerId, status));
    }

    @PutMapping("/{pathId}")
    public ResponseEntity<LearningPathResponse> updatePath(
            @PathVariable Long pathId,
            @Valid @RequestBody LearningPathRequest request) {
        return ResponseEntity.ok(pathService.updatePath(pathId, request));
    }

    @DeleteMapping("/{pathId}")
    public ResponseEntity<Void> deletePath(@PathVariable Long pathId) {
        pathService.deletePath(pathId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint combiné : parcours + preuves de compétences d'un apprenant.
     * Utilise OpenFeign pour appeler skill-evidence-service.
     */
    @GetMapping("/learner/{learnerId}/dashboard")
    public ResponseEntity<Map<String, Object>> getLearnerDashboard(@PathVariable Long learnerId) {
        List<LearningPathResponse> paths = pathService.getAllPathsByLearner(learnerId);
        List<SkillEvidenceClient.SkillEvidenceSummary> evidences =
                skillEvidenceClient.getEvidencesByLearner(learnerId);
        SkillEvidenceClient.LearnerAnalyticsSummary analytics =
                skillEvidenceClient.getLearnerAnalytics(learnerId);

        return ResponseEntity.ok(Map.of(
                "learningPaths", paths,
                "skillEvidences", evidences,
                "analytics", analytics
        ));
    }
}
