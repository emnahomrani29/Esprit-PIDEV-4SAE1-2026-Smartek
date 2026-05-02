package com.smartek.courseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartek.courseservice.dto.CourseRequest;
import com.smartek.courseservice.dto.CourseResponse;
import com.smartek.courseservice.entity.DeliveryMode;
import com.smartek.courseservice.exception.DuplicateResourceException;
import com.smartek.courseservice.exception.ResourceNotFoundException;
import com.smartek.courseservice.security.JwtAuthenticationFilter;
import com.smartek.courseservice.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour CourseController.
 *
 * Stratégie de sécurité :
 * - @WebMvcTest charge la config Spring Security auto (pas de SecurityConfig custom)
 * - JwtAuthenticationFilter est mocké pour éviter l'injection de JwtService
 * - @WithMockUser simule les rôles directement dans le SecurityContext
 * - Les tests de rôles vérifient que le contrôleur répond correctement selon le rôle
 */
@WebMvcTest(
    value = CourseController.class,
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = com.smartek.courseservice.security.JwtAuthenticationFilter.class
    )
)
@DisplayName("CourseController - Tests d'intégration")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    private ObjectMapper objectMapper;
    private CourseRequest validRequest;
    private CourseResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        validRequest = CourseRequest.builder()
                .title("Spring Boot Avancé")
                .content("Contenu du cours")
                .duration(LocalDate.of(2025, 12, 31))
                .trainerId(10L)
                .deliveryMode(DeliveryMode.PRESENTIEL)
                .build();

        sampleResponse = CourseResponse.builder()
                .courseId(1L)
                .title("Spring Boot Avancé")
                .trainerId(10L)
                .deliveryMode(DeliveryMode.PRESENTIEL)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/courses/health
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /health - Accessible sans authentification")
    @WithMockUser
    void healthEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/courses/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Course Service is running"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/courses
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/courses - Création de cours")
    class CreateCourse {

        @Test
        @DisplayName("Doit créer un cours avec succès → 201")
        @WithMockUser(roles = "TRAINER")
        void shouldCreateCourseSuccessfully() throws Exception {
            when(courseService.createCourse(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/courses")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.courseId").value(1L))
                    .andExpect(jsonPath("$.title").value("Spring Boot Avancé"));
        }

        @Test
        @DisplayName("Doit retourner 400 si le titre est déjà utilisé")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn400WhenTitleAlreadyExists() throws Exception {
            when(courseService.createCourse(any()))
                    .thenThrow(new DuplicateResourceException("Cours", "titre", "Spring Boot Avancé"));

            mockMvc.perform(post("/api/courses")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Doit retourner 400 si les champs obligatoires sont manquants")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            CourseRequest invalidRequest = CourseRequest.builder()
                    .title("") // titre vide → @NotBlank échoue
                    .trainerId(10L)
                    .duration(LocalDate.of(2025, 12, 31))
                    .deliveryMode(DeliveryMode.PRESENTIEL)
                    .build();

            mockMvc.perform(post("/api/courses")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/courses
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses - Liste des cours")
    class GetAllCourses {

        @Test
        @DisplayName("Doit retourner la liste des cours → 200")
        @WithMockUser(roles = "TRAINER")
        void shouldReturnAllCourses() throws Exception {
            when(courseService.getAllCourses()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].courseId").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Spring Boot Avancé"));
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun cours")
        @WithMockUser(roles = "LEARNER")
        void shouldReturnEmptyList() throws Exception {
            when(courseService.getAllCourses()).thenReturn(List.of());

            mockMvc.perform(get("/api/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/courses/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/{id}")
    class GetCourseById {

        @Test
        @DisplayName("Doit retourner le cours par ID → 200")
        @WithMockUser(roles = "TRAINER")
        void shouldReturnCourseById() throws Exception {
            when(courseService.getCourseById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/courses/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.courseId").value(1L))
                    .andExpect(jsonPath("$.title").value("Spring Boot Avancé"));
        }

        @Test
        @DisplayName("Doit retourner 404 si le cours n'existe pas")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn404WhenCourseNotFound() throws Exception {
            when(courseService.getCourseById(99L))
                    .thenThrow(new ResourceNotFoundException("Cours", "id", 99L));

            mockMvc.perform(get("/api/courses/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/courses/trainer/{trainerId}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/trainer/{trainerId}")
    class GetCoursesByTrainer {

        @Test
        @DisplayName("Doit retourner les cours d'un trainer → 200")
        @WithMockUser(roles = "TRAINER")
        void shouldReturnCoursesByTrainer() throws Exception {
            when(courseService.getCoursesByTrainer(10L)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/courses/trainer/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].trainerId").value(10L));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/courses/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/courses/{id} - Mise à jour")
    class UpdateCourse {

        @Test
        @DisplayName("Doit mettre à jour un cours → 200")
        @WithMockUser(roles = "TRAINER")
        void shouldUpdateCourse() throws Exception {
            when(courseService.updateCourse(eq(1L), any())).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/courses/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.courseId").value(1L));
        }

        @Test
        @DisplayName("Doit retourner 400 si le cours à mettre à jour n'existe pas")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn400WhenCourseNotFound() throws Exception {
            when(courseService.updateCourse(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Cours", "id", 99L));

            mockMvc.perform(put("/api/courses/99")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/courses/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/courses/{id}")
    class DeleteCourse {

        @Test
        @DisplayName("Doit supprimer un cours → 204")
        @WithMockUser(roles = "TRAINER")
        void shouldDeleteCourse() throws Exception {
            doNothing().when(courseService).deleteCourse(1L);

            mockMvc.perform(delete("/api/courses/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Doit retourner 204 même si le cours n'existe pas (DELETE idempotent)")
        @WithMockUser(roles = "TRAINER")
        void shouldReturn404WhenCourseNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Cours", "id", 99L))
                    .when(courseService).deleteCourse(99L);

            mockMvc.perform(delete("/api/courses/99").with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }
}
