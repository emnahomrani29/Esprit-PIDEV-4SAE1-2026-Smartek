package com.smartek.learningmicroservice.service;

import com.smartek.learningmicroservice.client.RecommendationClient;
import com.smartek.learningmicroservice.client.SkillEvidenceClient;
import com.smartek.learningmicroservice.entity.LearningPath;
import com.smartek.learningmicroservice.entity.LearningPathStatus;
import com.smartek.learningmicroservice.entity.LearningStylePreference;
import com.smartek.learningmicroservice.repository.LearningPathRepository;
import com.smartek.learningmicroservice.repository.LearningStylePreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RecommendationClient recommendationClient;
    private final SkillEvidenceClient skillEvidenceClient;
    private final LearningPathRepository pathRepository;
    private final LearningStylePreferenceRepository styleRepository;

    public RecommendationService(RecommendationClient recommendationClient,
                                 SkillEvidenceClient skillEvidenceClient,
                                 LearningPathRepository pathRepository,
                                 LearningStylePreferenceRepository styleRepository) {
        this.recommendationClient = recommendationClient;
        this.skillEvidenceClient = skillEvidenceClient;
        this.pathRepository = pathRepository;
        this.styleRepository = styleRepository;
    }

    /**
     * Génère des recommandations personnalisées pour un learner.
     * Agrège : parcours complétés + compétences validées + style d'apprentissage.
     */
    public RecommendationClient.RecommendationResponse getRecommendationsForLearner(Long learnerId, int topN) {

        // 1. Parcours complétés par le learner
        List<LearningPath> completedPaths = pathRepository.findByLearnerIdAndStatus(learnerId, LearningPathStatus.TERMINE);
        List<String> completedTitles = completedPaths.stream()
                .map(LearningPath::getTitle)
                .collect(Collectors.toList());

        // 2. Compétences validées via skill-evidence-service
        List<String> skillCategories = new ArrayList<>();
        Double averageScore = null;
        try {
            SkillEvidenceClient.LearnerAnalyticsSummary analytics = skillEvidenceClient.getLearnerAnalytics(learnerId);
            averageScore = analytics.averageScore();

            List<SkillEvidenceClient.SkillEvidenceSummary> evidences = skillEvidenceClient.getEvidencesByLearner(learnerId);
            skillCategories = evidences.stream()
                    .filter(e -> "APPROVED".equalsIgnoreCase(e.status()))
                    .map(SkillEvidenceClient.SkillEvidenceSummary::category)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Impossible de récupérer les compétences du learner {} : {}", learnerId, e.getMessage());
        }

        // 3. Style d'apprentissage préféré
        String preferredStyle = styleRepository.findByLearnerId(learnerId)
                .map(pref -> pref.getPreferredStyle().name())
                .orElse(null);

        // 4. Progression moyenne
        Integer avgProgress = completedPaths.isEmpty() ? null :
                (int) completedPaths.stream().mapToInt(LearningPath::getProgress).average().orElse(0);

        // 5. Parcours disponibles (non encore commencés par ce learner)
        List<Long> learnerPathIds = pathRepository.findByLearnerIdOrderByStartDateDesc(learnerId)
                .stream().map(LearningPath::getPathId).collect(Collectors.toList());

        List<RecommendationClient.AvailablePath> availablePaths = pathRepository.findAll()
                .stream()
                .filter(p -> !p.getLearnerId().equals(learnerId))
                .map(p -> new RecommendationClient.AvailablePath(
                        p.getPathId(),
                        p.getTitle(),
                        p.getDescription(),
                        extractTags(p)
                ))
                .distinct()
                .collect(Collectors.toList());

        // 6. Construire le profil learner et appeler le service ML
        RecommendationClient.LearnerProfile profile = new RecommendationClient.LearnerProfile(
                learnerId, completedTitles, skillCategories, preferredStyle, averageScore, avgProgress
        );

        RecommendationClient.RecommendationRequest request = new RecommendationClient.RecommendationRequest(
                profile, availablePaths, topN
        );

        return recommendationClient.getRecommendations(request);
    }

    /**
     * Extrait des tags depuis le titre et la description d'un parcours.
     * En production, ces tags seraient stockés en base.
     */
    private List<String> extractTags(LearningPath path) {
        List<String> tags = new ArrayList<>();
        if (path.getTitle() != null) {
            // Extraire des mots-clés simples du titre
            for (String word : path.getTitle().split("\\s+")) {
                if (word.length() > 3) tags.add(word.toLowerCase());
            }
        }
        return tags;
    }
}
