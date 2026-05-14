package com.smartek.offersservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.entity.Interview;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Interview — Tests d'intégration")
class InterviewIntegrationTest {

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

    private Offer saveOffer() {
        return offerRepository.save(Offer.builder()
                .title("Dev Java").description("Desc").companyName("TechCorp")
                .location("Paris").contractType("CDI").companyId(1L)
                .status("ACTIVE").viewCount(0L).positions(1).remote(false).build());
    }

    private Application saveApplication(Offer offer, String status) {
        Application app = new Application();
        app.setOffer(offer);
        app.setLearnerId(10L);
        app.setLearnerName("Alice Dupont");
        app.setLearnerEmail("alice@test.com");
        app.setStatus(status);
        app.setScore(75);
        return applicationRepository.save(app);
    }

    private Interview saveInterview(Application app, Offer offer, Interview.InterviewStatus status) {
        Interview interview = Interview.builder()
                .applicationId(app.getId())
                .offerId(offer.getId())
                .learnerId(app.getLearnerId())
                .learnerName(app.getLearnerName())
                .learnerEmail(app.getLearnerEmail())
                .interviewDate(LocalDateTime.now().plusDays(7))
                .location("Salle A")
                .status(status)
                .createdBy(5L)
                .build();
        return interviewRepository.save(interview);
    }

    // ─── Créer un entretien ───────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/interviews — Créer un entretien")
    class CreateInterviewTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("201 pour une candidature ACCEPTED")
        void create_shouldReturn201_whenApplicationAccepted() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");

            String body = """
                {
                    "applicationId": %d,
                    "interviewDate": "2030-06-15T10:00:00",
                    "location": "Salle A",
                    "meetingLink": "https://meet.google.com/abc",
                    "notes": "Préparer les questions Java",
                    "createdBy": 5
                }
                """.formatted(app.getId());

            MvcResult result = mockMvc.perform(post("/api/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SCHEDULED"))
                    .andExpect(jsonPath("$.learnerName").value("Alice Dupont"))
                    .andExpect(jsonPath("$.offerId").value(offer.getId()))
                    .andReturn();

            Long interviewId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
            assertThat(interviewRepository.findById(interviewId)).isPresent();
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("422 pour une candidature PENDING (BusinessException)")
        void create_shouldReturn400_whenApplicationPending() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "PENDING");

            String body = """
                {
                    "applicationId": %d,
                    "interviewDate": "2030-06-15T10:00:00",
                    "location": "Salle A",
                    "createdBy": 5
                }
                """.formatted(app.getId());

            mockMvc.perform(post("/api/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("404 si candidature introuvable (ResourceNotFoundException)")
        void create_shouldReturn400_whenApplicationNotFound() throws Exception {
            String body = """
                {
                    "applicationId": 99999,
                    "interviewDate": "2030-06-15T10:00:00",
                    "location": "Salle A",
                    "createdBy": 5
                }
                """;

            mockMvc.perform(post("/api/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── Récupérer les entretiens ─────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/interviews — Récupérer les entretiens")
    class GetInterviewsTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("GET /api/interviews → 200 avec tous les entretiens")
        void getAllInterviews_shouldReturn200() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            saveInterview(app, offer, Interview.InterviewStatus.SCHEDULED);

            mockMvc.perform(get("/api/interviews"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("GET /api/interviews/offer/{id} → 200 avec entretiens de l'offre")
        void getByOffer_shouldReturn200() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            saveInterview(app, offer, Interview.InterviewStatus.SCHEDULED);

            mockMvc.perform(get("/api/interviews/offer/" + offer.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].offerId").value(offer.getId()));
        }

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("GET /api/interviews/learner/{id} → 200 avec entretiens du candidat")
        void getByLearner_shouldReturn200() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            saveInterview(app, offer, Interview.InterviewStatus.SCHEDULED);

            mockMvc.perform(get("/api/interviews/learner/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].learnerId").value(10));
        }
    }

    // ─── Mise à jour du statut ────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/interviews/{id}/status — Mise à jour statut")
    class UpdateStatusTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("200 avec statut COMPLETED")
        void updateStatus_shouldReturn200_withCompleted() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            Interview interview = saveInterview(app, offer, Interview.InterviewStatus.SCHEDULED);

            mockMvc.perform(put("/api/interviews/" + interview.getId() + "/status")
                            .param("status", "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            Interview updated = interviewRepository.findById(interview.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(Interview.InterviewStatus.COMPLETED);
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("200 avec statut CANCELLED")
        void updateStatus_shouldReturn200_withCancelled() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            Interview interview = saveInterview(app, offer, Interview.InterviewStatus.SCHEDULED);

            mockMvc.perform(put("/api/interviews/" + interview.getId() + "/status")
                            .param("status", "CANCELLED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("400 si statut invalide")
        void updateStatus_shouldReturn400_whenInvalidStatus() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            Interview interview = saveInterview(app, offer, Interview.InterviewStatus.SCHEDULED);

            mockMvc.perform(put("/api/interviews/" + interview.getId() + "/status")
                            .param("status", "INVALID_STATUS"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── Suppression ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "TRAINER")
    @DisplayName("DELETE /api/interviews/{id} → 204 No Content")
    void deleteInterview_shouldReturn204() throws Exception {
        Offer offer = saveOffer();
        Application app = saveApplication(offer, "ACCEPTED");
        Interview interview = saveInterview(app, offer, Interview.InterviewStatus.SCHEDULED);

        mockMvc.perform(delete("/api/interviews/" + interview.getId()))
                .andExpect(status().isNoContent());

        assertThat(interviewRepository.findById(interview.getId())).isEmpty();
    }

    // ─── Feedback sur entretien ───────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/interview-feedbacks — Feedback")
    class FeedbackTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("201 avec feedback HIRED → candidature ACCEPTED")
        void submitFeedback_hired_shouldUpdateApplicationToAccepted() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            Interview interview = saveInterview(app, offer, Interview.InterviewStatus.COMPLETED);

            String body = """
                {
                    "interviewId": %d,
                    "applicationId": %d,
                    "rating": 5,
                    "strengths": "Excellente maîtrise de Java",
                    "weaknesses": "Peu d'expérience en cloud",
                    "generalComment": "Candidat recommandé",
                    "decision": "HIRED",
                    "submittedBy": 5
                }
                """.formatted(interview.getId(), app.getId());

            mockMvc.perform(post("/api/interview-feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.decision").value("HIRED"))
                    .andExpect(jsonPath("$.rating").value(5));

            Application updated = applicationRepository.findById(app.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo("ACCEPTED");
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("201 avec feedback REJECTED → candidature REJECTED")
        void submitFeedback_rejected_shouldUpdateApplicationToRejected() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            Interview interview = saveInterview(app, offer, Interview.InterviewStatus.COMPLETED);

            String body = """
                {
                    "interviewId": %d,
                    "applicationId": %d,
                    "rating": 2,
                    "decision": "REJECTED",
                    "submittedBy": 5
                }
                """.formatted(interview.getId(), app.getId());

            mockMvc.perform(post("/api/interview-feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.decision").value("REJECTED"));

            Application updated = applicationRepository.findById(app.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo("REJECTED");
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("422 si feedback déjà soumis pour cet entretien")
        void submitFeedback_duplicate_shouldReturn422() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            Interview interview = saveInterview(app, offer, Interview.InterviewStatus.COMPLETED);

            String body = """
                {
                    "interviewId": %d,
                    "applicationId": %d,
                    "rating": 4,
                    "decision": "HIRED",
                    "submittedBy": 5
                }
                """.formatted(interview.getId(), app.getId());

            mockMvc.perform(post("/api/interview-feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            // Deuxième soumission → 422
            mockMvc.perform(post("/api/interview-feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value(containsString("feedback existe déjà")));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("Feedback sur entretien SCHEDULED → auto-complété")
        void submitFeedback_scheduledInterview_shouldAutoComplete() throws Exception {
            Offer offer = saveOffer();
            Application app = saveApplication(offer, "ACCEPTED");
            Interview interview = saveInterview(app, offer, Interview.InterviewStatus.SCHEDULED);

            String body = """
                {
                    "interviewId": %d,
                    "applicationId": %d,
                    "rating": 4,
                    "decision": "PENDING",
                    "submittedBy": 5
                }
                """.formatted(interview.getId(), app.getId());

            mockMvc.perform(post("/api/interview-feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            Interview updated = interviewRepository.findById(interview.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(Interview.InterviewStatus.COMPLETED);
        }
    }
}
