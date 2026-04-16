package com.smartek.offersservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.offersservice.dto.ApplicationRequest;
import com.smartek.offersservice.dto.ApplicationResponse;
import com.smartek.offersservice.entity.Application;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.GlobalExceptionHandler;
import com.smartek.offersservice.security.JwtAuthFilter;
import com.smartek.offersservice.security.JwtService;
import com.smartek.offersservice.security.SecurityConfig;
import com.smartek.offersservice.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du controller ApplicationController avec @WebMvcTest.
 */
@WebMvcTest(ApplicationController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@DisplayName("ApplicationController — Tests WebMvc")
class ApplicationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ApplicationService applicationService;
    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private JwtService jwtService;

    @BeforeEach
    void configureMockFilter() throws Exception {
        doAnswer(inv -> {
            jakarta.servlet.FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private ApplicationResponse buildResponse(Long id, Application.ApplicationStatus status, int score) {
        return ApplicationResponse.builder()
                .id(id).offerId(1L).offerTitle("Dev Java")
                .learnerId(10L).learnerName("Alice").learnerEmail("alice@test.com")
                .status(status).score(score)
                .appliedAt(LocalDateTime.now())
                .build();
    }

    // ─── POST /api/applications ───────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/applications")
    class ApplyTests {

        @Test
        @WithMockUser(roles = "LEARNER")
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
        @WithMockUser(roles = "LEARNER")
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

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("403 si rôle TRAINER")
        void shouldReturn403_whenTrainer() throws Exception {
            ApplicationRequest request = ApplicationRequest.builder()
                    .offerId(1L).learnerId(10L)
                    .learnerName("Alice").learnerEmail("alice@test.com")
                    .build();
            mockMvc.perform(post("/api/applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── GET /api/applications/offer/{id}/ranked ──────────────────────────────

    @Nested
    @DisplayName("GET /api/applications/offer/{id}/ranked")
    class RankedApplicationsTests {

        @Test
        @WithMockUser(roles = "TRAINER")
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

    // ─── PUT /api/applications/{id}/status ───────────────────────────────────

    @Nested
    @DisplayName("PUT /api/applications/{id}/status")
    class UpdateStatusTests {

        @Test
        @WithMockUser(roles = "TRAINER")
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

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("403 si rôle LEARNER")
        void shouldReturn403_whenLearner() throws Exception {
            mockMvc.perform(put("/api/applications/1/status").param("status", "ACCEPTED"))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── PUT /api/applications/{id}/withdraw ─────────────────────────────────

    @Nested
    @DisplayName("PUT /api/applications/{id}/withdraw")
    class WithdrawTests {

        @Test
        @WithMockUser(roles = "LEARNER")
        @DisplayName("200 avec statut WITHDRAWN")
        void shouldReturn200_whenWithdrawn() throws Exception {
            when(applicationService.withdrawApplication(1L, 10L))
                    .thenReturn(buildResponse(1L, Application.ApplicationStatus.WITHDRAWN, 50));

            mockMvc.perform(put("/api/applications/1/withdraw").param("learnerId", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WITHDRAWN"));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("403 si rôle TRAINER")
        void shouldReturn403_whenTrainer() throws Exception {
            mockMvc.perform(put("/api/applications/1/withdraw").param("learnerId", "10"))
                    .andExpect(status().isForbidden());
        }
    }

    // ─── GET /api/applications/check/{offerId}/{learnerId} ───────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /check/{offerId}/{learnerId} → 200 avec boolean")
    void hasApplied_shouldReturn200_withBoolean() throws Exception {
        when(applicationService.hasApplied(1L, 10L)).thenReturn(true);

        mockMvc.perform(get("/api/applications/check/1/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasApplied").value(true));
    }
}
