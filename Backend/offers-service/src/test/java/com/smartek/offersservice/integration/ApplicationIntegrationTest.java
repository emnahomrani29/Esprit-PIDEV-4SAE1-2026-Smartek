package com.smartek.offersservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Application — Tests d'intégration")
class ApplicationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OfferRepository offerRepository;
    @Autowired private ApplicationRepository applicationRepository;

    @BeforeEach
    void cleanDatabase() {
        applicationRepository.deleteAll();
        offerRepository.deleteAll();
    }

    private Offer saveActiveOffer(String title) {
        return offerRepository.save(Offer.builder()
                .title(title).description("Description du poste")
                .companyName("TechCorp").location("Paris").contractType("CDI")
                .companyId(1L).status("ACTIVE").viewCount(0L).positions(2)
                .remote(false).requiredSkills(Set.of("Java", "Spring"))
                .experienceLevel("MID").build());
    }

    // ─── Postuler à une offre ─────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/applications — Postuler")
    class ApplyTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("201 avec score calculé automatiquement")
        void apply_shouldReturn201_withAutoScore() throws Exception {
            Offer offer = saveActiveOffer("Dev Java MID");

            String body = """
                {
                    "offerId": %d,
                    "learnerId": 10,
                    "learnerName": "Alice Dupont",
                    "learnerEmail": "alice@test.com",
                    "coverLetter": "J'ai une grande expérience en Java et Spring Boot. Mon équipe et moi avons livré des projets avec d'excellents résultats.",
                    "yearsOfExperience": 3
                }
                """.formatted(offer.getId());

            MvcResult result = mockMvc.perform(post("/api/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.score").value(greaterThan(0)))
                    .andExpect(jsonPath("$.learnerId").value(10))
                    .andReturn();

            // Vérifier en BDD
            Long appId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
            Application saved = applicationRepository.findById(appId).orElseThrow();
            assertThat(saved.getScore()).isGreaterThan(0);
            assertThat(saved.getStatus()).isEqualTo("PENDING");
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("422 si double candidature")
        void apply_shouldReturn422_whenDuplicate() throws Exception {
            Offer offer = saveActiveOffer("Dev Java");
            String body = """
                {
                    "offerId": %d,
                    "learnerId": 10,
                    "learnerName": "Alice",
                    "learnerEmail": "alice@test.com",
                    "coverLetter": "Ma lettre."
                }
                """.formatted(offer.getId());

            mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value(containsString("déjà postulé")));
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("422 si offre CLOSED")
        void apply_shouldReturn422_whenOfferClosed() throws Exception {
            Offer offer = offerRepository.save(Offer.builder()
                    .title("Offre fermée").description("Desc").companyName("Corp")
                    .location("Paris").contractType("CDI").companyId(1L)
                    .status("CLOSED").viewCount(0L).positions(1).remote(false).build());

            String body = """
                {
                    "offerId": %d,
                    "learnerId": 10,
                    "learnerName": "Alice",
                    "learnerEmail": "alice@test.com"
                }
                """.formatted(offer.getId());

            mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("404 si offre introuvable")
        void apply_shouldReturn404_whenOfferNotFound() throws Exception {
            String body = """
                {
                    "offerId": 99999,
                    "learnerId": 10,
                    "learnerName": "Alice",
                    "learnerEmail": "alice@test.com"
                }
                """;

            mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("400 si learnerId manquant")
        void apply_shouldReturn400_whenLearnerIdMissing() throws Exception {
            Offer offer = saveActiveOffer("Dev Java");
            String body = """
                {
                    "offerId": %d,
                    "learnerName": "Alice",
                    "learnerEmail": "alice@test.com"
                }
                """.formatted(offer.getId());

            mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── Mise à jour du statut ────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/applications/{id}/status — Mise à jour statut")
    class UpdateStatusTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("200 avec statut ACCEPTED et note recruteur")
        void updateStatus_shouldReturn200_withAccepted() throws Exception {
            Offer offer = saveActiveOffer("Dev Java");
            Application app = new Application();
            app.setOffer(offer);
            app.setLearnerId(10L);
            app.setLearnerName("Alice");
            app.setLearnerEmail("alice@test.com");
            app.setStatus("PENDING");
            app.setScore(75);
            Application saved = applicationRepository.save(app);

            mockMvc.perform(put("/api/applications/" + saved.getId() + "/status")
                            .param("status", "ACCEPTED")
                            .param("recruiterNote", "Excellent profil, très motivé"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.recruiterNote").value("Excellent profil, très motivé"));

            Application updated = applicationRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo("ACCEPTED");
            assertThat(updated.getRecruiterNote()).isEqualTo("Excellent profil, très motivé");
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("404 si candidature introuvable")
        void updateStatus_shouldReturn404_whenNotFound() throws Exception {
            mockMvc.perform(put("/api/applications/99999/status").param("status", "ACCEPTED"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── Retrait de candidature ───────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/applications/{id}/withdraw — Retrait")
    class WithdrawTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 avec statut WITHDRAWN")
        void withdraw_shouldReturn200_withWithdrawn() throws Exception {
            Offer offer = saveActiveOffer("Dev Java");
            Application app = new Application();
            app.setOffer(offer);
            app.setLearnerId(10L);
            app.setLearnerName("Alice");
            app.setLearnerEmail("alice@test.com");
            app.setStatus("PENDING");
            app.setScore(50);
            Application saved = applicationRepository.save(app);

            mockMvc.perform(put("/api/applications/" + saved.getId() + "/withdraw")
                            .param("learnerId", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WITHDRAWN"));
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("422 si retrait par un autre candidat")
        void withdraw_shouldReturn422_whenWrongLearner() throws Exception {
            Offer offer = saveActiveOffer("Dev Java");
            Application app = new Application();
            app.setOffer(offer);
            app.setLearnerId(10L);
            app.setLearnerName("Alice");
            app.setLearnerEmail("alice@test.com");
            app.setStatus("PENDING");
            app.setScore(50);
            Application saved = applicationRepository.save(app);

            mockMvc.perform(put("/api/applications/" + saved.getId() + "/withdraw")
                            .param("learnerId", "999"))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // ─── Vérification de candidature ─────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /api/applications/check/{offerId}/{learnerId} → true si candidature existe")
    void hasApplied_shouldReturnTrue_whenApplicationExists() throws Exception {
        Offer offer = saveActiveOffer("Dev Java");
        Application app = new Application();
        app.setOffer(offer);
        app.setLearnerId(10L);
        app.setLearnerName("Alice");
        app.setLearnerEmail("alice@test.com");
        app.setStatus("PENDING");
        app.setScore(50);
        applicationRepository.save(app);

        mockMvc.perform(get("/api/applications/check/" + offer.getId() + "/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasApplied").value(true));
    }

    // ─── Analyse de matching ──────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /api/applications/{id}/match-analysis → 200 avec analyse complète")
    void matchAnalysis_shouldReturn200_withCompleteAnalysis() throws Exception {
        Offer offer = saveActiveOffer("Dev Java MID");
        Application app = new Application();
        app.setOffer(offer);
        app.setLearnerId(10L);
        app.setLearnerName("Alice");
        app.setLearnerEmail("alice@test.com");
        app.setCoverLetter("J'ai de l'expérience en Java et Spring Boot.");
        app.setStatus("PENDING");
        app.setScore(60);
        Application saved = applicationRepository.save(app);

        mockMvc.perform(get("/api/applications/" + saved.getId() + "/match-analysis")
                        .param("yearsOfExperience", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScore").exists())
                .andExpect(jsonPath("$.matchLevel").exists())
                .andExpect(jsonPath("$.matchedSkills").isArray())
                .andExpect(jsonPath("$.missingSkills").isArray())
                .andExpect(jsonPath("$.suggestions").isArray());
    }
}
