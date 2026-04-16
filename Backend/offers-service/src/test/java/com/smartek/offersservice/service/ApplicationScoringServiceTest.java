package com.smartek.offersservice.service;

import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour ApplicationScoringService.
 *
 * Couvre exhaustivement la logique de scoring :
 *  - Score de compétences (50 pts max)
 *  - Score d'expérience (30 pts max) pour chaque niveau
 *  - Score de lettre de motivation (20 pts max)
 *  - Cas limites : null, vide, dépassement de 100
 */
@DisplayName("ApplicationScoringService — Tests unitaires")
class ApplicationScoringServiceTest {

    private ApplicationScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ApplicationScoringService();
    }

    // ─── Score de compétences ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Score de compétences (50 pts max)")
    class SkillScoreTests {

        @Test
        @DisplayName("50 pts si toutes les compétences correspondent (+ 15 neutre expérience)")
        void shouldReturn50_whenAllSkillsMatch() {
            Offer offer = buildOffer(Set.of("Java", "Spring", "MySQL"), null);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of("Java", "Spring", "MySQL"), null);

            // 50 (compétences) + 15 (expérience neutre, level=null) + 0 (lettre) = 65
            assertThat(score).isEqualTo(65);
        }

        @Test
        @DisplayName("0 pt de compétences si aucune correspondance")
        void shouldReturn0SkillScore_whenNoMatch() {
            Offer offer = buildOffer(Set.of("Java", "Spring"), null);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of("PHP", "Laravel"), null);

            // 0 (compétences) + 15 (expérience neutre) + 0 (lettre) = 15
            assertThat(score).isEqualTo(15);
        }

        @Test
        @DisplayName("25 pts si 50% des compétences correspondent")
        void shouldReturn25_whenHalfSkillsMatch() {
            Offer offer = buildOffer(Set.of("Java", "Spring", "MySQL", "Docker"), null);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of("Java", "Spring"), null);

            // 2/4 = 50% de 50 = 25 pts + 15 (expérience neutre) = 40
            assertThat(score).isEqualTo(40);
        }

        @Test
        @DisplayName("25 pts (neutre) si l'offre n'a pas de compétences requises")
        void shouldReturn25Neutral_whenNoRequiredSkills() {
            Offer offer = buildOffer(Set.of(), null);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of("Java"), null);

            // 25 (compétences neutres) + 15 (expérience neutre) = 40
            assertThat(score).isEqualTo(40);
        }

        @Test
        @DisplayName("0 pt si le candidat n'a aucune compétence")
        void shouldReturn0_whenCandidateHasNoSkills() {
            Offer offer = buildOffer(Set.of("Java", "Spring"), Offer.ExperienceLevel.SENIOR);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of(), 0);

            // 0 (compétences) + 0 (SENIOR 0 an) + 0 (lettre) = 0
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("Comparaison insensible à la casse")
        void shouldMatchSkills_caseInsensitive() {
            Offer offer = buildOffer(Set.of("Java", "SPRING"), Offer.ExperienceLevel.JUNIOR);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of("java", "spring"), 1);

            // 50 (compétences) + 30 (JUNIOR 1 an) + 0 (lettre) = 80
            assertThat(score).isEqualTo(80);
        }
    }

    // ─── Score d'expérience ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Score d'expérience (30 pts max)")
    class ExperienceScoreTests {

        @ParameterizedTest(name = "JUNIOR + {0} ans → {1} pts")
        @CsvSource({
            "0, 30",   // 0 an = junior parfait
            "1, 30",   // 1 an = junior parfait
            "2, 30",   // 2 ans = junior parfait
            "3, 15",   // 3 ans = junior acceptable (30/2)
            "4, 15",   // 4 ans = junior acceptable
            "5, 0",    // 5 ans = trop expérimenté pour junior
            "10, 0"    // 10 ans = trop expérimenté
        })
        @DisplayName("Niveau JUNIOR")
        void juniorExperienceScore(int years, int expectedScore) {
            Offer offer = buildOffer(Set.of(), Offer.ExperienceLevel.JUNIOR);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of(), years);

            // Score total = 25 (neutre compétences) + expectedScore (expérience)
            assertThat(score).isEqualTo(25 + expectedScore);
        }

        @ParameterizedTest(name = "MID + {0} ans → {1} pts")
        @CsvSource({
            "2, 30",   // 2 ans = mid parfait
            "3, 30",   // 3 ans = mid parfait
            "5, 30",   // 5 ans = mid parfait
            "1, 10",   // 1 an = sous-qualifié (30/3)
            "6, 20",   // 6 ans = légèrement surqualifié (30*2/3)
        })
        @DisplayName("Niveau MID")
        void midExperienceScore(int years, int expectedScore) {
            Offer offer = buildOffer(Set.of(), Offer.ExperienceLevel.MID);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of(), years);

            assertThat(score).isEqualTo(25 + expectedScore);
        }

        @ParameterizedTest(name = "SENIOR + {0} ans → {1} pts")
        @CsvSource({
            "5, 30",   // 5 ans = senior parfait
            "8, 30",   // 8 ans = senior parfait
            "3, 15",   // 3 ans = acceptable (30/2)
            "4, 15",   // 4 ans = acceptable
            "1, 0",    // 1 an = insuffisant
            "2, 0"     // 2 ans = insuffisant
        })
        @DisplayName("Niveau SENIOR")
        void seniorExperienceScore(int years, int expectedScore) {
            Offer offer = buildOffer(Set.of(), Offer.ExperienceLevel.SENIOR);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of(), years);

            assertThat(score).isEqualTo(25 + expectedScore);
        }

        @ParameterizedTest(name = "EXPERT + {0} ans → {1} pts")
        @CsvSource({
            "8, 30",   // 8 ans = expert parfait
            "10, 30",  // 10 ans = expert parfait
            "5, 15",   // 5 ans = acceptable (30/2)
            "7, 15",   // 7 ans = acceptable
            "3, 0",    // 3 ans = insuffisant
            "1, 0"     // 1 an = insuffisant
        })
        @DisplayName("Niveau EXPERT")
        void expertExperienceScore(int years, int expectedScore) {
            Offer offer = buildOffer(Set.of(), Offer.ExperienceLevel.EXPERT);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of(), years);

            assertThat(score).isEqualTo(25 + expectedScore);
        }

        @Test
        @DisplayName("15 pts (neutre) si le niveau requis est null")
        void shouldReturn15Neutral_whenLevelIsNull() {
            Offer offer = buildOffer(Set.of(), null);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of(), 5);

            // 25 (compétences neutres) + 15 (expérience neutre = 30/2) + 0 (lettre) = 40
            assertThat(score).isEqualTo(40);
        }

        @Test
        @DisplayName("0 pt d'expérience si yearsOfExperience est null")
        void shouldReturn0_whenYearsIsNull() {
            Offer offer = buildOffer(Set.of(), Offer.ExperienceLevel.MID);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of(), null);

            // 25 (compétences neutres) + 0 (expérience null) + 0 (lettre) = 25
            assertThat(score).isEqualTo(25);
        }
    }

    // ─── Score de lettre de motivation ────────────────────────────────────────

    @Nested
    @DisplayName("Score de lettre de motivation (20 pts max)")
    class CoverLetterScoreTests {

        @Test
        @DisplayName("0 pt si la lettre est null (avec offre sans compétences requises)")
        void shouldReturn0_whenCoverLetterIsNull() {
            // Use non-empty required skills + no match → 0 skill pts; SENIOR 0 yr → 0 exp pts
            Offer offer = buildOffer(Set.of("Java"), Offer.ExperienceLevel.SENIOR);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of(), 0);

            // 0 (compétences) + 0 (SENIOR 0 an) + 0 (lettre null) = 0
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("0 pt si la lettre est vide")
        void shouldReturn0_whenCoverLetterIsBlank() {
            Offer offer = buildOffer(Set.of("Java"), Offer.ExperienceLevel.SENIOR);
            Application app = buildApplication("   ");

            int score = scoringService.calculateScore(app, offer, Set.of(), 0);

            // 0 (compétences) + 0 (SENIOR 0 an) + 0 (lettre vide) = 0
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("1 pt pour une lettre très courte (< 100 chars)")
        void shouldReturn1_whenVeryShortLetter() {
            Offer offer = buildOffer(Set.of("Java"), Offer.ExperienceLevel.SENIOR);
            Application app = buildApplication("Bonjour, je postule.");

            int score = scoringService.calculateScore(app, offer, Set.of(), 0);

            // 0 (compétences) + 0 (SENIOR 0 an) + 1 (lettre courte) = 1
            assertThat(score).isEqualTo(1);
        }

        @Test
        @DisplayName("Score plus élevé pour une lettre longue avec mots-clés")
        void shouldReturnHigherScore_whenLongLetterWithKeywords() {
            String richLetter = "J'ai une grande expérience dans le développement de projets en équipe. "
                    + "Mes compétences techniques et ma motivation sont au service des objectifs. "
                    + "J'ai contribué à des résultats significatifs grâce à mon expertise. "
                    + "Je suis prêt à apporter ma contribution à votre équipe.";

            Offer offer = buildOffer(Set.of(), null);
            Application app = buildApplication(richLetter);

            int score = scoringService.calculateScore(app, offer, Set.of(), null);

            assertThat(score).isGreaterThan(30); // 25 + score lettre > 5
        }

        @Test
        @DisplayName("Score ne dépasse jamais 100")
        void scoreShouldNeverExceed100() {
            String maxLetter = "expérience compétence motivation projet équipe résultat objectif contribution "
                    + "experience skill team result - très longue lettre professionnelle avec beaucoup de contenu "
                    + "démontrant une expertise avancée dans tous les domaines requis pour ce poste senior.";

            Offer offer = buildOffer(Set.of("Java", "Spring", "MySQL"), Offer.ExperienceLevel.JUNIOR);
            Application app = buildApplication(maxLetter);

            int score = scoringService.calculateScore(app, offer, Set.of("Java", "Spring", "MySQL"), 1);

            assertThat(score).isLessThanOrEqualTo(100);
        }
    }

    // ─── Tests de combinaison ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Score combiné")
    class CombinedScoreTests {

        @Test
        @DisplayName("Score maximum (100) pour un candidat parfait")
        void shouldReturnMaxScore_forPerfectCandidate() {
            // Need exactly 20 pts from cover letter: length >= 500 (10 pts) + 10 keywords (10 pts)
            String perfectLetter = "expérience compétence motivation projet équipe résultat objectif contribution "
                    + "experience skill team result "
                    + "a".repeat(450); // pad to >= 500 chars total

            Offer offer = buildOffer(Set.of("Java", "Spring"), Offer.ExperienceLevel.JUNIOR);
            Application app = buildApplication(perfectLetter);

            int score = scoringService.calculateScore(app, offer, Set.of("Java", "Spring"), 1);

            // 50 (compétences) + 30 (JUNIOR 1 an) + 20 (lettre ≥500 chars + 10 mots-clés) = 100
            assertThat(score).isEqualTo(100);
        }

        @Test
        @DisplayName("Score 0 pour un candidat sans aucun atout")
        void shouldReturnZero_forWorstCandidate() {
            Offer offer = buildOffer(Set.of("Java", "Spring"), Offer.ExperienceLevel.SENIOR);
            Application app = buildApplication(null);

            int score = scoringService.calculateScore(app, offer, Set.of(), 0);

            // 0 (compétences) + 0 (expérience SENIOR 0 an) + 0 (pas de lettre) = 0
            assertThat(score).isEqualTo(0);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Offer buildOffer(Set<String> skills, Offer.ExperienceLevel level) {
        return Offer.builder()
                .id(1L)
                .title("Développeur Java")
                .requiredSkills(skills)
                .experienceLevel(level)
                .build();
    }

    private Application buildApplication(String coverLetter) {
        return Application.builder()
                .id(1L)
                .coverLetter(coverLetter)
                .build();
    }
}
