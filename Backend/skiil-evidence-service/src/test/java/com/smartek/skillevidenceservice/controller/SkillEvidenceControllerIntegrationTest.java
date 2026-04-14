package com.smartek.skillevidenceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartek.skillevidenceservice.config.SecurityConfig;
import com.smartek.skillevidenceservice.dto.SkillEvidenceRequest;
import com.smartek.skillevidenceservice.dto.SkillEvidenceResponse;
import com.smartek.skillevidenceservice.entity.EvidenceCategory;
import com.smartek.skillevidenceservice.entity.EvidenceStatus;
import com.smartek.skillevidenceservice.service.SkillEvidenceService;
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
    controllers = SkillEvidenceController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    )
)
class SkillEvidenceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SkillEvidenceService service;

    private SkillEvidenceResponse response;
    private SkillEvidenceRequest request;

    @BeforeEach
    void setUp() {
        response = new SkillEvidenceResponse(
                1, "Java Certification", "http://example.com/cert.pdf",
                "Oracle Java SE 17", LocalDate.now(),
                10L, "Alice Dupont", "alice@example.com",
                EvidenceStatus.PENDING, null, null, null, null,
                EvidenceCategory.PROGRAMMING
        );

        request = new SkillEvidenceRequest(
                "Java Certification", "http://example.com/cert.pdf",
                "Oracle Java SE 17", 10L, "Alice Dupont",
                "alice@example.com", EvidenceCategory.PROGRAMMING
        );
    }

    @Test
    void POST_createEvidence_returns201() throws Exception {
        when(service.createEvidence(any())).thenReturn(response);

        mockMvc.perform(post("/api/skill-evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Certification"))
                .andExpect(jsonPath("$.learnerId").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void GET_getAllEvidence_returns200() throws Exception {
        when(service.getAllEvidence()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/skill-evidence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Certification"));
    }

    @Test
    void GET_getByLearner_returns200() throws Exception {
        when(service.getAllEvidenceByLearner(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/skill-evidence/learner/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void GET_getById_returns200() throws Exception {
        when(service.getEvidenceById(1)).thenReturn(response);

        mockMvc.perform(get("/api/skill-evidence/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidenceId").value(1));
    }

    @Test
    void GET_getById_notFound_returns4xx() throws Exception {
        when(service.getEvidenceById(99)).thenThrow(new RuntimeException("Preuve de compétence non trouvée"));

        mockMvc.perform(get("/api/skill-evidence/99"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void PUT_updateEvidence_returns200() throws Exception {
        when(service.updateEvidence(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/skill-evidence/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Certification"));
    }

    @Test
    void DELETE_deleteEvidence_returns204() throws Exception {
        doNothing().when(service).deleteEvidence(1);

        mockMvc.perform(delete("/api/skill-evidence/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void POST_createEvidence_missingTitle_returns400() throws Exception {
        SkillEvidenceRequest invalid = new SkillEvidenceRequest(
                "", "http://example.com/cert.pdf", "desc",
                10L, "Alice", "alice@example.com", null
        );

        mockMvc.perform(post("/api/skill-evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
