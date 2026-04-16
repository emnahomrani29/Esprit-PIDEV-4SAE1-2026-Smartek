package com.smartek.offersservice.service;

import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de scoring automatique des candidatures.
 * Score (0-100) basé sur 3 critères :
 *  1. Compétences requises (40 pts max)
 *  2. Niveau d'expérience (30 pts max)
 *  3. Lettre de motivation (30 pts max)
 */
@Service
@Slf4j
public class ApplicationScoringService {

    private static final int MAX_SKILL_SCORE       = 40;
    private static final int MAX_EXPERIENCE_SCORE  = 30;
    private static final int MAX_COVER_LETTER_SCORE = 30;

    public int calculateScore(Application application, Offer offer, Integer yearsOfExperience) {
        int score = 0;
        score += calculateSkillScore(offer.getRequiredSkills(), application.getCoverLetter());
        score += calculateExperienceScore(offer.getExperienceLevel(), yearsOfExperience);
        score += calculateCoverLetterScore(application.getCoverLetter());
        int total = Math.min(score, 100);
        log.debug("Score candidature {} : {}/100", application.getId(), total);
        return total;
    }

    /** Analyse complète : score + compétences manquantes + suggestions */
    public Map<String, Object> analyzeMatch(Application application, Offer offer, Integer yearsOfExperience) {
        Set<String> required = offer.getRequiredSkills() != null ? offer.getRequiredSkills() : new HashSet<>();
        String coverLetter = application.getCoverLetter() != null ? application.getCoverLetter().toLowerCase() : "";

        // Compétences trouvées dans la lettre de motivation
        List<String> matchedSkills = required.stream()
                .filter(s -> coverLetter.contains(s.toLowerCase()))
                .collect(Collectors.toList());

        List<String> missingSkills = required.stream()
                .filter(s -> !coverLetter.contains(s.toLowerCase()))
                .collect(Collectors.toList());

        int skillScore = calculateSkillScore(required, application.getCoverLetter());
        int expScore   = calculateExperienceScore(offer.getExperienceLevel(), yearsOfExperience);
        int clScore    = calculateCoverLetterScore(application.getCoverLetter());
        int total      = Math.min(skillScore + expScore + clScore, 100);

        // Suggestions d'amélioration
        List<String> suggestions = new ArrayList<>();
        if (!missingSkills.isEmpty()) {
            suggestions.add("Mentionnez ces compétences dans votre lettre : " + String.join(", ", missingSkills));
        }
        if (application.getCoverLetter() == null || application.getCoverLetter().length() < 200) {
            suggestions.add("Rédigez une lettre de motivation plus détaillée (minimum 200 caractères)");
        }
        if (yearsOfExperience == null) {
            suggestions.add("Précisez vos années d'expérience lors de la candidature");
        }
        if (offer.getExperienceLevel() != null && yearsOfExperience != null) {
            String level = offer.getExperienceLevel().toUpperCase();
            if ("SENIOR".equals(level) && yearsOfExperience < 5) {
                suggestions.add("Ce poste requiert 5+ ans d'expérience (vous avez " + yearsOfExperience + " ans)");
            } else if ("EXPERT".equals(level) && yearsOfExperience < 8) {
                suggestions.add("Ce poste requiert 8+ ans d'expérience (vous avez " + yearsOfExperience + " ans)");
            }
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Votre profil correspond bien à cette offre !");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalScore", total);
        result.put("skillScore", skillScore);
        result.put("experienceScore", expScore);
        result.put("coverLetterScore", clScore);
        result.put("matchedSkills", matchedSkills);
        result.put("missingSkills", missingSkills);
        result.put("suggestions", suggestions);
        result.put("matchLevel", total >= 75 ? "EXCELLENT" : total >= 50 ? "BON" : total >= 25 ? "MOYEN" : "FAIBLE");
        return result;
    }

    private int calculateSkillScore(Set<String> requiredSkills, String coverLetter) {
        if (requiredSkills == null || requiredSkills.isEmpty()) return MAX_SKILL_SCORE / 2;
        if (coverLetter == null || coverLetter.isBlank()) return 0;
        String lower = coverLetter.toLowerCase();
        long matched = requiredSkills.stream().filter(s -> lower.contains(s.toLowerCase())).count();
        return (int) Math.round((double) matched / requiredSkills.size() * MAX_SKILL_SCORE);
    }

    private int calculateExperienceScore(String requiredLevel, Integer yearsOfExperience) {
        if (requiredLevel == null || requiredLevel.isBlank()) return MAX_EXPERIENCE_SCORE / 2;
        if (yearsOfExperience == null) return 0;
        return switch (requiredLevel.toUpperCase()) {
            case "JUNIOR" -> yearsOfExperience <= 2 ? MAX_EXPERIENCE_SCORE
                           : yearsOfExperience <= 4 ? MAX_EXPERIENCE_SCORE / 2 : 0;
            case "MID"    -> yearsOfExperience >= 2 && yearsOfExperience <= 5 ? MAX_EXPERIENCE_SCORE
                           : yearsOfExperience < 2 ? MAX_EXPERIENCE_SCORE / 3 : MAX_EXPERIENCE_SCORE * 2 / 3;
            case "SENIOR" -> yearsOfExperience >= 5 ? MAX_EXPERIENCE_SCORE
                           : yearsOfExperience >= 3 ? MAX_EXPERIENCE_SCORE / 2 : 0;
            case "EXPERT" -> yearsOfExperience >= 8 ? MAX_EXPERIENCE_SCORE
                           : yearsOfExperience >= 5 ? MAX_EXPERIENCE_SCORE / 2 : 0;
            default       -> MAX_EXPERIENCE_SCORE / 2;
        };
    }

    private int calculateCoverLetterScore(String coverLetter) {
        if (coverLetter == null || coverLetter.isBlank()) return 0;
        int score = 0;
        int length = coverLetter.trim().length();
        if (length >= 500) score += 15;
        else if (length >= 200) score += 10;
        else if (length >= 100) score += 5;
        else score += 2;
        String lower = coverLetter.toLowerCase();
        String[] keywords = {"expérience", "compétence", "motivation", "projet", "équipe",
                             "résultat", "objectif", "contribution", "experience", "skill", "team", "result"};
        long kw = Arrays.stream(keywords).filter(lower::contains).count();
        score += Math.min((int) kw, 15);
        return Math.min(score, MAX_COVER_LETTER_SCORE);
    }
}
