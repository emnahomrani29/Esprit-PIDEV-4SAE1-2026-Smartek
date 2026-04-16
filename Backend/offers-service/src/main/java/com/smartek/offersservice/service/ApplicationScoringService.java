package com.smartek.offersservice.service;

import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service de scoring automatique des candidatures.
 *
 * Logique métier avancée :
 * Le score (0-100) est calculé selon 3 critères pondérés :
 *
 *  1. Correspondance des compétences (50 pts max)
 *     → Intersection entre les compétences requises et celles du candidat
 *
 *  2. Niveau d'expérience (30 pts max)
 *     → Correspondance entre le niveau requis et l'expérience déclarée
 *
 *  3. Qualité de la lettre de motivation (20 pts max)
 *     → Longueur et présence de mots-clés pertinents
 *
 * Ce score permet de trier automatiquement les candidatures et d'aider
 * les recruteurs à prioriser leur traitement.
 */
@Service
@Slf4j
public class ApplicationScoringService {

    private static final int MAX_SKILL_SCORE       = 50;
    private static final int MAX_EXPERIENCE_SCORE  = 30;
    private static final int MAX_COVER_LETTER_SCORE = 20;

    /**
     * Calcule le score d'une candidature par rapport à une offre.
     *
     * @param application la candidature à scorer
     * @param offer       l'offre concernée
     * @param candidateSkills compétences déclarées par le candidat
     * @param yearsOfExperience années d'expérience déclarées
     * @return score entre 0 et 100
     */
    public int calculateScore(Application application, Offer offer,
                               Set<String> candidateSkills, Integer yearsOfExperience) {
        int score = 0;

        score += calculateSkillScore(offer.getRequiredSkills(), candidateSkills);
        score += calculateExperienceScore(offer.getExperienceLevel(), yearsOfExperience);
        score += calculateCoverLetterScore(application.getCoverLetter());

        log.debug("Score calculé pour la candidature {} : {}/100", application.getId(), score);
        return Math.min(score, 100);
    }

    /**
     * Score de compétences : 50 pts max.
     * Formule : (compétences correspondantes / compétences requises) * 50
     */
    private int calculateSkillScore(Set<String> requiredSkills, Set<String> candidateSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return MAX_SKILL_SCORE / 2; // Score neutre si pas de compétences requises
        }
        if (candidateSkills == null || candidateSkills.isEmpty()) {
            return 0;
        }

        long matchCount = candidateSkills.stream()
                .filter(skill -> requiredSkills.stream()
                        .anyMatch(req -> req.equalsIgnoreCase(skill)))
                .count();

        double ratio = (double) matchCount / requiredSkills.size();
        return (int) Math.round(ratio * MAX_SKILL_SCORE);
    }

    /**
     * Score d'expérience : 30 pts max.
     * Correspondance entre le niveau requis et les années d'expérience.
     */
    private int calculateExperienceScore(Offer.ExperienceLevel requiredLevel, Integer yearsOfExperience) {
        if (requiredLevel == null) return MAX_EXPERIENCE_SCORE / 2;
        if (yearsOfExperience == null) return 0;

        return switch (requiredLevel) {
            case JUNIOR -> yearsOfExperience <= 2 ? MAX_EXPERIENCE_SCORE
                         : yearsOfExperience <= 4 ? MAX_EXPERIENCE_SCORE / 2
                         : 0;
            case MID    -> yearsOfExperience >= 2 && yearsOfExperience <= 5 ? MAX_EXPERIENCE_SCORE
                         : yearsOfExperience < 2 ? MAX_EXPERIENCE_SCORE / 3
                         : MAX_EXPERIENCE_SCORE * 2 / 3;
            case SENIOR -> yearsOfExperience >= 5 ? MAX_EXPERIENCE_SCORE
                         : yearsOfExperience >= 3 ? MAX_EXPERIENCE_SCORE / 2
                         : 0;
            case EXPERT -> yearsOfExperience >= 8 ? MAX_EXPERIENCE_SCORE
                         : yearsOfExperience >= 5 ? MAX_EXPERIENCE_SCORE / 2
                         : 0;
        };
    }

    /**
     * Score de la lettre de motivation : 20 pts max.
     * Basé sur la longueur et la présence de mots-clés professionnels.
     */
    private int calculateCoverLetterScore(String coverLetter) {
        if (coverLetter == null || coverLetter.isBlank()) return 0;

        int score = 0;
        int length = coverLetter.trim().length();

        // Points pour la longueur (min 200 chars pour un score complet)
        if (length >= 500) score += 10;
        else if (length >= 200) score += 7;
        else if (length >= 100) score += 4;
        else score += 1;

        // Points pour les mots-clés professionnels
        String lower = coverLetter.toLowerCase();
        String[] keywords = {"expérience", "compétence", "motivation", "projet",
                             "équipe", "résultat", "objectif", "contribution",
                             "experience", "skill", "team", "result"};
        long keywordCount = 0;
        for (String kw : keywords) {
            if (lower.contains(kw)) keywordCount++;
        }
        score += Math.min((int) keywordCount, 10);

        return Math.min(score, MAX_COVER_LETTER_SCORE);
    }
}
