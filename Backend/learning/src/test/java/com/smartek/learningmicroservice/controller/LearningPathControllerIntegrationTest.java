package com.smartek.learningmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.learningmicroservice.client.SkillEvidenceClient;
import com.smartek.learningmicroservice.config.SecurityConfig;
import com.smartek.learningmicroservice.dto.LearningPathRequest;
import com.smartek.learningmicroservice.dto.LearningPathResponse;
import com.smartek.learningmicroservice.entity.LearningPathStatus;
import com.smartek.learningmicroservice.service.LearningPathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = LearningPathController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    )
)
class LearningPathControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LearningPathService pathService;

    @MockBean
    private SkillEvidenceClient skillEvidenceClient;

    private LearningPathResponse response;
    private LearningPathRequest request;

    @BeforeEach
    void setUp() {
        response = new LearningPathResponse(
                1L, "Spring Boot Mastery", "Learn Spring Boot",
                5L, "Bob Martin", LearningPathStatus.EN_COURS,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 40
        );

        request = new LearningPathRequest(
                "Spring Boot Mastery", "Learn Spring Boot",
                5L, "Bob Martin", LearningPathStatus.EN_COURS,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 40
        );
    }

    @Test
    void POST_createPath_returns201() throws Exception {
        when(pathService.createPath(any())).thenReturn(response);

        mockMvc.perform(post("/api/learning-paths")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Spring Boot Mastery"))
                .andExpect(jsonPath("$.learnerId").value(5))
                .andExpect(jsonPath("$.progress").value(40));
    }

    @Test
    void GET_getAllPaths_returns200() throws Exception {
        when(pathService.getAllPaths()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/learning-paths"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Spring Boot Mastery"));
    }

    @Test
    void GET_getPathsByLearner_returns200() throws Exception {
        when(pathService.getAllPathsByLearner(5L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/learning-paths/learner/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void GET_getPathById_returns200() throws Exception {
        when(pathService.getPathById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/learning-paths/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pathId").value(1));
    }

    @Test
    void GET_getPathById_notFound_throwsException() throws Exception {
        when(pathService.getPathById(99L)).thenThrow(new RuntimeException("Parcours d'apprentissage non trouvé"));

        org.junit.jupiter.api.Assertions.assertThrows(
            jakarta.servlet.ServletException.class,
            () -> mockMvc.perform(get("/api/learning-paths/99"))
        );
    }

    @Test
    void GET_getPathsByStatus_returns200() throws Exception {
        when(pathService.getPathsByStatus(LearningPathStatus.EN_COURS)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/learning-paths/status/EN_COURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("EN_COURS"));
    }

    @Test
    void PUT_updatePath_returns200() throws Exception {
        when(pathService.updatePath(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/learning-paths/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Boot Mastery"));
    }

    @Test
    void DELETE_deletePath_returns204() throws Exception {
        doNothing().when(pathService).deletePath(1L);

        mockMvc.perform(delete("/api/learning-paths/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void POST_createPath_missingTitle_returns400() throws Exception {
        LearningPathRequest invalid = new LearningPathRequest(
                "", "desc", 5L, "Bob",
                LearningPathStatus.EN_COURS, LocalDate.now(), null, 0
        );

        mockMvc.perform(post("/api/learning-paths")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
