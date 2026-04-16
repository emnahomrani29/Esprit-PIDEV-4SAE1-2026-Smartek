package com.smartek.offersservice.service;

import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour ApplicationScoringService.
 */
@DisplayName("ApplicationScoringService — Tests unitaires")
class ApplicationScoringServiceTest {

    private ApplicationScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ApplicationScoringService();
    }

    // ─── Score d'expérience ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Score d'expérience (50 pts max)")
    class ExperienceScoreTests {

        @ParameterizedTest(name = "JUNIOR + {0} ans → expScore={1}")
        @CsvSource({
            "0, 50",
            "1, 50",
            "2, 50",
            "3, 25",
            "4, 25",
            "5, 0",
            "10, 0"
        })
        @DisplayName("Niveau JUNIOR")
        void juniorExperienceScore(int years, int expectedExpScore) {
            Offer offer = buildOffer("JUNIOR");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, years);
            // score = expScore + coverLetterScore(null=0)
            assertThat(score).isEqualTo(expectedExpScore);
        }

        @ParameterizedTest(name = "MID + {0} ans → expScore={1}")
        @CsvSource({
            "2, 50",
            "3, 50",
            "5, 50",
            "1, 16",
            "6, 33"
        })
        @DisplayName("Niveau MID")
        void midExperienceScore(int years, int expectedExpScore) {
            Offer offer = buildOffer("MID");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, years);
            assertThat(score).isEqualTo(expectedExpScore);
        }

        @ParameterizedTest(name = "SENIOR + {0} ans → expScore={1}")
        @CsvSource({
            "5, 50",
            "8, 50",
            "3, 25",
            "4, 25",
            "1, 0",
            "2, 0"
        })
        @DisplayName("Niveau SENIOR")
        void seniorExperienceScore(int years, int expectedExpScore) {
            Offer offer = buildOffer("SENIOR");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, years);
            assertThat(score).isEqualTo(expectedExpScore);
        }

        @Test
        @DisplayName("25 pts (neutre) si le niveau requis est null")
        void shouldReturn25Neutral_whenLevelIsNull() {
            Offer offer = buildOffer(null);
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, 5);
            assertThat(score).isEqualTo(25); // 25 (neutre) + 0 (lettre null)
        }

        @Test
        @DisplayName("0 pt d'expérience si yearsOfExperience est null")
        void shouldReturn0_whenYearsIsNull() {
            Offer offer = buildOffer("MID");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, null);
            assertThat(score).isEqualTo(0); // 0 (exp null) + 0 (lettre null)
        }
    }

    // ─── Score de lettre de motivation ────────────────────────────────────────

    @Nested
    @DisplayName("Score de lettre de motivation (50 pts max)")
    class CoverLetterScoreTests {

        @Test
        @DisplayName("0 pt si la lettre est null")
        void shouldReturn0_whenCoverLetterIsNull() {
            Offer offer = buildOffer("SENIOR");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, 0);
            assertThat(score).isEqualTo(0); // 0 (SENIOR 0 an) + 0 (lettre null)
        }

        @Test
        @DisplayName("0 pt si la lettre est vide")
        void shouldReturn0_whenCoverLetterIsBlank() {
            Offer offer = buildOffer("SENIOR");
            Application app = buildApplication("   ");
            int score = scoringService.calculateScore(app, offer, 0);
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("Score positif pour une lettre courte")
        void shouldReturnPositive_whenShortLetter() {
            Offer offer = buildOffer("SENIOR");
            Application app = buildApplication("Bonjour, je postule.");
            int score = scoringService.calculateScore(app, offer, 0);
            assertThat(score).isGreaterThan(0);
        }

        @Test
        @DisplayName("Score plus élevé pour une lettre longue avec mots-clés")
        void shouldReturnHigherScore_whenLongLetterWithKeywords() {
            String richLetter = "J'ai une grande expérience dans le développement de projets en équipe. "
                    + "Mes compétences techniques et ma motivation sont au service des objectifs. "
                    + "J'ai contribué à des résultats significatifs grâce à mon expertise. "
                    + "Je suis prêt à apporter ma contribution à votre équipe.";
            Offer offer = buildOffer(null);
            Application app = buildApplication(richLetter);
            int score = scoringService.calculateScore(app, offer, null);
            assertThat(score).isGreaterThan(20);
        }

        @Test
        @DisplayName("Score ne dépasse jamais 100")
        void scoreShouldNeverExceed100() {
            String maxLetter = "expérience compétence motivation projet équipe résultat objectif contribution "
                    + "experience skill team result - très longue lettre professionnelle avec beaucoup de contenu "
                    + "démontrant une expertise avancée dans tous les domaines requis pour ce poste senior. "
                    + "a".repeat(400);
            Offer offer = buildOffer("JUNIOR");
            Application app = buildApplication(maxLetter);
            int score = scoringService.calculateScore(app, offer, 1);
            assertThat(score).isLessThanOrEqualTo(100);
        }
    }

    // ─── Tests combinés ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Score combiné")
    class CombinedScoreTests {

        @Test
        @DisplayName("Score 0 pour un candidat sans atout")
        void shouldReturnZero_forWorstCandidate() {
            Offer offer = buildOffer("SENIOR");
            Application app = buildApplication(null);
            int score = scoringService.calculateScore(app, offer, 0);
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("Score positif pour un bon candidat")
        void shouldReturnPositive_forGoodCandidate() {
            String letter = "expérience compétence motivation projet équipe résultat objectif contribution "
                    + "experience skill team result " + "a".repeat(450);
            Offer offer = buildOffer("JUNIOR");
            Application app = buildApplication(letter);
            int score = scoringService.calculateScore(app, offer, 1);
            assertThat(score).isGreaterThan(50);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Offer buildOffer(String experienceLevel) {
        Offer offer = new Offer();
        offer.setId(1L);
        offer.setTitle("Développeur Java");
        offer.setDescription("Description");
        offer.setCompanyName("TechCorp");
        offer.setLocation("Paris");
        offer.setContractType("CDI");
        offer.setCompanyId(1L);
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
