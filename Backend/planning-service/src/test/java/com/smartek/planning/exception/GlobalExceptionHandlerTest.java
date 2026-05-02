package com.smartek.planning.exception;

import com.smartek.planning.controller.PlanningController;
import com.smartek.planning.service.PlanningService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = PlanningController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.smartek.planning.security.JwtAuthenticationFilter.class
    )
)
@DisplayName("GlobalExceptionHandler (planning-service) - Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanningService planningService;

    @Nested
    @DisplayName("RuntimeException → 400 Bad Request")
    class RuntimeExceptions {

        @Test
        @DisplayName("Conflit de créneau → 400 avec message d'erreur")
        @WithMockUser
        void timeConflict_returns400WithMessage() throws Exception {
            when(planningService.createPlanning(any()))
                    .thenThrow(new RuntimeException("Time slot conflicts with existing planning"));

            String json = """
                    {
                        "date": "2026-05-20",
                        "startTime": "09:00:00",
                        "endTime": "11:00:00",
                        "title": "Formation",
                        "eventType": "TRAINING",
                        "color": "#fff"
                    }
                    """;

            mockMvc.perform(post("/api/plannings")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Time slot conflicts with existing planning"));
        }

        @Test
        @DisplayName("Planning non trouvé → 400")
        @WithMockUser
        void planningNotFound_returns400() throws Exception {
            when(planningService.getPlanningById(99L))
                    .thenThrow(new RuntimeException("Planning not found with id: 99"));

            mockMvc.perform(get("/api/plannings/99"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Validation (@Valid) → 400 avec erreurs de champs")
    class ValidationErrors {

        @Test
        @DisplayName("Corps vide → 400")
        @WithMockUser
        void emptyBody_returns400() throws Exception {
            mockMvc.perform(post("/api/plannings")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
