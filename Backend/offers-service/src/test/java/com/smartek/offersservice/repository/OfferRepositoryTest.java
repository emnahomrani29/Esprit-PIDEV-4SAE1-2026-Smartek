package com.smartek.offersservice.repository;

import com.smartek.offersservice.entity.Offer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("OfferRepository — Tests de repository")
class OfferRepositoryTest {

    @Autowired private OfferRepository offerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Offer activeOffer1;
    private Offer activeOffer2;
    private Offer closedOffer;
    private Offer expiredOffer;

    @BeforeEach
    void setUp() {
        offerRepository.deleteAll();

        activeOffer1 = offerRepository.save(Offer.builder()
                .title("Développeur Java Senior").description("Poste Java senior")
                .companyName("TechCorp").location("Paris").contractType("CDI")
                .companyId(1L).status("ACTIVE").viewCount(100L).positions(2)
                .remote(false).domain("IT").experienceLevel("SENIOR")
                .salaryMin(50000).salaryMax(70000)
                .requiredSkills(Set.of("Java", "Spring", "Docker"))
                .build());

        activeOffer2 = offerRepository.save(Offer.builder()
                .title("Data Scientist Python").description("Poste data science")
                .companyName("DataCorp").location("Lyon").contractType("CDI")
                .companyId(2L).status("ACTIVE").viewCount(50L).positions(1)
                .remote(true).domain("DATA").experienceLevel("MID")
                .salaryMin(45000).salaryMax(60000)
                .requiredSkills(Set.of("Python", "ML"))
                .build());

        closedOffer = offerRepository.save(Offer.builder()
                .title("Dev React Fermé").description("Poste fermé")
                .companyName("WebCorp").location("Paris").contractType("CDD")
                .companyId(1L).status("CLOSED").viewCount(20L).positions(1)
                .remote(false)
                .build());

        expiredOffer = offerRepository.save(Offer.builder()
                .title("Dev Angular Expiré").description("Poste expiré")
                .companyName("TechCorp").location("Marseille").contractType("CDI")
                .companyId(1L).status("ACTIVE").viewCount(5L).positions(1)
                .remote(false)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build());
    }

    // ─── findByCompanyId ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByCompanyId()")
    class FindByCompanyIdTests {

        @Test
        @DisplayName("Retourne toutes les offres d'une entreprise")
        void shouldReturnAllOffersForCompany() {
            List<Offer> result = offerRepository.findByCompanyId(1L);
            assertThat(result).hasSize(3); // activeOffer1 + closedOffer + expiredOffer
        }

        @Test
        @DisplayName("Retourne liste vide pour entreprise inconnue")
        void shouldReturnEmpty_whenCompanyNotFound() {
            List<Offer> result = offerRepository.findByCompanyId(999L);
            assertThat(result).isEmpty();
        }
    }

    // ─── findByStatus ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByStatus()")
    class FindByStatusTests {

        @Test
        @DisplayName("Retourne les offres ACTIVE (y compris celles avec expiresAt passé)")
        void shouldReturnActiveOffers() {
            // expiredOffer a status=ACTIVE même si expiresAt est passé
            // (le scheduler change le status, pas la requête findByStatus)
            List<Offer> result = offerRepository.findByStatus("ACTIVE");
            assertThat(result).hasSize(3); // activeOffer1 + activeOffer2 + expiredOffer
        }

        @Test
        @DisplayName("Retourne les offres CLOSED")
        void shouldReturnClosedOffers() {
            List<Offer> result = offerRepository.findByStatus("CLOSED");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Dev React Fermé");
        }

        @Test
        @DisplayName("Pagination fonctionne correctement")
        void shouldSupportPagination() {
            Page<Offer> page = offerRepository.findByStatus("ACTIVE", PageRequest.of(0, 1));
            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getContent()).hasSize(1);
        }
    }

    // ─── countByCompanyIdAndStatus ────────────────────────────────────────────

    @Nested
    @DisplayName("countByCompanyIdAndStatus()")
    class CountByCompanyIdAndStatusTests {

        @Test
        @DisplayName("Compte correctement les offres ACTIVE d'une entreprise")
        void shouldCountActiveOffersForCompany() {
            long count = offerRepository.countByCompanyIdAndStatus(1L, "ACTIVE");
            assertThat(count).isEqualTo(2); // activeOffer1 + expiredOffer (status=ACTIVE)
        }

        @Test
        @DisplayName("Compte correctement les offres CLOSED d'une entreprise")
        void shouldCountClosedOffersForCompany() {
            long count = offerRepository.countByCompanyIdAndStatus(1L, "CLOSED");
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Retourne 0 pour entreprise sans offres")
        void shouldReturn0_whenNoOffersForCompany() {
            long count = offerRepository.countByCompanyIdAndStatus(999L, "ACTIVE");
            assertThat(count).isEqualTo(0);
        }
    }

    // ─── findExpiredOffers ────────────────────────────────────────────────────

    @Nested
    @DisplayName("findExpiredOffers()")
    class FindExpiredOffersTests {

        @Test
        @DisplayName("Retourne les offres ACTIVE dont expiresAt est passé")
        void shouldReturnExpiredActiveOffers() {
            List<Offer> expired = offerRepository.findExpiredOffers(LocalDateTime.now());
            assertThat(expired).hasSize(1);
            assertThat(expired.get(0).getTitle()).isEqualTo("Dev Angular Expiré");
        }

        @Test
        @DisplayName("Ne retourne pas les offres CLOSED même si expiresAt est passé")
        void shouldNotReturnClosedOffers() {
            // Ajouter une offre CLOSED avec expiresAt passé
            offerRepository.save(Offer.builder()
                    .title("Offre CLOSED expirée").description("Desc")
                    .companyName("Corp").location("Paris").contractType("CDI")
                    .companyId(3L).status("CLOSED").viewCount(0L).positions(1).remote(false)
                    .expiresAt(LocalDateTime.now().minusDays(2))
                    .build());

            List<Offer> expired = offerRepository.findExpiredOffers(LocalDateTime.now());
            assertThat(expired).noneMatch(o -> "CLOSED".equals(o.getStatus()));
        }

        @Test
        @DisplayName("Ne retourne pas les offres dont expiresAt est dans le futur")
        void shouldNotReturnFutureExpiringOffers() {
            offerRepository.save(Offer.builder()
                    .title("Offre future").description("Desc")
                    .companyName("Corp").location("Paris").contractType("CDI")
                    .companyId(3L).status("ACTIVE").viewCount(0L).positions(1).remote(false)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build());

            List<Offer> expired = offerRepository.findExpiredOffers(LocalDateTime.now());
            assertThat(expired).noneMatch(o -> "Offre future".equals(o.getTitle()));
        }
    }

    // ─── incrementViewCount ───────────────────────────────────────────────────

    @Nested
    @DisplayName("incrementViewCount()")
    class IncrementViewCountTests {

        @Test
        @DisplayName("Incrémente le compteur de vues de 1")
        void shouldIncrementViewCount() {
            long initialCount = activeOffer1.getViewCount();
            offerRepository.incrementViewCount(activeOffer1.getId());
            // Flush + clear nécessaires pour que @Modifying @Query soit visible
            entityManager.flush();
            entityManager.clear();

            Offer updated = offerRepository.findById(activeOffer1.getId()).orElseThrow();
            assertThat(updated.getViewCount()).isEqualTo(initialCount + 1);
        }
    }

    // ─── findTopViewedOffers ──────────────────────────────────────────────────

    @Nested
    @DisplayName("findTopViewedOffers()")
    class FindTopViewedOffersTests {

        @Test
        @DisplayName("Retourne les offres ACTIVE triées par viewCount décroissant")
        void shouldReturnTopViewedActiveOffers() {
            Pageable pageable = PageRequest.of(0, 3);
            List<Offer> top = offerRepository.findTopViewedOffers(pageable);

            assertThat(top).isNotEmpty();
            assertThat(top.get(0).getViewCount()).isGreaterThanOrEqualTo(top.get(top.size() - 1).getViewCount());
            assertThat(top).allMatch(o -> "ACTIVE".equals(o.getStatus()));
        }

        @Test
        @DisplayName("Limite le nombre de résultats")
        void shouldLimitResults() {
            Pageable pageable = PageRequest.of(0, 1);
            List<Offer> top = offerRepository.findTopViewedOffers(pageable);
            assertThat(top).hasSize(1);
        }
    }

    // ─── searchWithFilters ────────────────────────────────────────────────────

    @Nested
    @DisplayName("searchWithFilters()")
    class SearchWithFiltersTests {

        @Test
        @DisplayName("Recherche par keyword dans le titre")
        void shouldSearchByKeywordInTitle() {
            Page<Offer> result = offerRepository.searchWithFilters(
                    "Java", null, null, null, null, null, null, null, null,
                    PageRequest.of(0, 10));
            assertThat(result.getContent()).anyMatch(o -> o.getTitle().contains("Java"));
        }

        @Test
        @DisplayName("Filtre par contractType")
        void shouldFilterByContractType() {
            Page<Offer> result = offerRepository.searchWithFilters(
                    null, null, "CDI", null, null, null, null, null, null,
                    PageRequest.of(0, 10));
            assertThat(result.getContent()).allMatch(o -> "CDI".equals(o.getContractType()));
        }

        @Test
        @DisplayName("Filtre par remote=true")
        void shouldFilterByRemote() {
            Page<Offer> result = offerRepository.searchWithFilters(
                    null, null, null, null, null, null, true, null, null,
                    PageRequest.of(0, 10));
            assertThat(result.getContent()).allMatch(o -> Boolean.TRUE.equals(o.getRemote()));
        }

        @Test
        @DisplayName("Filtre par salaire minimum")
        void shouldFilterBySalaryMin() {
            Page<Offer> result = offerRepository.searchWithFilters(
                    null, null, null, null, null, null, null, 48000, null,
                    PageRequest.of(0, 10));
            assertThat(result.getContent()).allMatch(o -> o.getSalaryMin() == null || o.getSalaryMin() >= 48000);
        }

        @Test
        @DisplayName("Filtre par location (insensible à la casse)")
        void shouldFilterByLocationCaseInsensitive() {
            Page<Offer> result = offerRepository.searchWithFilters(
                    null, null, null, "paris", null, null, null, null, null,
                    PageRequest.of(0, 10));
            assertThat(result.getContent()).allMatch(o ->
                    o.getLocation().toLowerCase().contains("paris"));
        }

        @Test
        @DisplayName("Retourne liste vide si aucun résultat")
        void shouldReturnEmpty_whenNoMatch() {
            Page<Offer> result = offerRepository.searchWithFilters(
                    "XYZ_INEXISTANT_KEYWORD", null, null, null, null, null, null, null, null,
                    PageRequest.of(0, 10));
            assertThat(result.getContent()).isEmpty();
        }
    }
}
