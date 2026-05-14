package com.smartek.trainingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartek.trainingservice.config.TestSecurityConfig;
import com.smartek.trainingservice.dto.TrainingRequest;
import com.smartek.trainingservice.dto.TrainingResponse;
import com.smartek.trainingservice.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour TrainingController.
 * Le training-service n'a pas de SecurityConfig custom → pas besoin de @WithMockUser.
 */
@WebMvcTest(TrainingController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("TrainingController - Tests d'intégration")
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingService trainingService;

    private ObjectMapper objectMapper;
    private TrainingRequest validRequest;
    private TrainingResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        validRequest = TrainingRequest.builder()
                .title("Formation Spring Boot")
                .description("Microservices avec Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(List.of(1L, 2L))
                .createdBy(10L)
                .build();

        sampleResponse = TrainingResponse.builder()
                .trainingId(1L)
                .title("Formation Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(List.of(1L, 2L))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/health
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /health - Doit retourner 200")
    void healthEndpointShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/trainings/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Training Service is running"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/trainings
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/trainings - Création")
    class CreateTraining {

        @Test
        @DisplayName("Doit créer une formation → 201")
        void shouldCreateTrainingAndReturn201() throws Exception {
            when(trainingService.createTraining(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.trainingId").value(1L))
                    .andExpect(jsonPath("$.title").value("Formation Spring Boot"))
                    .andExpect(jsonPath("$.category").value("Backend"));
        }

        @Test
        @DisplayName("Doit retourner 400 si le titre existe déjà")
        void shouldReturn400WhenTitleAlreadyExists() throws Exception {
            when(trainingService.createTraining(any()))
                    .thenThrow(new RuntimeException("Une formation avec ce titre existe déjà"));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Une formation avec ce titre existe déjà"));
        }

        @Test
        @DisplayName("Doit retourner 400 si les champs obligatoires sont manquants")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            TrainingRequest invalidRequest = TrainingRequest.builder()
                    .title("") // titre vide → @NotBlank échoue
                    .category("Backend")
                    .level("Débutant")
                    .duration(LocalDate.now().plusMonths(1))
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings - Liste")
    class GetAllTrainings {

        @Test
        @DisplayName("Doit retourner la liste des formations → 200")
        void shouldReturnAllTrainings() throws Exception {
            when(trainingService.getAllTrainings()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/trainings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].trainingId").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Formation Spring Boot"));
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune formation")
        void shouldReturnEmptyList() throws Exception {
            when(trainingService.getAllTrainings()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/trainings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/{id}")
    class GetTrainingById {

        @Test
        @DisplayName("Doit retourner la formation par ID → 200")
        void shouldReturnTrainingById() throws Exception {
            when(trainingService.getTrainingById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/trainings/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trainingId").value(1L))
                    .andExpect(jsonPath("$.title").value("Formation Spring Boot"));
        }

        @Test
        @DisplayName("Doit retourner 404 si la formation n'existe pas")
        void shouldReturn404WhenNotFound() throws Exception {
            when(trainingService.getTrainingById(99L))
                    .thenThrow(new RuntimeException("Formation non trouvée avec l'ID: 99"));

            mockMvc.perform(get("/api/trainings/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Formation non trouvée avec l'ID: 99"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/trainings/category/{category}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/trainings/category/{category}")
    class GetByCategory {

        @Test
        @DisplayName("Doit retourner les formations par catégorie → 200")
        void shouldReturnTrainingsByCategory() throws Exception {
            when(trainingService.getTrainingsByCategory("Backend")).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/trainings/category/Backend"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].category").value("Backend"));
        }

        @Test
        @DisplayName("Doit retourner une liste vide pour une catégorie inconnue")
        void shouldReturnEmptyListForUnknownCategory() throws Exception {
            when(trainingService.getTrainingsByCategory("Unknown")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/trainings/category/Unknown"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/trainings/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/trainings/{id} - Mise à jour")
    class UpdateTraining {

        @Test
        @DisplayName("Doit mettre à jour une formation → 200")
        void shouldUpdateTraining() throws Exception {
            when(trainingService.updateTraining(eq(1L), any())).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/trainings/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trainingId").value(1L));
        }

        @Test
        @DisplayName("Doit retourner 400 si la formation n'existe pas")
        void shouldReturn400WhenNotFound() throws Exception {
            when(trainingService.updateTraining(eq(99L), any()))
                    .thenThrow(new RuntimeException("Formation non trouvée avec l'ID: 99"));

            mockMvc.perform(put("/api/trainings/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/trainings/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/trainings/{id}")
    class DeleteTraining {

        @Test
        @DisplayName("Doit supprimer une formation → 204")
        void shouldDeleteTraining() throws Exception {
            // deleteTraining is void — no stub needed, Mockito does nothing by default
            mockMvc.perform(delete("/api/trainings/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Doit retourner 500 si la formation n'existe pas")
        void shouldReturn500WhenNotFound() throws Exception {
            doThrow(new RuntimeException("Formation non trouvée"))
                    .when(trainingService).deleteTraining(99L);

            mockMvc.perform(delete("/api/trainings/99"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/trainings/{trainingId}/courses/{courseId}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/trainings/{trainingId}/courses/{courseId} - Ajout de cours")
    class AddCourseToTraining {

        @Test
        @DisplayName("Doit ajouter un cours à une formation → 200")
        void shouldAddCourseToTraining() throws Exception {
            when(trainingService.addCourseToTraining(1L, 5L)).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/trainings/1/courses/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trainingId").value(1L));
        }

        @Test
        @DisplayName("Doit retourner 400 si la formation n'existe pas")
        void shouldReturn400WhenTrainingNotFound() throws Exception {
            when(trainingService.addCourseToTraining(99L, 5L))
                    .thenThrow(new RuntimeException("Formation non trouvée"));

            mockMvc.perform(post("/api/trainings/99/courses/5"))
                    .andExpect(status().isBadRequest());
        }
    }
}
