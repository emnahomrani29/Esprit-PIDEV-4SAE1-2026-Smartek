package com.smartek.offersservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartek.offersservice.dto.ApplicationRequest;
import com.smartek.offersservice.dto.ApplicationResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.GlobalExceptionHandler;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du controller ApplicationController — standalone MockMvc (sans contexte Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationController — Tests")
class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(applicationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ApplicationResponse buildResponse(Long id, Application.ApplicationStatus status, int score) {
        return ApplicationResponse.builder()
                .id(id).offerId(1L).offerTitle("Dev Java")
                .learnerId(10L).learnerName("Alice").learnerEmail("alice@test.com")
                .status(status).score(score)
                .appliedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/applications")
    class ApplyTests {

        @Test
        @DisplayName("201 Created avec score calculé")
        void shouldReturn201_withScore() throws Exception {
            ApplicationRequest request = ApplicationRequest.builder()
                    .offerId(1L).learnerId(10L)
                    .learnerName("Alice").learnerEmail("alice@test.com")
                    .coverLetter("Ma lettre de motivation")
                    .build();
            when(applicationService.applyToOffer(any()))
                    .thenReturn(buildResponse(1L, Application.ApplicationStatus.PENDING, 75));

            mockMvc.perform(post("/api/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.score").value(75));
        }

        @Test
        @DisplayName("422 si double candidature")
        void shouldReturn422_whenAlreadyApplied() throws Exception {
            when(applicationService.applyToOffer(any()))
                    .thenThrow(new BusinessException("Vous avez déjà postulé à cette offre"));

            ApplicationRequest request = ApplicationRequest.builder()
                    .offerId(1L).learnerId(10L)
                    .learnerName("Alice").learnerEmail("alice@test.com")
                    .build();

            mockMvc.perform(post("/api/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("déjà postulé")));
        }
    }

    @Nested
    @DisplayName("GET /api/applications/offer/{id}/ranked")
    class RankedApplicationsTests {

        @Test
        @DisplayName("200 avec candidatures triées par score décroissant")
        void shouldReturn200_withRankedApplications() throws Exception {
            List<ApplicationResponse> ranked = List.of(
                    buildResponse(1L, Application.ApplicationStatus.PENDING, 90),
                    buildResponse(2L, Application.ApplicationStatus.PENDING, 70),
                    buildResponse(3L, Application.ApplicationStatus.PENDING, 50)
            );
            when(applicationService.getApplicationsByOfferSortedByScore(1L)).thenReturn(ranked);

            mockMvc.perform(get("/api/applications/offer/1/ranked"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].score").value(90))
                    .andExpect(jsonPath("$[1].score").value(70))
                    .andExpect(jsonPath("$[2].score").value(50));
        }
    }

    @Nested
    @DisplayName("PUT /api/applications/{id}/status")
    class UpdateStatusTests {

        @Test
        @DisplayName("200 avec le nouveau statut")
        void shouldReturn200_withUpdatedStatus() throws Exception {
            when(applicationService.updateApplicationStatus(eq(1L), eq("ACCEPTED"), eq("Excellent")))
                    .thenReturn(buildResponse(1L, Application.ApplicationStatus.ACCEPTED, 80));

            mockMvc.perform(put("/api/applications/1/status")
                            .param("status", "ACCEPTED")
                            .param("recruiterNote", "Excellent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));
        }
    }

    @Nested
    @DisplayName("PUT /api/applications/{id}/withdraw")
    class WithdrawTests {

        @Test
        @DisplayName("200 avec statut WITHDRAWN")
        void shouldReturn200_whenWithdrawn() throws Exception {
            when(applicationService.withdrawApplication(1L, 10L))
                    .thenReturn(buildResponse(1L, Application.ApplicationStatus.WITHDRAWN, 50));

            mockMvc.perform(put("/api/applications/1/withdraw").param("learnerId", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WITHDRAWN"));
        }
    }

    @Test
    @DisplayName("GET /check/{offerId}/{learnerId} → 200 avec boolean")
    void hasApplied_shouldReturn200_withBoolean() throws Exception {
        when(applicationService.hasApplied(1L, 10L)).thenReturn(true);

        mockMvc.perform(get("/api/applications/check/1/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasApplied").value(true));
    }

    @Test
    @DisplayName("GET /offer/{offerId} → 200 avec liste des candidatures")
    void getApplicationsByOffer_shouldReturn200() throws Exception {
        when(applicationService.getApplicationsByOffer(1L))
                .thenReturn(List.of(buildResponse(1L, Application.ApplicationStatus.PENDING, 50)));

        mockMvc.perform(get("/api/applications/offer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].offerId").value(1));
    }

    @Test
    @DisplayName("GET /learner/{learnerId} → 200 avec liste des candidatures")
    void getApplicationsByLearner_shouldReturn200() throws Exception {
        when(applicationService.getApplicationsByLearner(10L))
                .thenReturn(List.of(buildResponse(1L, Application.ApplicationStatus.PENDING, 50)));

        mockMvc.perform(get("/api/applications/learner/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].learnerId").value(10));
    }

    @Nested
    @DisplayName("GET /api/applications/{id}/match-analysis")
    class MatchAnalysisTests {

        @Test
        @DisplayName("200 avec analyse complète")
        void matchAnalysis_shouldReturn200() throws Exception {
            Map<String, Object> analysis = Map.of(
                    "totalScore", 75,
                    "matchLevel", "EXCELLENT",
                    "matchedSkills", List.of("Java", "Spring"),
                    "missingSkills", List.of("Docker"),
                    "suggestions", List.of("Mentionnez Docker dans votre lettre")
            );
            when(applicationService.getMatchAnalysis(1L, 3)).thenReturn(analysis);

            mockMvc.perform(get("/api/applications/1/match-analysis")
                            .param("yearsOfExperience", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalScore").value(75))
                    .andExpect(jsonPath("$.matchLevel").value("EXCELLENT"));
        }

        @Test
        @DisplayName("200 sans yearsOfExperience")
        void matchAnalysis_withoutYears_shouldReturn200() throws Exception {
            Map<String, Object> analysis = Map.of(
                    "totalScore", 30,
                    "matchLevel", "MOYEN",
                    "matchedSkills", List.of(),
                    "missingSkills", List.of("Java"),
                    "suggestions", List.of("Précisez vos années d'expérience")
            );
            when(applicationService.getMatchAnalysis(1L, null)).thenReturn(analysis);

            mockMvc.perform(get("/api/applications/1/match-analysis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalScore").value(30));
        }

        @Test
        @DisplayName("404 si candidature non trouvée")
        void matchAnalysis_notFound_returns404() throws Exception {
            when(applicationService.getMatchAnalysis(99L, null))
                    .thenThrow(new ResourceNotFoundException("Candidature non trouvée: 99"));

            mockMvc.perform(get("/api/applications/99/match-analysis"))
                    .andExpect(status().isNotFound());
        }
    }
}
