package com.smartek.offersservice.service;

import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires exhaustifs pour ApplicationScoringService.
 * Couvre les 3 critères : compétences (40pts), expérience (30pts), lettre (30pts).
 */
@DisplayName("ApplicationScoringService — Tests unitaires")
class ApplicationScoringServiceTest {

    private ApplicationScoringService scoringService;

    @BeforeEach
    void setUp() { scoringService = new ApplicationScoringService(); }

    // ─── SCORE DE COMPÉTENCES (40 pts max) ───────────────────────────────────

    @Nested
    @DisplayName("Score de compétences (40 pts max)")
    class SkillScoreTests {

        @Test
        @DisplayName("40 pts si toutes les compétences sont dans la lettre")
        void allSkillsPresent_returns40() {
            Offer offer = buildOffer(Set.of("Java", "Spring"), null);
            Application app = buildApplication("J'ai de l'expérience en Java et Spring Boot.");
            int score = scoringService.calculateScore(app, offer, null);
            // 40 (skills) + 15 (exp neutre) + score lettre
            assertThat(score).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("0 pt de compétences si aucune correspondance")
        void noSkillsMatch_returns0SkillScore() {
            Offer offer = buildOffer(Set.of("Java", "Spring"), "SENIOR");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, 0);
            // 0 (skills) + 0 (SENIOR 0 an) + 0 (lettre null) = 0
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("20 pts si 50% des compétences correspondent")
        void halfSkillsMatch_returns20() {
            Offer offer = buildOffer(Set.of("Java", "Python"), null);
            Application app = buildApplication("Je maîtrise Java.");
            // 1/2 = 50% de 40 = 20 pts skills
            int skillScore = 20;
            int score = scoringService.calculateScore(app, offer, null);
            assertThat(score).isGreaterThanOrEqualTo(skillScore);
        }

        @Test
        @DisplayName("20 pts (neutre) si aucune compétence requise")
        void noRequiredSkills_returnsNeutral() {
            Offer offer = buildOffer(Set.of(), null);
            Application app = buildApplication(null);
            // 20 (neutre) + 15 (exp neutre) + 0 (lettre) = 35
            int score = scoringService.calculateScore(app, offer, null);
            assertThat(score).isEqualTo(35);
        }

        @Test
        @DisplayName("Matching insensible à la casse")
        void skillMatching_caseInsensitive() {
            Offer offer = buildOffer(Set.of("JAVA", "SPRING"), "JUNIOR");
            Application app = buildApplication("java spring boot");
            int score = scoringService.calculateScore(app, offer, 1);
            // 40 (skills) + 30 (JUNIOR 1 an) + score lettre
            assertThat(score).isGreaterThanOrEqualTo(70);
        }
    }

    // ─── SCORE D'EXPÉRIENCE (30 pts max) ─────────────────────────────────────

    @Nested
    @DisplayName("Score d'expérience (30 pts max)")
    class ExperienceScoreTests {

        @ParameterizedTest(name = "JUNIOR + {0} ans → expScore={1}")
        @CsvSource({"0,30", "1,30", "2,30", "3,15", "4,15", "5,0", "10,0"})
        @DisplayName("Niveau JUNIOR")
        void juniorExperienceScore(int years, int expectedExpScore) {
            Offer offer = buildOffer(Set.of(), "JUNIOR");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, years);
            // 20 (neutre skills) + expectedExpScore + 0 (lettre)
            assertThat(score).isEqualTo(20 + expectedExpScore);
        }

        @ParameterizedTest(name = "MID + {0} ans → expScore={1}")
        @CsvSource({"2,30", "3,30", "5,30", "1,10", "6,20"})
        @DisplayName("Niveau MID")
        void midExperienceScore(int years, int expectedExpScore) {
            Offer offer = buildOffer(Set.of(), "MID");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, years);
            assertThat(score).isEqualTo(20 + expectedExpScore);
        }

        @ParameterizedTest(name = "SENIOR + {0} ans → expScore={1}")
        @CsvSource({"5,30", "8,30", "3,15", "4,15", "1,0", "2,0"})
        @DisplayName("Niveau SENIOR")
        void seniorExperienceScore(int years, int expectedExpScore) {
            Offer offer = buildOffer(Set.of(), "SENIOR");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, years);
            assertThat(score).isEqualTo(20 + expectedExpScore);
        }

        @ParameterizedTest(name = "EXPERT + {0} ans → expScore={1}")
        @CsvSource({"8,30", "10,30", "5,15", "7,15", "3,0", "1,0"})
        @DisplayName("Niveau EXPERT")
        void expertExperienceScore(int years, int expectedExpScore) {
            Offer offer = buildOffer(Set.of(), "EXPERT");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, years);
            assertThat(score).isEqualTo(20 + expectedExpScore);
        }

        @Test
        @DisplayName("15 pts (neutre) si niveau requis null")
        void nullLevel_returnsNeutral() {
            Offer offer = buildOffer(Set.of(), null);
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, 5);
            assertThat(score).isEqualTo(35); // 20 + 15 + 0
        }

        @Test
        @DisplayName("0 pt si yearsOfExperience null")
        void nullYears_returns0() {
            Offer offer = buildOffer(Set.of(), "MID");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, null);
            assertThat(score).isEqualTo(20); // 20 + 0 + 0
        }
    }

    // ─── SCORE DE LETTRE (30 pts max) ────────────────────────────────────────

    @Nested
    @DisplayName("Score de lettre de motivation (30 pts max)")
    class CoverLetterScoreTests {

        @Test
        @DisplayName("0 pt si lettre null")
        void nullLetter_returns0() {
            Offer offer = buildOffer(Set.of(), "SENIOR");
            Application app = buildApplication(null);
            assertThat(scoringService.calculateScore(app, offer, 0)).isEqualTo(20);
        }

        @Test
        @DisplayName("0 pt si lettre vide")
        void blankLetter_returns0() {
            Offer offer = buildOffer(Set.of(), "SENIOR");
            Application app = buildApplication("   ");
            assertThat(scoringService.calculateScore(app, offer, 0)).isEqualTo(20);
        }

        @Test
        @DisplayName("Score positif pour lettre courte")
        void shortLetter_returnsPositive() {
            Offer offer = buildOffer(Set.of(), "SENIOR");
            Application app = buildApplication("Je suis motivé pour ce poste.");
            assertThat(scoringService.calculateScore(app, offer, 0)).isGreaterThan(20);
        }

        @Test
        @DisplayName("Score plus élevé avec mots-clés professionnels")
        void letterWithKeywords_returnsHigherScore() {
            String richLetter = "J'ai une grande expérience en développement de projets en équipe. "
                    + "Mes compétences et ma motivation sont au service des objectifs. "
                    + "J'ai contribué à des résultats significatifs grâce à mon expertise.";
            Offer offer = buildOffer(Set.of(), null);
            Application app = buildApplication(richLetter);
            int score = scoringService.calculateScore(app, offer, null);
            assertThat(score).isGreaterThan(30);
        }

        @Test
        @DisplayName("Score ne dépasse jamais 100")
        void scoreNeverExceeds100() {
            String maxLetter = "expérience compétence motivation projet équipe résultat objectif contribution "
                    + "experience skill team result " + "a".repeat(500);
            Offer offer = buildOffer(Set.of("Java", "Spring"), "JUNIOR");
            Application app = buildApplication(maxLetter);
            int score = scoringService.calculateScore(app, offer, 1);
            assertThat(score).isLessThanOrEqualTo(100);
        }
    }

    // ─── ANALYSE DE MATCHING ─────────────────────────────────────────────────

    @Nested
    @DisplayName("analyzeMatch() — Analyse complète")
    class AnalyzeMatchTests {

        @Test
        @DisplayName("Retourne toutes les clés attendues")
        void analyzeMatch_returnsAllKeys() {
            Offer offer = buildOffer(Set.of("Java", "Spring"), "MID");
            Application app = buildApplication("J'ai de l'expérience en Java.");
            Map<String, Object> result = scoringService.analyzeMatch(app, offer, 3);

            assertThat(result).containsKeys("totalScore", "skillScore", "experienceScore",
                    "coverLetterScore", "matchedSkills", "missingSkills", "suggestions", "matchLevel");
        }

        @Test
        @DisplayName("matchedSkills contient les compétences présentes dans la lettre")
        void analyzeMatch_detectsMatchedSkills() {
            Offer offer = buildOffer(Set.of("Java", "Python"), "MID");
            Application app = buildApplication("Je maîtrise Java depuis 3 ans.");
            Map<String, Object> result = scoringService.analyzeMatch(app, offer, 3);

            @SuppressWarnings("unchecked")
            List<String> matched = (List<String>) result.get("matchedSkills");
            @SuppressWarnings("unchecked")
            List<String> missing = (List<String>) result.get("missingSkills");

            assertThat(matched).contains("Java");
            assertThat(missing).contains("Python");
        }

        @Test
        @DisplayName("matchLevel EXCELLENT pour score >= 75")
        void analyzeMatch_excellentLevel() {
            Offer offer = buildOffer(Set.of("Java"), "JUNIOR");
            String letter = "Java expérience compétence motivation projet équipe résultat objectif "
                    + "contribution experience skill team result " + "a".repeat(400);
            Application app = buildApplication(letter);
            Map<String, Object> result = scoringService.analyzeMatch(app, offer, 1);
            assertThat(result.get("matchLevel")).isEqualTo("EXCELLENT");
        }

        @Test
        @DisplayName("matchLevel FAIBLE pour score < 25")
        void analyzeMatch_weakLevel() {
            Offer offer = buildOffer(Set.of("Java", "Spring", "Docker"), "SENIOR");
            Application app = buildApplication(null);
            Map<String, Object> result = scoringService.analyzeMatch(app, offer, 0);
            assertThat(result.get("matchLevel")).isEqualTo("FAIBLE");
        }

        @Test
        @DisplayName("Suggestions incluent les compétences manquantes")
        void analyzeMatch_suggestsMissingSkills() {
            Offer offer = buildOffer(Set.of("Docker", "Kubernetes"), "MID");
            Application app = buildApplication("Je suis développeur Java.");
            Map<String, Object> result = scoringService.analyzeMatch(app, offer, 3);

            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) result.get("suggestions");
            assertThat(suggestions).anyMatch(s -> s.contains("Docker") || s.contains("Kubernetes"));
        }

        @Test
        @DisplayName("Suggestion pour lettre trop courte")
        void analyzeMatch_suggestsLongerLetter() {
            Offer offer = buildOffer(Set.of(), "MID");
            Application app = buildApplication("Court.");
            Map<String, Object> result = scoringService.analyzeMatch(app, offer, 3);

            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) result.get("suggestions");
            assertThat(suggestions).anyMatch(s -> s.contains("lettre") || s.contains("200"));
        }

        @Test
        @DisplayName("Suggestion pour expérience insuffisante SENIOR")
        void analyzeMatch_suggestsMoreExperienceForSenior() {
            Offer offer = buildOffer(Set.of(), "SENIOR");
            Application app = buildApplication("Je suis développeur.");
            Map<String, Object> result = scoringService.analyzeMatch(app, offer, 2);

            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) result.get("suggestions");
            assertThat(suggestions).anyMatch(s -> s.contains("5+") || s.contains("expérience"));
        }

        @Test
        @DisplayName("Message positif si profil correspond bien")
        void analyzeMatch_positiveMessageWhenGoodMatch() {
            Offer offer = buildOffer(Set.of("Java"), "JUNIOR");
            String letter = "Java expérience compétence motivation " + "a".repeat(300);
            Application app = buildApplication(letter);
            Map<String, Object> result = scoringService.analyzeMatch(app, offer, 1);

            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) result.get("suggestions");
            assertThat(suggestions).anyMatch(s -> s.contains("correspond") || s.contains("bien"));
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Offer buildOffer(Set<String> skills, String experienceLevel) {
        Offer offer = new Offer();
        offer.setId(1L);
        offer.setTitle("Dev Java");
        offer.setDescription("Desc");
        offer.setCompanyName("Corp");
        offer.setLocation("Paris");
        offer.setContractType("CDI");
        offer.setCompanyId(1L);
        offer.setRequiredSkills(skills);
        offer.setExperienceLevel(experienceLevel);
        return offer;
    }

    private Application buildApplication(String coverLetter) {
        Application app = new Application();
        app.setId(1L);
        app.setOfferId(1L);
        app.setLearnerId(10L);
        app.setLearnerName("Alice");
        app.setLearnerEmail("alice@test.com");
        app.setCoverLetter(coverLetter);
        return app;
    }
}
