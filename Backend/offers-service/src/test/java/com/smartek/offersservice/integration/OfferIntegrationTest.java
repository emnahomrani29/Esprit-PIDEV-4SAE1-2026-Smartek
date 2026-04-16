package com.smartek.offersservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.offersservice.dto.OfferRequest;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Offer;
import com.smartek.offersservice.repository.ApplicationRepository;
import com.smartek.offersservice.repository.InterviewFeedbackRepository;
import com.smartek.offersservice.repository.InterviewRepository;
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

/**
 * Tests d'intégration pour l'offers-service.
 *
 * Utilise H2 en mémoire (profil "test") + MockMvc + Spring Security Test.
 *
 * Couvre :
 *  - Sécurité JWT (rôles, accès refusé)
 *  - Validation des DTOs
 *  - Règles métier via HTTP (422 Unprocessable Entity)
 *  - Workflow complet : offre → candidature → entretien → feedback
 *  - Pagination et recherche
 *  - Statistiques
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Offers Service — Tests d'intégration")
class OfferIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OfferRepository offerRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private InterviewRepository interviewRepository;
    @Autowired private InterviewFeedbackRepository feedbackRepository;

    @BeforeEach
    void cleanDatabase() {
        feedbackRepository.deleteAll();
        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        offerRepository.deleteAll();
    }

    // ─── Health ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/offers/health → 200 OK (public)")
    void health_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/offers/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("running")));
    }

    // ─── Sécurité ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Sécurité — Contrôle d'accès par rôle")
    class SecurityTests {

        @Test
        @DisplayName("POST /api/offers → 403 si non authentifié")
        void createOffer_shouldReturn403_whenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildOfferRequest("Test"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("POST /api/offers → 403 si rôle LEARNER")
        void createOffer_shouldReturn403_whenLearner() throws Exception {
            mockMvc.perform(post("/api/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildOfferRequest("Test"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("POST /api/offers → 201 si rôle TRAINER")
        void createOffer_shouldReturn201_whenTrainer() throws Exception {
            mockMvc.perform(post("/api/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildOfferRequest("Dev Java"))))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/offers → 201 si rôle ADMIN")
        void createOffer_shouldReturn201_whenAdmin() throws Exception {
            mockMvc.perform(post("/api/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildOfferRequest("Dev Python"))))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("GET /api/offers/{id} → 200 sans authentification (public)")
        void getOffer_shouldReturn200_whenPublic() throws Exception {
            Offer saved = offerRepository.save(buildOffer("Dev Java"));

            mockMvc.perform(get("/api/offers/" + saved.getId()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("POST /api/applications → 201 si rôle LEARNER")
        void applyToOffer_shouldReturn201_whenLearner() throws Exception {
            Offer offer = offerRepository.save(buildOffer("Dev Java"));

            String body = """
                {
                    "offerId": %d,
                    "learnerId": 10,
                    "learnerName": "Alice Dupont",
                    "learnerEmail": "alice@test.com",
                    "coverLetter": "Je suis motivée pour ce poste."
                }
                """.formatted(offer.getId());

            mockMvc.perform(post("/api/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("POST /api/applications → 403 si rôle TRAINER")
        void applyToOffer_shouldReturn403_whenTrainer() throws Exception {
            Offer offer = offerRepository.save(buildOffer("Dev Java"));
            String body = """
                {
                    "offerId": %d,
                    "learnerId": 10,
                    "learnerName": "Alice",
                    "learnerEmail": "alice@test.com"
                }
                """.formatted(offer.getId());
            mockMvc.perform(post("/api/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Validation des DTOs")
    class ValidationTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("POST /api/offers → 400 si titre manquant")
        void createOffer_shouldReturn400_whenTitleMissing() throws Exception {
            String body = """
                {
                    "description": "Desc",
                    "companyName": "Corp",
                    "location": "Paris",
                    "contractType": "CDI",
                    "companyId": 1
                }
                """;

            mockMvc.perform(post("/api/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.title").exists());
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("POST /api/offers → 400 si companyId manquant")
        void createOffer_shouldReturn400_whenCompanyIdMissing() throws Exception {
            String body = """
                {
                    "title": "Dev Java",
                    "description": "Desc",
                    "companyName": "Corp",
                    "location": "Paris",
                    "contractType": "CDI"
                }
                """;

            mockMvc.perform(post("/api/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.companyId").exists());
        }
    }

    // ─── Règles métier via HTTP ───────────────────────────────────────────────

    @Nested
    @DisplayName("Règles métier — Réponses HTTP")
    class BusinessRuleTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("POST /api/offers → 422 si date d'expiration dans le passé")
        void createOffer_shouldReturn422_whenExpiresAtInPast() throws Exception {
            String body = """
                {
                    "title": "Dev Java",
                    "description": "Desc",
                    "companyName": "Corp",
                    "location": "Paris",
                    "contractType": "CDI",
                    "companyId": 1,
                    "expiresAt": "2020-01-01T00:00:00"
                }
                """;

            mockMvc.perform(post("/api/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("POST /api/applications → 422 si double candidature")
        void applyToOffer_shouldReturn422_whenAlreadyApplied() throws Exception {
            Offer offer = offerRepository.save(buildOffer("Dev Java"));

            String body = """
                {
                    "offerId": %d,
                    "learnerId": 10,
                    "learnerName": "Alice",
                    "learnerEmail": "alice@test.com",
                    "coverLetter": "Ma lettre."
                }
                """.formatted(offer.getId());

            // Première candidature
            mockMvc.perform(post("/api/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            // Deuxième candidature → 422
            mockMvc.perform(post("/api/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("POST /api/applications → 422 si offre CLOSED")
        void applyToOffer_shouldReturn422_whenOfferClosed() throws Exception {
            Offer offer = buildOffer("Dev Java");
            offer.setStatus("CLOSED");
            offer = offerRepository.save(offer);

            String body = """
                {
                    "offerId": %d,
                    "learnerId": 10,
                    "learnerName": "Alice",
                    "learnerEmail": "alice@test.com"
                }
                """.formatted(offer.getId());

            mockMvc.perform(post("/api/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("DELETE /api/offers/{id} → 422 si candidatures acceptées")
        void deleteOffer_shouldReturn422_whenAcceptedApplicationsExist() throws Exception {
            Offer offer = offerRepository.save(buildOffer("Dev Java"));

            // Créer une candidature ACCEPTED directement en BDD
            Application app = Application.builder()
                    .learnerId(10L).learnerName("Alice").learnerEmail("alice@test.com")
                    .status("ACCEPTED").score(80)
                    .build();
            app.setOffer(offer);
            applicationRepository.save(app);

            mockMvc.perform(delete("/api/offers/" + offer.getId()))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // ─── Workflow complet ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Workflow complet : Offre → Candidature → Entretien → Feedback")
    class FullWorkflowTests {

        @Test
        @DisplayName("Flux complet bout-en-bout")
        void fullWorkflow_shouldSucceed() throws Exception {
            // ── Étape 1 : Créer une offre (TRAINER) ──────────────────────────
            MvcResult createOfferResult = mockMvc.perform(
                    post("/api/offers")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("trainer@test.com").roles("TRAINER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildOfferRequest("Dev Java Senior"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Dev Java Senior"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andReturn();

            Long offerId = objectMapper.readTree(createOfferResult.getResponse().getContentAsString())
                    .get("id").asLong();

            // ── Étape 2 : Consulter l'offre (public) → vues incrémentées ─────
            mockMvc.perform(get("/api/offers/" + offerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.viewCount").value(greaterThanOrEqualTo(1)));

            // ── Étape 3 : Postuler (LEARNER) ─────────────────────────────────
            String applyBody = """
                {
                    "offerId": %d,
                    "learnerId": 20,
                    "learnerName": "Bob Martin",
                    "learnerEmail": "bob@test.com",
                    "coverLetter": "Je suis très motivé pour ce poste. J'ai une grande expérience en Java et Spring Boot.",
                    "candidateSkills": ["Java", "Spring"],
                    "yearsOfExperience": 4
                }
                """.formatted(offerId);

            MvcResult applyResult = mockMvc.perform(
                    post("/api/applications")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("bob@test.com").roles("LEARNER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(applyBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.score").value(greaterThan(0)))
                    .andReturn();

            Long applicationId = objectMapper.readTree(applyResult.getResponse().getContentAsString())
                    .get("id").asLong();

            // ── Étape 4 : Accepter la candidature (TRAINER) ───────────────────
            mockMvc.perform(
                    put("/api/applications/" + applicationId + "/status")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("trainer@test.com").roles("TRAINER"))
                            .param("status", "ACCEPTED")
                            .param("recruiterNote", "Excellent profil"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.recruiterNote").value("Excellent profil"));

            // ── Étape 5 : Planifier un entretien (TRAINER) ────────────────────
            String interviewBody = """
                {
                    "applicationId": %d,
                    "interviewDate": "2030-06-15T10:00:00",
                    "location": "Paris — Siège social",
                    "meetingLink": "https://meet.google.com/abc-def",
                    "notes": "Préparer les questions techniques Java",
                    "createdBy": 5
                }
                """.formatted(applicationId);

            MvcResult interviewResult = mockMvc.perform(
                    post("/api/interviews")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("trainer@test.com").roles("TRAINER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(interviewBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SCHEDULED"))
                    .andExpect(jsonPath("$.learnerName").value("Bob Martin"))
                    .andReturn();

            Long interviewId = objectMapper.readTree(interviewResult.getResponse().getContentAsString())
                    .get("id").asLong();

            // ── Étape 6 : Marquer l'entretien comme terminé ───────────────────
            mockMvc.perform(
                    put("/api/interviews/" + interviewId + "/status")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("trainer@test.com").roles("TRAINER"))
                            .param("status", "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            // ── Étape 7 : Soumettre le feedback (TRAINER) ─────────────────────
            String feedbackBody = """
                {
                    "interviewId": %d,
                    "applicationId": %d,
                    "rating": 5,
                    "strengths": "Excellente maîtrise de Java et Spring Boot",
                    "weaknesses": "Peu d'expérience en architecture microservices",
                    "generalComment": "Candidat très prometteur, recommandé pour le poste",
                    "decision": "HIRED",
                    "submittedBy": 5
                }
                """.formatted(interviewId, applicationId);

            mockMvc.perform(
                    post("/api/interviews/feedback")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("trainer@test.com").roles("TRAINER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(feedbackBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.decision").value("HIRED"))
                    .andExpect(jsonPath("$.rating").value(5));

            // ── Vérification finale : la candidature est ACCEPTED ─────────────
            Application finalApp = applicationRepository.findById(applicationId).orElseThrow();
            assertThat(finalApp.getStatus()).isEqualTo("ACCEPTED");
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Impossible de planifier un entretien pour une candidature PENDING")
        void shouldFail_whenSchedulingInterviewForPendingApplication() throws Exception {
            Offer offer = offerRepository.save(buildOffer("Dev Java"));
            Application app = Application.builder()
                    .learnerId(10L).learnerName("Alice").learnerEmail("alice@test.com")
                    .status("PENDING").score(50)
                    .build();
            app.setOffer(offer);
            Application savedApp = applicationRepository.save(app);

            String body = """
                {
                    "applicationId": %d,
                    "interviewDate": "2030-06-15T10:00:00",
                    "location": "Paris",
                    "createdBy": 5
                }
                """.formatted(savedApp.getId());

            mockMvc.perform(post("/api/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Feedback sur entretien SCHEDULED → auto-complété et feedback accepté")
        void shouldAutoComplete_scheduledInterview_whenSubmittingFeedback() throws Exception {
            Offer offer = offerRepository.save(buildOffer("Dev Java"));
            Application app = Application.builder()
                    .learnerId(10L).learnerName("Alice").learnerEmail("alice@test.com")
                    .status("ACCEPTED").score(50)
                    .build();
            app.setOffer(offer);
            Application savedApp = applicationRepository.save(app);

            com.smartek.offersservice.entity.Interview interview =
                    com.smartek.offersservice.entity.Interview.builder()
                            .offerId(offer.getId()).learnerId(10L)
                            .learnerName("Alice").learnerEmail("alice@test.com")
                            .interviewDate(java.time.LocalDateTime.now().plusDays(7))
                            .location("Paris").createdBy(5L)
                            .status(com.smartek.offersservice.entity.Interview.InterviewStatus.SCHEDULED)
                            .build();
            interview.setApplicationId(savedApp.getId());
            com.smartek.offersservice.entity.Interview savedInterview = interviewRepository.save(interview);

            String body = """
                {
                    "interviewId": %d,
                    "applicationId": %d,
                    "rating": 4,
                    "decision": "HIRED",
                    "submittedBy": 5
                }
                """.formatted(savedInterview.getId(), savedApp.getId());

            // Avec le nouveau comportement, le feedback sur SCHEDULED auto-complète l'entretien
            mockMvc.perform(post("/api/interviews/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.decision").value("HIRED"));
        }
    }

    // ─── Recherche et pagination ──────────────────────────────────────────────

    @Nested
    @DisplayName("Recherche et pagination")
    class SearchAndPaginationTests {

        @Test
        @DisplayName("POST /api/offers/search → 200 avec résultats paginés")
        void searchOffers_shouldReturnPagedResults() throws Exception {
            offerRepository.save(buildOffer("Développeur Java Senior"));
            offerRepository.save(buildOffer("Data Scientist Python"));

            String body = """
                {
                    "keyword": "Java",
                    "page": 0,
                    "size": 10,
                    "sortBy": "createdAt",
                    "sortDir": "desc"
                }
                """;

            mockMvc.perform(post("/api/offers/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(0)))
                    .andExpect(jsonPath("$.pageable").exists());
        }

        @Test
        @DisplayName("GET /api/offers/top-viewed → 200 avec liste")
        void topViewedOffers_shouldReturnList() throws Exception {
            mockMvc.perform(get("/api/offers/top-viewed").param("limit", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("GET /api/offers → 200 avec pagination")
        void getAllOffers_shouldReturnPaginatedResults() throws Exception {
            offerRepository.save(buildOffer("Offre 1"));
            offerRepository.save(buildOffer("Offre 2"));

            mockMvc.perform(get("/api/offers")
                            .param("page", "0")
                            .param("size", "5")
                            .param("sortBy", "createdAt")
                            .param("sortDir", "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
        }
    }

    // ─── Statistiques ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Statistiques")
    class StatsTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("GET /api/offers/stats/company/{id} → 200 avec toutes les métriques")
        void getStats_shouldReturnAllMetrics() throws Exception {
            mockMvc.perform(get("/api/offers/stats/company/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.companyId").value(1))
                    .andExpect(jsonPath("$.totalOffers").exists())
                    .andExpect(jsonPath("$.activeOffers").exists())
                    .andExpect(jsonPath("$.totalApplications").exists())
                    .andExpect(jsonPath("$.acceptanceRate").exists())
                    .andExpect(jsonPath("$.averageApplicationScore").exists());
        }
    }

    // ─── Candidatures classées par score ─────────────────────────────────────

    @Nested
    @DisplayName("Classement des candidatures par score")
    class RankedApplicationsTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("GET /api/applications/offer/{id}/ranked → candidatures triées par score")
        void rankedApplications_shouldBeSortedByScore() throws Exception {
            Offer offer = offerRepository.save(buildOffer("Dev Java"));

            // Créer des candidatures avec des scores différents
            for (int score : new int[]{30, 90, 60}) {
                Application app = Application.builder()
                        .learnerId((long) score)
                        .learnerName("Candidat " + score)
                        .learnerEmail("candidat" + score + "@test.com")
                        .status("PENDING")
                        .score(score)
                        .build();
                app.setOffer(offer);
                applicationRepository.save(app);
            }

            mockMvc.perform(get("/api/applications/offer/" + offer.getId() + "/ranked"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].score").value(greaterThanOrEqualTo(
                            objectMapper.readTree(
                                    mockMvc.perform(get("/api/applications/offer/" + offer.getId() + "/ranked"))
                                            .andReturn().getResponse().getContentAsString()
                            ).get(1).get("score").asInt()
                    )));
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private OfferRequest buildOfferRequest(String title) {
        return OfferRequest.builder()
                .title(title)
                .description("Description du poste " + title)
                .companyName("TechCorp")
                .location("Paris")
                .contractType("CDI")
                .companyId(1L)
                .positions(3)
                .requiredSkills(Set.of("Java", "Spring"))
                .build();
    }

    private Offer buildOffer(String title) {
        return Offer.builder()
                .title(title)
                .description("Description")
                .companyName("TechCorp")
                .location("Paris")
                .contractType("CDI")
                .companyId(1L)
                .positions(3)
                .status("ACTIVE")
                .viewCount(0L)
                .remote(false)
                .build();
    }
}
