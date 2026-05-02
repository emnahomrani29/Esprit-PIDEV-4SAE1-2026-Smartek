package com.smartek.event.exception;

import com.smartek.event.controller.EventController;
import com.smartek.event.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@ActiveProfiles("test")
@DisplayName("GlobalExceptionHandler (event-service) - Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Nested
    @DisplayName("RuntimeException → 400 Bad Request")
    class RuntimeExceptions {

        @Test
        @DisplayName("RuntimeException retourne 400 avec message")
        void runtimeException_returns400() throws Exception {
            when(eventService.getEventById(99L))
                    .thenThrow(new RuntimeException("Event not found with id: 99"));

            mockMvc.perform(get("/api/events/99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Event not found with id: 99"))
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("RuntimeException lors de la création retourne 400")
        void createEvent_runtimeException_returns400() throws Exception {
            when(eventService.createEvent(any()))
                    .thenThrow(new RuntimeException("End date must be after start date"));

            String json = """
                    {
                        "title": "Test",
                        "location": "Tunis",
                        "startDate": "2026-06-01T10:00:00",
                        "endDate": "2026-06-02T10:00:00",
                        "maxParticipations": 50,
                        "createdBy": 1
                    }
                    """;

            mockMvc.perform(post("/api/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Validation (@Valid) → 400 avec détails des champs")
    class ValidationErrors {

        @Test
        @DisplayName("Titre manquant → 400 avec erreurs de validation")
        void missingTitle_returns400WithFieldErrors() throws Exception {
            String invalidJson = """
                    {
                        "location": "Tunis",
                        "startDate": "2026-06-01T10:00:00",
                        "endDate": "2026-06-02T10:00:00",
                        "maxParticipations": 50,
                        "createdBy": 1
                    }
                    """;

            mockMvc.perform(post("/api/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").exists());
        }
    }
}
