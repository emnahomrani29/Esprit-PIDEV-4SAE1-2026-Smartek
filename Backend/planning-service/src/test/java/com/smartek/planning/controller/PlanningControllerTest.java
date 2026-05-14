package com.smartek.planning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartek.planning.dto.PlanningRequest;
import com.smartek.planning.dto.PlanningResponse;
import com.smartek.planning.service.PlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour PlanningController.
 * JwtAuthenticationFilter exclu du scan pour éviter l'injection de JwtService.
 * GlobalExceptionHandler importé pour tester la gestion des exceptions.
 */
@WebMvcTest(
    value = PlanningController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.smartek.planning.security.JwtAuthenticationFilter.class
    )
)
@org.springframework.context.annotation.Import(com.smartek.planning.exception.GlobalExceptionHandler.class)
@DisplayName("PlanningController - Tests d'intégration")
class PlanningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanningService planningService;

    private ObjectMapper objectMapper;
    private PlanningRequest validRequest;
    private PlanningResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        validRequest = new PlanningRequest(
                LocalDate.of(2026, 5, 20),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                "Formation Spring Boot",
                "Session de formation",
                "TRAINING",
                "Salle A",
                "#3498db"
        );

        sampleResponse = new PlanningResponse(
                1L,
                LocalDate.of(2026, 5, 20),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                "Formation Spring Boot",
                "Session de formation",
                "TRAINING",
                "Salle A",
                "#3498db",
                "DRAFT"
        );
    }

    @Test
    @DisplayName("GET /health - Accessible")
    @WithMockUser
    void healthEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/plannings/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Planning Service is running"));
    }

    @Nested
    @DisplayName("POST /api/plannings - Création")
    class CreatePlanning {

        @Test
        @DisplayName("Doit créer un planning → 201")
        @WithMockUser(roles = "TRAINER")
        void shouldCreatePlanning() throws Exception {
            when(planningService.createPlanning(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/plannings")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.planningId").value(1L))
                    .andExpect(jsonPath("$.title").value("Formation Spring Boot"))
                    .andExpect(jsonPath("$.status").value("DRAFT"));
        }

        @Test
        @DisplayName("Doit retourner 400 si conflit de créneau")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn400OnConflict() throws Exception {
            when(planningService.createPlanning(any()))
                    .thenThrow(new RuntimeException("Time slot conflicts with existing planning"));

            mockMvc.perform(post("/api/plannings")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Time slot conflicts with existing planning"));
        }

        @Test
        @DisplayName("Doit retourner 400 si les champs obligatoires sont manquants")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            PlanningRequest invalidRequest = new PlanningRequest(
                    null, // date manquante
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    "", // titre vide
                    null,
                    "TRAINING",
                    null,
                    null
            );

            mockMvc.perform(post("/api/plannings")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/plannings - Liste")
    class GetAllPlannings {

        @Test
        @DisplayName("Doit retourner tous les plannings → 200")
        @WithMockUser(roles = "TRAINER")
        void shouldReturnAllPlannings() throws Exception {
            when(planningService.getAllPlannings()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/plannings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].planningId").value(1L));
        }

        @Test
        @DisplayName("Doit retourner une liste vide")
        @WithMockUser(roles = "LEARNER")
        void shouldReturnEmptyList() throws Exception {
            when(planningService.getAllPlannings()).thenReturn(List.of());

            mockMvc.perform(get("/api/plannings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/plannings/published - Plannings publiés")
    class GetPublishedPlannings {

        @Test
        @DisplayName("Doit retourner les plannings publiés → 200")
        @WithMockUser(roles = "LEARNER")
        void shouldReturnPublishedPlannings() throws Exception {
            PlanningResponse published = new PlanningResponse(
                    2L, LocalDate.of(2026, 5, 20),
                    LocalTime.of(9, 0), LocalTime.of(11, 0),
                    "Session publiée", null, "TRAINING", null, "#fff", "PUBLISHED"
            );
            when(planningService.getPublishedPlannings()).thenReturn(List.of(published));

            mockMvc.perform(get("/api/plannings/published"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("PUBLISHED"));
        }
    }

    @Nested
    @DisplayName("POST /api/plannings/{id}/publish - Publication")
    class PublishPlanning {

        @Test
        @DisplayName("Doit publier un planning → 200 avec status PUBLISHED")
        @WithMockUser(roles = "TRAINER")
        void shouldPublishPlanning() throws Exception {
            PlanningResponse published = new PlanningResponse(
                    1L, LocalDate.of(2026, 5, 20),
                    LocalTime.of(9, 0), LocalTime.of(11, 0),
                    "Formation Spring Boot", null, "TRAINING", null, "#3498db", "PUBLISHED"
            );
            when(planningService.publishPlanning(1L)).thenReturn(published);

            mockMvc.perform(post("/api/plannings/1/publish").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PUBLISHED"));
        }

        @Test
        @DisplayName("Doit retourner 400 si le planning n'existe pas")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn400WhenPlanningNotFound() throws Exception {
            when(planningService.publishPlanning(99L))
                    .thenThrow(new RuntimeException("Planning not found with id: 99"));

            mockMvc.perform(post("/api/plannings/99/publish").with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Planning not found with id: 99"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/plannings/{id}")
    class DeletePlanning {

        @Test
        @DisplayName("Doit supprimer un planning → 204")
        @WithMockUser(roles = "TRAINER")
        void shouldDeletePlanning() throws Exception {
            doNothing().when(planningService).deletePlanning(1L);

            mockMvc.perform(delete("/api/plannings/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Doit retourner 400 si le planning n'existe pas")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn400WhenPlanningNotFound() throws Exception {
            doThrow(new RuntimeException("Planning not found")).when(planningService).deletePlanning(99L);

            mockMvc.perform(delete("/api/plannings/99").with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Planning not found"));
        }
    }

    @Nested
    @DisplayName("PUT /api/plannings/{id} - Mise à jour")
    class UpdatePlanning {

        @Test
        @DisplayName("Doit mettre à jour un planning → 200")
        @WithMockUser(roles = "TRAINER")
        void shouldUpdatePlanning() throws Exception {
            when(planningService.updatePlanning(eq(1L), any())).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/plannings/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.planningId").value(1L));
        }
    }
}
