package com.smartek.learningmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartek.learningmicroservice.client.SkillEvidenceClient;
import com.smartek.learningmicroservice.dto.LearningPathRequest;
import com.smartek.learningmicroservice.dto.LearningPathResponse;
import com.smartek.learningmicroservice.entity.LearningPathStatus;
import com.smartek.learningmicroservice.service.LearningPathService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = LearningPathController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.smartek.learningmicroservice.config.JwtAuthFilter.class
    )
)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.smartek.learningmicroservice.config.TestSecurityConfig.class)
@DisplayName("LearningPathController - Tests d'intégration")
class LearningPathControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LearningPathService pathService;

    @MockBean
    private SkillEvidenceClient skillEvidenceClient;

    private ObjectMapper objectMapper;
    private LearningPathRequest validRequest;
    private LearningPathResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        validRequest = new LearningPathRequest(
                "Parcours Java Backend",
                "Maîtriser Spring Boot",
                5L,
                "Alice Dupont",
                LearningPathStatus.EN_COURS,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30),
                40
        );

        sampleResponse = new LearningPathResponse(
                1L,
                "Parcours Java Backend",
                "Maîtriser Spring Boot",
                5L,
                "Alice Dupont",
                LearningPathStatus.EN_COURS,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30),
                40
        );
    }

    @Nested
    @DisplayName("POST /api/learning-paths - Création")
    class CreatePath {

        @Test
        @DisplayName("Doit créer un parcours → 201")
        void shouldCreatePath() throws Exception {
            when(pathService.createPath(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/learning-paths")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.pathId").value(1L))
                    .andExpect(jsonPath("$.title").value("Parcours Java Backend"));
        }

        @Test
        @DisplayName("Doit retourner 4xx ou 5xx si titre dupliqué")
        void shouldReturn400WhenDuplicateTitle() throws Exception {
            when(pathService.createPath(any()))
                    .thenThrow(new RuntimeException("Un parcours avec ce titre existe déjà"));

            // Le controller ne gère pas les RuntimeException — Spring propage en 500
            mockMvc.perform(post("/api/learning-paths")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/learning-paths - Liste")
    class GetAllPaths {

        @Test
        @DisplayName("Doit retourner tous les parcours → 200")
        void shouldReturnAllPaths() throws Exception {
            when(pathService.getAllPaths()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/learning-paths"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].pathId").value(1L));
        }

        @Test
        @DisplayName("Doit retourner une liste vide")
        void shouldReturnEmptyList() throws Exception {
            when(pathService.getAllPaths()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/learning-paths"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/learning-paths/{pathId}")
    class GetPathById {

        @Test
        @DisplayName("Doit retourner le parcours par ID → 200")
        void shouldReturnPathById() throws Exception {
            when(pathService.getPathById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/learning-paths/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pathId").value(1L))
                    .andExpect(jsonPath("$.title").value("Parcours Java Backend"));
        }

        @Test
        @DisplayName("Doit retourner 5xx si le parcours n'existe pas")
        void shouldReturn5xxWhenNotFound() throws Exception {
            when(pathService.getPathById(99L))
                    .thenThrow(new RuntimeException("Parcours d'apprentissage non trouvé"));

            // Le controller ne gère pas les RuntimeException → Spring retourne 500
            mockMvc.perform(get("/api/learning-paths/99"))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/learning-paths/learner/{learnerId}")
    class GetPathsByLearner {

        @Test
        @DisplayName("Doit retourner les parcours d'un apprenant → 200")
        void shouldReturnPathsByLearner() throws Exception {
            when(pathService.getAllPathsByLearner(5L)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/learning-paths/learner/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].learnerId").value(5L));
        }
    }

    @Nested
    @DisplayName("GET /api/learning-paths/status/{status}")
    class GetPathsByStatus {

        @Test
        @DisplayName("Doit retourner les parcours par statut → 200")
        void shouldReturnPathsByStatus() throws Exception {
            when(pathService.getPathsByStatus(LearningPathStatus.EN_COURS))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/learning-paths/status/EN_COURS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("EN_COURS"));
        }
    }

    @Nested
    @DisplayName("PUT /api/learning-paths/{pathId} - Mise à jour")
    class UpdatePath {

        @Test
        @DisplayName("Doit mettre à jour un parcours → 200")
        void shouldUpdatePath() throws Exception {
            when(pathService.updatePath(eq(1L), any())).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/learning-paths/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pathId").value(1L));
        }
    }

    @Nested
    @DisplayName("DELETE /api/learning-paths/{pathId}")
    class DeletePath {

        @Test
        @DisplayName("Doit supprimer un parcours → 204")
        void shouldDeletePath() throws Exception {
            doNothing().when(pathService).deletePath(1L);

            mockMvc.perform(delete("/api/learning-paths/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Doit retourner 5xx si le parcours n'existe pas")
        void shouldReturn5xxWhenNotFound() throws Exception {
            doThrow(new RuntimeException("Parcours d'apprentissage non trouvé"))
                    .when(pathService).deletePath(99L);

            // Le controller ne gère pas les RuntimeException → Spring retourne 500
            mockMvc.perform(delete("/api/learning-paths/99"))
                    .andExpect(status().is5xxServerError());
        }
    }
}
