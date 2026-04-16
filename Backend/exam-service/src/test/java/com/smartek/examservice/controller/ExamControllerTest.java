package com.smartek.examservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartek.examservice.config.TestSecurityConfig;
import com.smartek.examservice.dto.ExamRequest;
import com.smartek.examservice.dto.ExamResponse;
import com.smartek.examservice.security.JwtAuthenticationFilter;
import com.smartek.examservice.security.SecurityConfig;
import com.smartek.examservice.service.ExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour ExamController.
 * Vérifie les règles de sécurité (rôles TRAINER/LEARNER) et les réponses HTTP.
 *
 * SecurityConfig et JwtAuthenticationFilter sont exclus du contexte de test ;
 * TestSecurityConfig les remplace avec une configuration simplifiée.
 */
@WebMvcTest(
        controllers = ExamController.class,
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ExamController - Tests d'intégration")
class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExamService examService;

    private ObjectMapper objectMapper;
    private ExamRequest validRequest;
    private ExamResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        validRequest = new ExamRequest();
        validRequest.setCourseId(5L);
        validRequest.setExamType("QUIZ");
        validRequest.setTitle("Quiz Spring Boot");
        validRequest.setDuration(60);
        validRequest.setPassingScore(70);
        validRequest.setTotalMarks(100);
        validRequest.setIsActive(true);
        validRequest.setCreatedBy(10L);

        sampleResponse = new ExamResponse();
        sampleResponse.setId(1L);
        sampleResponse.setCourseId(5L);
        sampleResponse.setExamType("QUIZ");
        sampleResponse.setTitle("Quiz Spring Boot");
        sampleResponse.setDuration(60);
        sampleResponse.setPassingScore(70);
        sampleResponse.setTotalMarks(100);
        sampleResponse.setIsActive(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/exams/health
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /health - Accessible sans authentification")
    void healthEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/exams/health"))
                .andExpect(status().isOk());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/exams
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/exams - Création d'examen")
    class CreateExam {

        @Test
        @DisplayName("Doit créer un examen avec le rôle TRAINER → 201")
        @WithMockUser(roles = "TRAINER")
        void shouldCreateExamAsTrainer() throws Exception {
            when(examService.createExam(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/exams")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Quiz Spring Boot"))
                    .andExpect(jsonPath("$.examType").value("QUIZ"));
        }

        @Test
        @DisplayName("Doit refuser la création avec le rôle LEARNER → 403")
        @WithMockUser(roles = "LEARNER")
        void shouldForbidExamCreationAsLearner() throws Exception {
            mockMvc.perform(post("/api/exams")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Doit refuser la création sans authentification → 401")
        void shouldRejectUnauthenticatedCreation() throws Exception {
            mockMvc.perform(post("/api/exams")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/exams
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exams - Liste des examens")
    class GetAllExams {

        @Test
        @DisplayName("Doit retourner la liste pour un TRAINER → 200")
        @WithMockUser(roles = "TRAINER")
        void shouldReturnExamsForTrainer() throws Exception {
            when(examService.getAllExams()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/exams"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        @DisplayName("Doit retourner la liste pour un LEARNER → 200")
        @WithMockUser(roles = "LEARNER")
        void shouldReturnExamsForLearner() throws Exception {
            when(examService.getAllExams()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/exams"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Doit refuser l'accès sans authentification → 401")
        void shouldRejectUnauthenticatedAccess() throws Exception {
            mockMvc.perform(get("/api/exams"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/exams/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/exams/{id}")
    class GetExamById {

        @Test
        @DisplayName("Doit retourner l'examen par ID → 200")
        @WithMockUser(roles = "TRAINER")
        void shouldReturnExamByIdForTrainer() throws Exception {
            when(examService.getExamById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/exams/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("Doit retourner 500 si l'examen n'existe pas (RuntimeException non gérée)")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn500WhenExamNotFound() throws Exception {
            when(examService.getExamById(99L)).thenThrow(new RuntimeException("Exam not found with id: 99"));

            mockMvc.perform(get("/api/exams/99"))
                    .andExpect(status().is5xxServerError());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/exams/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/exams/{id}")
    class DeleteExam {

        @Test
        @DisplayName("Doit supprimer un examen avec le rôle TRAINER → 204")
        @WithMockUser(roles = "TRAINER")
        void shouldDeleteExamAsTrainer() throws Exception {
            doNothing().when(examService).deleteExam(1L);

            mockMvc.perform(delete("/api/exams/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Doit refuser la suppression avec le rôle LEARNER → 403")
        @WithMockUser(roles = "LEARNER")
        void shouldForbidDeletionAsLearner() throws Exception {
            mockMvc.perform(delete("/api/exams/1").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/exams/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/exams/{id} - Mise à jour")
    class UpdateExam {

        @Test
        @DisplayName("Doit mettre à jour un examen avec le rôle TRAINER → 200")
        @WithMockUser(roles = "TRAINER")
        void shouldUpdateExamAsTrainer() throws Exception {
            when(examService.updateExam(eq(1L), any())).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/exams/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("Doit refuser la mise à jour avec le rôle LEARNER → 403")
        @WithMockUser(roles = "LEARNER")
        void shouldForbidUpdateAsLearner() throws Exception {
            mockMvc.perform(put("/api/exams/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isForbidden());
        }
    }
}
