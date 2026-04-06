package com.smartek.learningmicroservice.service;

import com.smartek.learningmicroservice.dto.LearningStylePreferenceRequest;
import com.smartek.learningmicroservice.dto.LearningStylePreferenceResponse;
import com.smartek.learningmicroservice.entity.LearningStylePreference;
import com.smartek.learningmicroservice.repository.LearningStylePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningStylePreferenceService {

    private final LearningStylePreferenceRepository repository;

    @Transactional
    public LearningStylePreferenceResponse createOrUpdatePreference(
            LearningStylePreferenceRequest request) {

        Optional<LearningStylePreference> existingOpt =
                repository.findByLearnerId(request.getLearnerId());

        LearningStylePreference preference = existingOpt.orElseGet(() ->
                LearningStylePreference.builder()
                        .learnerId(request.getLearnerId())
                        .learnerName(request.getLearnerName())
                        .build()
        );

        // Mise à jour des champs
        preference.setPreferredStyle(request.getPreferredStyle());
        preference.setVideoPreferred(
                Boolean.TRUE.equals(request.getVideoPreferred()));
        preference.setTextPreferred(
                Boolean.TRUE.equals(request.getTextPreferred()));
        preference.setPracticalWorkPreferred(
                Boolean.TRUE.equals(request.getPracticalWorkPreferred()));

        // learnerName peut être mis à jour si fourni (optionnel)
        if (request.getLearnerName() != null && !request.getLearnerName().isBlank()) {
            preference.setLearnerName(request.getLearnerName());
        }

        LearningStylePreference saved = repository.save(preference);
        return mapToResponse(saved);
    }

    public LearningStylePreferenceResponse getByLearnerId(Long learnerId) {
        LearningStylePreference pref = repository.findByLearnerId(learnerId)
                .orElseThrow(() -> new RuntimeException(
                        "No learning style preference found for learner: " + learnerId));

        return mapToResponse(pref);
    }

    public boolean existsForLearner(Long learnerId) {
        return repository.existsByLearnerId(learnerId);
    }

    @Transactional
    public void deleteByLearnerId(Long learnerId) {
        LearningStylePreference pref = repository.findByLearnerId(learnerId)
                .orElseThrow(() -> new RuntimeException(
                        "No preference to delete for learner: " + learnerId));

        repository.delete(pref);
    }

    public List<LearningStylePreferenceResponse> getAllPreferences() {
        List<LearningStylePreference> preferences = repository.findAll();
        return preferences.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LearningStylePreferenceResponse mapToResponse(LearningStylePreference p) {
        return new LearningStylePreferenceResponse(
                p.getId(),
                p.getPreferredStyle(),
                p.getVideoPreferred(),
                p.getTextPreferred(),
                p.getPracticalWorkPreferred(),
                p.getLastUpdated(),
                p.getLearnerId(),
                p.getLearnerName()
        );
    }
}
