package com.smartek.offersservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartek.offersservice.dto.InterviewFeedbackRequest;
import com.smartek.offersservice.dto.InterviewFeedbackResponse;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.GlobalExceptionHandler;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.service.InterviewFeedbackService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du controller InterviewFeedbackController — standalone MockMvc (sans contexte Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewFeedbackController — Tests")
class InterviewFeedbackControllerTest {

    @Mock
    private InterviewFeedbackService feedbackService;

    @InjectMocks
    private InterviewFeedbackController feedbackController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(feedbackController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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

    @Nested
    @DisplayName("POST /api/interview-feedbacks")
    class SubmitFeedbackTests {

        @Test
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
        @DisplayName("422 si feedback déjà soumis")
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

    @Nested
    @DisplayName("POST /api/interviews/feedback (endpoint alternatif)")
    class SubmitFeedbackAltTests {

        @Test
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
        @DisplayName("422 si entretien non terminé")
        void submitFeedbackAlt_notCompleted_returns422() throws Exception {
            when(feedbackService.submitFeedback(any()))
                    .thenThrow(new BusinessException("Le feedback ne peut être soumis que pour un entretien terminé"));

            mockMvc.perform(post("/api/interviews/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("PENDING"))))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("GET /api/interview-feedbacks/interview/{id}")
    class GetFeedbackByInterviewTests {

        @Test
        @DisplayName("200 avec le feedback trouvé")
        void getFeedbackByInterview_found_returns200() throws Exception {
            when(feedbackService.getFeedbackByInterview(10L)).thenReturn(buildResponse());

            mockMvc.perform(get("/api/interview-feedbacks/interview/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.interviewId").value(10))
                    .andExpect(jsonPath("$.rating").value(4));
        }

        @Test
        @DisplayName("404 si feedback non trouvé")
        void getFeedbackByInterview_notFound_returns404() throws Exception {
            when(feedbackService.getFeedbackByInterview(99L))
                    .thenThrow(new ResourceNotFoundException("Feedback non trouvé"));

            mockMvc.perform(get("/api/interview-feedbacks/interview/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("GET /api/interview-feedbacks/application/{id} → 200 avec liste")
    void getFeedbacksByApplication_returns200() throws Exception {
        when(feedbackService.getFeedbacksByApplication(1L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/interview-feedbacks/application/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].applicationId").value(1));
    }
}
