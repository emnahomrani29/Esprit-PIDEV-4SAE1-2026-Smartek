package com.smartek.offersservice.service;

import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service de scoring automatique des candidatures.
 *
 * Le score (0-100) est calculé selon 2 critères :
 *  1. Niveau d'expérience (50 pts max) — correspondance entre le niveau requis et les années déclarées
 *  2. Qualité de la lettre de motivation (50 pts max) — longueur et mots-clés
 */
@Service
@Slf4j
public class ApplicationScoringService {

    private static final int MAX_EXPERIENCE_SCORE = 50;
    private static final int MAX_COVER_LETTER_SCORE = 50;

    /**
     * Calcule le score d'une candidature par rapport à une offre.
     *
     * @param application       la candidature à scorer
     * @param offer             l'offre concernée
     * @param yearsOfExperience années d'expérience déclarées
     * @return score entre 0 et 100
     */
    public int calculateScore(Application application, Offer offer, Integer yearsOfExperience) {
        int score = 0;
        score += calculateExperienceScore(offer.getExperienceLevel(), yearsOfExperience);
        score += calculateCoverLetterScore(application.getCoverLetter());
        log.debug("Score calculé pour la candidature {} : {}/100", application.getId(), score);
        return Math.min(score, 100);
    }

    /**
     * Score d'expérience : 50 pts max.
     * Basé sur la correspondance entre le niveau requis (String) et les années.
     */
    private int calculateExperienceScore(String requiredLevel, Integer yearsOfExperience) {
        if (requiredLevel == null || requiredLevel.isBlank()) return MAX_EXPERIENCE_SCORE / 2;
        if (yearsOfExperience == null) return 0;

        return switch (requiredLevel.toUpperCase()) {
            case "JUNIOR" -> yearsOfExperience <= 2 ? MAX_EXPERIENCE_SCORE
                           : yearsOfExperience <= 4 ? MAX_EXPERIENCE_SCORE / 2
                           : 0;
            case "MID"    -> yearsOfExperience >= 2 && yearsOfExperience <= 5 ? MAX_EXPERIENCE_SCORE
                           : yearsOfExperience < 2 ? MAX_EXPERIENCE_SCORE / 3
                           : MAX_EXPERIENCE_SCORE * 2 / 3;
            case "SENIOR" -> yearsOfExperience >= 5 ? MAX_EXPERIENCE_SCORE
                           : yearsOfExperience >= 3 ? MAX_EXPERIENCE_SCORE / 2
                           : 0;
            case "EXPERT" -> yearsOfExperience >= 8 ? MAX_EXPERIENCE_SCORE
                           : yearsOfExperience >= 5 ? MAX_EXPERIENCE_SCORE / 2
                           : 0;
            default       -> MAX_EXPERIENCE_SCORE / 2;
        };
    }

    /**
     * Score de la lettre de motivation : 50 pts max.
     */
    private int calculateCoverLetterScore(String coverLetter) {
        if (coverLetter == null || coverLetter.isBlank()) return 0;

        int score = 0;
        int length = coverLetter.trim().length();

        if (length >= 500) score += 25;
        else if (length >= 200) score += 18;
        else if (length >= 100) score += 10;
        else score += 3;

        String lower = coverLetter.toLowerCase();
        String[] keywords = {"expérience", "compétence", "motivation", "projet",
                             "équipe", "résultat", "objectif", "contribution",
                             "experience", "skill", "team", "result"};
        long keywordCount = 0;
        for (String kw : keywords) {
            if (lower.contains(kw)) keywordCount++;
        }
        score += Math.min((int) keywordCount * 2, 25);

        return Math.min(score, MAX_COVER_LETTER_SCORE);
    }
}
