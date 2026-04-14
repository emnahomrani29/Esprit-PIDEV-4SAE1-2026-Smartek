package com.smartek.learningmicroservice.controller;

import com.smartek.learningmicroservice.dto.LearningStylePreferenceRequest;
import com.smartek.learningmicroservice.dto.LearningStylePreferenceResponse;
import com.smartek.learningmicroservice.service.LearningStylePreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-style-preferences")
public class LearningStylePreferenceController {

    private final LearningStylePreferenceService service;

    public LearningStylePreferenceController(LearningStylePreferenceService service) {
        this.service = service;
    }

    /**
     * CREATE or UPDATE (upsert) - le plus courant pour ce type d'entité
     */
    @PostMapping
    public ResponseEntity<LearningStylePreferenceResponse> savePreference(
            @Valid @RequestBody LearningStylePreferenceRequest request) {

        LearningStylePreferenceResponse response = service.createOrUpdatePreference(request);
        return ResponseEntity.ok(response);  // 200 OK même en création (car upsert)
    }

    @GetMapping
    public ResponseEntity<List<LearningStylePreferenceResponse>> getAllPreferences() {
        List<LearningStylePreferenceResponse> preferences = service.getAllPreferences();
        return ResponseEntity.ok(preferences);
    }

    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<LearningStylePreferenceResponse> getPreference(
            @PathVariable Long learnerId) {
        return ResponseEntity.ok(service.getByLearnerId(learnerId));
    }

    @GetMapping("/learner/{learnerId}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable Long learnerId) {
        return ResponseEntity.ok(service.existsForLearner(learnerId));
    }

    @DeleteMapping("/learner/{learnerId}")
    public ResponseEntity<Void> deletePreference(@PathVariable Long learnerId) {
        service.deleteByLearnerId(learnerId);
        return ResponseEntity.noContent().build();
    }
}
