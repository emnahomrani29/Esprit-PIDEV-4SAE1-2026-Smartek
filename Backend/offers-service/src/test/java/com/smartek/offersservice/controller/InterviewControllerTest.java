package com.smartek.offersservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartek.offersservice.dto.InterviewRequest;
import com.smartek.offersservice.dto.InterviewResponse;
import com.smartek.offersservice.exception.BusinessException;
import com.smartek.offersservice.exception.GlobalExceptionHandler;
import com.smartek.offersservice.exception.ResourceNotFoundException;
import com.smartek.offersservice.service.InterviewService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du controller InterviewController — standalone MockMvc (sans contexte Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewController — Tests")
class InterviewControllerTest {

    @Mock
    private InterviewService interviewService;

    @InjectMocks
    private InterviewController interviewController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(interviewController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private InterviewResponse buildResponse(Long id, String status) {
        return InterviewResponse.builder()
                .id(id).applicationId(1L).offerId(100L).learnerId(2L)
                .learnerName("Bob Dupont").learnerEmail("bob@test.com")
                .interviewDate(LocalDateTime.now().plusDays(3))
                .location("Salle A").status(status).createdBy(5L)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/interviews")
    class CreateInterviewTests {

        @Test
        @DisplayName("201 Created pour une candidature ACCEPTED")
        void shouldReturn201_whenApplicationAccepted() throws Exception {
            InterviewRequest request = InterviewRequest.builder()
                    .applicationId(1L)
                    .interviewDate(LocalDateTime.now().plusDays(3))
                    .location("Salle A").createdBy(5L).build();
            when(interviewService.createInterview(any())).thenReturn(buildResponse(10L, "SCHEDULED"));

            mockMvc.perform(post("/api/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SCHEDULED"))
                    .andExpect(jsonPath("$.learnerName").value("Bob Dupont"));
        }

        @Test
        @DisplayName("422 si candidature non ACCEPTED")
        void shouldReturn400_whenApplicationNotAccepted() throws Exception {
            InterviewRequest request = InterviewRequest.builder()
                    .applicationId(2L)
                    .interviewDate(LocalDateTime.now().plusDays(3))
                    .location("Salle A").createdBy(5L).build();
            when(interviewService.createInterview(any()))
                    .thenThrow(new BusinessException("La candidature doit être acceptée"));

            mockMvc.perform(post("/api/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("404 si candidature introuvable")
        void shouldReturn400_whenApplicationNotFound() throws Exception {
            InterviewRequest request = InterviewRequest.builder()
                    .applicationId(99L)
                    .interviewDate(LocalDateTime.now().plusDays(3))
                    .location("Salle A").createdBy(5L).build();
            when(interviewService.createInterview(any()))
                    .thenThrow(new ResourceNotFoundException("Candidature non trouvée: 99"));

            mockMvc.perform(post("/api/interviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("GET /api/interviews → 200 avec liste complète")
    void getAllInterviews_shouldReturn200() throws Exception {
        when(interviewService.getAllInterviews())
                .thenReturn(List.of(buildResponse(10L, "SCHEDULED"), buildResponse(11L, "COMPLETED")));

        mockMvc.perform(get("/api/interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/interviews/company/{id} → 200")
    void getInterviewsByCompany_shouldReturn200() throws Exception {
        when(interviewService.getInterviewsByCompany(1L))
                .thenReturn(List.of(buildResponse(10L, "SCHEDULED")));

        mockMvc.perform(get("/api/interviews/company/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].offerId").value(100));
    }

    @Test
    @DisplayName("GET /api/interviews/offer/{id} → 200")
    void getInterviewsByOffer_shouldReturn200() throws Exception {
        when(interviewService.getInterviewsByOffer(100L))
                .thenReturn(List.of(buildResponse(10L, "SCHEDULED")));

        mockMvc.perform(get("/api/interviews/offer/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].offerId").value(100));
    }

    @Test
    @DisplayName("GET /api/interviews/learner/{id} → 200")
    void getInterviewsByLearner_shouldReturn200() throws Exception {
        when(interviewService.getInterviewsByLearner(2L))
                .thenReturn(List.of(buildResponse(10L, "SCHEDULED")));

        mockMvc.perform(get("/api/interviews/learner/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].learnerId").value(2));
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} → 200")
    void getInterviewsByApplication_shouldReturn200() throws Exception {
        when(interviewService.getInterviewsByApplication(1L))
                .thenReturn(List.of(buildResponse(10L, "SCHEDULED")));

        mockMvc.perform(get("/api/interviews/application/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(1));
    }

    @Nested
    @DisplayName("PUT /api/interviews/{id}/status")
    class UpdateStatusTests {

        @Test
        @DisplayName("200 avec le nouveau statut COMPLETED")
        void shouldReturn200_whenStatusUpdated() throws Exception {
            when(interviewService.updateInterviewStatus(10L, "COMPLETED"))
                    .thenReturn(buildResponse(10L, "COMPLETED"));

            mockMvc.perform(put("/api/interviews/10/status").param("status", "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("400 si statut invalide")
        void shouldReturn400_whenInvalidStatus() throws Exception {
            when(interviewService.updateInterviewStatus(10L, "INVALID_STATUS"))
                    .thenThrow(new RuntimeException("Statut invalide"));

            mockMvc.perform(put("/api/interviews/10/status").param("status", "INVALID_STATUS"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 si entretien introuvable")
        void shouldReturn400_whenInterviewNotFound() throws Exception {
            when(interviewService.updateInterviewStatus(99L, "COMPLETED"))
                    .thenThrow(new RuntimeException("Entretien non trouvé"));

            mockMvc.perform(put("/api/interviews/99/status").param("status", "COMPLETED"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/interviews/{id}")
    class UpdateInterviewTests {

        @Test
        @DisplayName("200 avec l'entretien mis à jour")
        void shouldReturn200_whenUpdated() throws Exception {
            InterviewRequest request = InterviewRequest.builder()
                    .applicationId(1L)
                    .interviewDate(LocalDateTime.now().plusDays(5))
                    .location("Salle B").meetingLink("https://meet.google.com/xyz")
                    .notes("Nouvelles notes").createdBy(5L).build();
            when(interviewService.updateInterview(eq(10L), any()))
                    .thenReturn(buildResponse(10L, "SCHEDULED"));

            mockMvc.perform(put("/api/interviews/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10));
        }

        @Test
        @DisplayName("400 si entretien introuvable")
        void shouldReturn400_whenNotFound() throws Exception {
            InterviewRequest request = InterviewRequest.builder()
                    .applicationId(1L)
                    .interviewDate(LocalDateTime.now().plusDays(5))
                    .location("Salle B").createdBy(5L).build();
            when(interviewService.updateInterview(eq(99L), any()))
                    .thenThrow(new RuntimeException("Entretien non trouvé"));

            mockMvc.perform(put("/api/interviews/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/interviews/{id}")
    class DeleteInterviewTests {

        @Test
        @DisplayName("204 No Content si suppression réussie")
        void shouldReturn204_whenDeleted() throws Exception {
            doNothing().when(interviewService).deleteInterview(10L);

            mockMvc.perform(delete("/api/interviews/10"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("400 si entretien introuvable")
        void shouldReturn400_whenNotFound() throws Exception {
            doThrow(new RuntimeException("Entretien non trouvé"))
                    .when(interviewService).deleteInterview(99L);

            mockMvc.perform(delete("/api/interviews/99"))
                    .andExpect(status().isBadRequest());
        }
    }
}
