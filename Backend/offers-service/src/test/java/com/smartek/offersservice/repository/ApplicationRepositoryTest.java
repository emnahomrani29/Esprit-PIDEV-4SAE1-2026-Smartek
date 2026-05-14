package com.smartek.offersservice.repository;

import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("ApplicationRepository — Tests de repository")
class ApplicationRepositoryTest {

    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private OfferRepository offerRepository;

    private Offer offer1;
    private Offer offer2;
    private Application app1;
    private Application app2;
    private Application app3;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        offerRepository.deleteAll();

        offer1 = offerRepository.save(Offer.builder()
                .title("Dev Java").description("Desc").companyName("TechCorp")
                .location("Paris").contractType("CDI").companyId(1L)
                .status("ACTIVE").viewCount(0L).positions(1).remote(false).build());

        offer2 = offerRepository.save(Offer.builder()
                .title("Dev Python").description("Desc").companyName("DataCorp")
                .location("Lyon").contractType("CDI").companyId(2L)
                .status("ACTIVE").viewCount(0L).positions(1).remote(false).build());

        app1 = new Application();
        app1.setOffer(offer1);
        app1.setLearnerId(10L);
        app1.setLearnerName("Alice");
        app1.setLearnerEmail("alice@test.com");
        app1.setStatus("PENDING");
        app1.setScore(80);
        app1 = applicationRepository.save(app1);

        app2 = new Application();
        app2.setOffer(offer1);
        app2.setLearnerId(11L);
        app2.setLearnerName("Bob");
        app2.setLearnerEmail("bob@test.com");
        app2.setStatus("ACCEPTED");
        app2.setScore(90);
        app2 = applicationRepository.save(app2);

        app3 = new Application();
        app3.setOffer(offer2);
        app3.setLearnerId(10L);
        app3.setLearnerName("Alice");
        app3.setLearnerEmail("alice@test.com");
        app3.setStatus("REJECTED");
        app3.setScore(40);
        app3 = applicationRepository.save(app3);
    }

    // ─── findByOfferId ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByOfferId()")
    class FindByOfferIdTests {

        @Test
        @DisplayName("Retourne les candidatures d'une offre")
        void shouldReturnApplicationsForOffer() {
            List<Application> result = applicationRepository.findByOfferId(offer1.getId());
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Retourne liste vide pour offre sans candidatures")
        void shouldReturnEmpty_whenNoApplications() {
            Offer emptyOffer = offerRepository.save(Offer.builder()
                    .title("Vide").description("Desc").companyName("Corp")
                    .location("Paris").contractType("CDI").companyId(3L)
                    .status("ACTIVE").viewCount(0L).positions(1).remote(false).build());
            assertThat(applicationRepository.findByOfferId(emptyOffer.getId())).isEmpty();
        }
    }

    // ─── findByOfferIdOrderByScoreDesc ────────────────────────────────────────

    @Test
    @DisplayName("findByOfferIdOrderByScoreDesc() → triées par score décroissant")
    void shouldReturnApplicationsSortedByScoreDesc() {
        List<Application> result = applicationRepository.findByOfferIdOrderByScoreDesc(offer1.getId());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getScore()).isGreaterThanOrEqualTo(result.get(1).getScore());
    }

    // ─── findByLearnerId ──────────────────────────────────────────────────────

    @Test
    @DisplayName("findByLearnerId() → retourne toutes les candidatures du candidat")
    void shouldReturnApplicationsForLearner() {
        List<Application> result = applicationRepository.findByLearnerId(10L);
        assertThat(result).hasSize(2); // app1 + app3
    }

    // ─── findByOfferIdAndLearnerId ────────────────────────────────────────────

    @Nested
    @DisplayName("findByOfferIdAndLearnerId()")
    class FindByOfferAndLearnerTests {

        @Test
        @DisplayName("Retourne la candidature si elle existe")
        void shouldReturnApplication_whenExists() {
            Optional<Application> result = applicationRepository.findByOfferIdAndLearnerId(offer1.getId(), 10L);
            assertThat(result).isPresent();
            assertThat(result.get().getLearnerName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Retourne vide si la candidature n'existe pas")
        void shouldReturnEmpty_whenNotExists() {
            Optional<Application> result = applicationRepository.findByOfferIdAndLearnerId(offer1.getId(), 999L);
            assertThat(result).isEmpty();
        }
    }

    // ─── existsByOfferIdAndLearnerId ──────────────────────────────────────────

    @Nested
    @DisplayName("existsByOfferIdAndLearnerId()")
    class ExistsByOfferAndLearnerTests {

        @Test
        @DisplayName("Retourne true si la candidature existe")
        void shouldReturnTrue_whenExists() {
            assertThat(applicationRepository.existsByOfferIdAndLearnerId(offer1.getId(), 10L)).isTrue();
        }

        @Test
        @DisplayName("Retourne false si la candidature n'existe pas")
        void shouldReturnFalse_whenNotExists() {
            assertThat(applicationRepository.existsByOfferIdAndLearnerId(offer1.getId(), 999L)).isFalse();
        }
    }

    // ─── countByOfferId ───────────────────────────────────────────────────────

    @Test
    @DisplayName("countByOfferId() → compte correctement")
    void shouldCountApplicationsForOffer() {
        assertThat(applicationRepository.countByOfferId(offer1.getId())).isEqualTo(2);
        assertThat(applicationRepository.countByOfferId(offer2.getId())).isEqualTo(1);
    }

    // ─── countByOfferIdAndStatus ──────────────────────────────────────────────

    @Test
    @DisplayName("countByOfferIdAndStatus() → compte par statut")
    void shouldCountByStatus() {
        assertThat(applicationRepository.countByOfferIdAndStatus(offer1.getId(), "ACCEPTED")).isEqualTo(1);
        assertThat(applicationRepository.countByOfferIdAndStatus(offer1.getId(), "PENDING")).isEqualTo(1);
        assertThat(applicationRepository.countByOfferIdAndStatus(offer1.getId(), "REJECTED")).isEqualTo(0);
    }

    // ─── countByCompanyId ─────────────────────────────────────────────────────

    @Test
    @DisplayName("countByCompanyId() → compte toutes les candidatures d'une entreprise")
    void shouldCountApplicationsByCompany() {
        assertThat(applicationRepository.countByCompanyId(1L)).isEqualTo(2); // app1 + app2
        assertThat(applicationRepository.countByCompanyId(2L)).isEqualTo(1); // app3
    }

    // ─── countByCompanyIdAndStatus ────────────────────────────────────────────

    @Test
    @DisplayName("countByCompanyIdAndStatus() → compte par statut pour une entreprise")
    void shouldCountByCompanyAndStatus() {
        assertThat(applicationRepository.countByCompanyIdAndStatus(1L, "ACCEPTED")).isEqualTo(1);
        assertThat(applicationRepository.countByCompanyIdAndStatus(1L, "PENDING")).isEqualTo(1);
        assertThat(applicationRepository.countByCompanyIdAndStatus(2L, "REJECTED")).isEqualTo(1);
    }

    // ─── averageScoreByCompanyId ──────────────────────────────────────────────

    @Test
    @DisplayName("averageScoreByCompanyId() → calcule la moyenne des scores")
    void shouldCalculateAverageScore() {
        // offer1 (companyId=1): scores 80 + 90 → moyenne = 85
        double avg = applicationRepository.averageScoreByCompanyId(1L);
        assertThat(avg).isEqualTo(85.0);
    }

    @Test
    @DisplayName("averageScoreByCompanyId() → retourne 0 si aucune candidature")
    void shouldReturn0_whenNoApplications() {
        double avg = applicationRepository.averageScoreByCompanyId(999L);
        assertThat(avg).isEqualTo(0.0);
    }
}
