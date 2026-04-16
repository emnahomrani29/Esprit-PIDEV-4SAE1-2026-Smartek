package com.smartek.offersservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.offersservice.dto.InterviewFeedbackRequest;
import com.smartek.offersservice.dto.InterviewFeedbackResponse;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.GlobalExceptionHandler;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.security.JwtAuthFilter;
import com.smartek.offersservice.security.JwtService;
import com.smartek.offersservice.security.SecurityConfig;
import com.smartek.offersservice.service.InterviewFeedbackService;
import org.junit.jupiter.api.*;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests WebMvc pour InterviewFeedbackController.
 * Couvre les deux endpoints (primary + alternative) et les cas métier.
 */
@WebMvcTest(InterviewFeedbackController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@DisplayName("InterviewFeedbackController — Tests WebMvc")
class InterviewFeedbackControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private InterviewFeedbackService feedbackService;
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

    private InterviewFeedbackResponse buildResponse() {
        return InterviewFeedbackResponse.builder()
                .id(1L).interviewId(10L).applicationId(1L)
                .rating(4).decision("HIRED").submittedBy(5L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private InterviewFeedbackRequest buildRequest(String decision) {
        InterviewFeedbackRequest req = new InterviewFeedbackRequest();
        req.setInterviewId(10L);
        req.setApplicationId(1L);
        req.setRating(4);
        req.setStrengths("Bon profil");
        req.setWeaknesses("Peu d'expérience");
        req.setGeneralComment("Candidat prometteur");
        req.setDecision(decision);
        req.setSubmittedBy(5L);
        return req;
    }

    // ─── POST /api/interview-feedbacks ────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/interview-feedbacks")
    class SubmitFeedbackTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("201 Created avec feedback HIRED")
        void submitFeedback_hired_returns201() throws Exception {
            when(feedbackService.submitFeedback(any())).thenReturn(buildResponse());

            mockMvc.perform(post("/api/interview-feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("HIRED"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.decision").value("HIRED"))
                    .andExpect(jsonPath("$.interviewId").value(10));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("201 Created avec feedback REJECTED")
        void submitFeedback_rejected_returns201() throws Exception {
            InterviewFeedbackResponse rejected = InterviewFeedbackResponse.builder()
                    .id(2L).interviewId(10L).applicationId(1L)
                    .rating(2).decision("REJECTED").submittedBy(5L)
                    .createdAt(LocalDateTime.now()).build();
            when(feedbackService.submitFeedback(any())).thenReturn(rejected);

            mockMvc.perform(post("/api/interview-feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("REJECTED"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.decision").value("REJECTED"));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("422 si feedback déjà soumis pour cet entretien")
        void submitFeedback_duplicate_returns422() throws Exception {
            when(feedbackService.submitFeedback(any()))
                    .thenThrow(new BusinessException("Un feedback existe déjà pour cet entretien"));

            mockMvc.perform(post("/api/interview-feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("HIRED"))))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("feedback existe déjà")));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("404 si entretien non trouvé")
        void submitFeedback_interviewNotFound_returns404() throws Exception {
            when(feedbackService.submitFeedback(any()))
                    .thenThrow(new ResourceNotFoundException("Entretien non trouvé"));

            mockMvc.perform(post("/api/interview-feedbacks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("HIRED"))))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── POST /api/interviews/feedback (alternative) ──────────────────────────

    @Nested
    @DisplayName("POST /api/interviews/feedback (endpoint alternatif)")
    class SubmitFeedbackAltTests {

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("201 Created via endpoint alternatif")
        void submitFeedbackAlt_returns201() throws Exception {
            when(feedbackService.submitFeedback(any())).thenReturn(buildResponse());

            mockMvc.perform(post("/api/interviews/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("HIRED"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.decision").value("HIRED"));
        }

        @Test
        @WithMockUser(roles = "TRAINER")
        @DisplayName("422 si entretien non terminé (BusinessException)")
        void submitFeedbackAlt_notCompleted_returns422() throws Exception {
            when(feedbackService.submitFeedback(any()))
                    .thenThrow(new BusinessException("Le feedback ne peut être soumis que pour un entretien terminé"));

            mockMvc.perform(post("/api/interviews/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("PENDING"))))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // ─── GET /api/interview-feedbacks/interview/{id} ──────────────────────────

    @Nested
    @DisplayName("GET /api/interview-feedbacks/interview/{id}")
    class GetFeedbackByInterviewTests {

        @Test
        @WithMockUser
        @DisplayName("200 avec le feedback trouvé")
        void getFeedbackByInterview_found_returns200() throws Exception {
            when(feedbackService.getFeedbackByInterview(10L)).thenReturn(buildResponse());

            mockMvc.perform(get("/api/interview-feedbacks/interview/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.interviewId").value(10))
                    .andExpect(jsonPath("$.rating").value(4));
        }

        @Test
        @WithMockUser
        @DisplayName("404 si feedback non trouvé")
        void getFeedbackByInterview_notFound_returns404() throws Exception {
            when(feedbackService.getFeedbackByInterview(99L))
                    .thenThrow(new ResourceNotFoundException("Feedback non trouvé"));

            mockMvc.perform(get("/api/interview-feedbacks/interview/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── GET /api/interview-feedbacks/application/{id} ────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /api/interview-feedbacks/application/{id} → 200 avec liste")
    void getFeedbacksByApplication_returns200() throws Exception {
        when(feedbackService.getFeedbacksByApplication(1L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/interview-feedbacks/application/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].applicationId").value(1));
    }
}
