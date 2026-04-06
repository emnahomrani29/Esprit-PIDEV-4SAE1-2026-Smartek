package com.smartek.learningmicroservice.controller;

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

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService pathService;

    @PostMapping
    public ResponseEntity<LearningPathResponse> createPath(@Valid @RequestBody LearningPathRequest request) {
        LearningPathResponse response = pathService.createPath(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer tous les parcours d'un apprenant
     */
    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<LearningPathResponse>> getPathsByLearner(@PathVariable Long learnerId) {
        List<LearningPathResponse> paths = pathService.getAllPathsByLearner(learnerId);
        return ResponseEntity.ok(paths);
    }

    /**
     * Récupérer tous les parcours (pour admin)
     */
    @GetMapping
    public ResponseEntity<List<LearningPathResponse>> getAllPaths() {
        List<LearningPathResponse> paths = pathService.getAllPaths();
        return ResponseEntity.ok(paths);
    }

    /**
     * Récupérer un parcours par ID
     */
    @GetMapping("/{pathId}")
    public ResponseEntity<LearningPathResponse> getPathById(@PathVariable Long pathId) {
        LearningPathResponse path = pathService.getPathById(pathId);
        return ResponseEntity.ok(path);
    }

    /**
     * Récupérer les parcours par statut
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LearningPathResponse>> getPathsByStatus(@PathVariable LearningPathStatus status) {
        List<LearningPathResponse> paths = pathService.getPathsByStatus(status);
        return ResponseEntity.ok(paths);
    }

    /**
     * Récupérer les parcours d'un apprenant par statut
     */
    @GetMapping("/learner/{learnerId}/status/{status}")
    public ResponseEntity<List<LearningPathResponse>> getPathsByLearnerAndStatus(
            @PathVariable Long learnerId,
            @PathVariable LearningPathStatus status) {
        List<LearningPathResponse> paths = pathService.getPathsByLearnerAndStatus(learnerId, status);
        return ResponseEntity.ok(paths);
    }

    /**
     * Mettre à jour un parcours
     */
    @PutMapping("/{pathId}")
    public ResponseEntity<LearningPathResponse> updatePath(
            @PathVariable Long pathId,
            @Valid @RequestBody LearningPathRequest request) {
        LearningPathResponse response = pathService.updatePath(pathId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un parcours
     */
    @DeleteMapping("/{pathId}")
    public ResponseEntity<Void> deletePath(@PathVariable Long pathId) {
        pathService.deletePath(pathId);
        return ResponseEntity.noContent().build();
    }
}
