package com.smartek.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartek.event.config.TestSecurityConfig;
import com.smartek.event.dto.EventRequest;
import com.smartek.event.dto.EventResponse;
import com.smartek.event.service.EventService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("EventController - Tests d'intégration")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    private ObjectMapper objectMapper;
    private EventRequest validRequest;
    private EventResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        validRequest = new EventRequest();
        validRequest.setTitle("Formation DevOps");
        validRequest.setDescription("Sprint 3 DevOps");
        validRequest.setLocation("Tunis");
        validRequest.setStartDate(LocalDateTime.now().plusDays(1));
        validRequest.setEndDate(LocalDateTime.now().plusDays(2));
        validRequest.setMaxParticipations(50);
        validRequest.setPhysicalCapacity(50);
        validRequest.setOnlineCapacity(0);
        validRequest.setPrice(BigDecimal.ZERO);
        validRequest.setIsPaid(false);
        validRequest.setCreatedBy(1L);

        sampleResponse = new EventResponse();
        sampleResponse.setEventId(1L);
        sampleResponse.setTitle("Formation DevOps");
        sampleResponse.setLocation("Tunis");
        sampleResponse.setStatus("DRAFT");
        sampleResponse.setMaxParticipations(50);
        sampleResponse.setCurrentParticipations(0);
        sampleResponse.setPrice(BigDecimal.ZERO);
        sampleResponse.setIsPaid(false);
    }

    @Test
    @DisplayName("GET /health → 200")
    void healthEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/events/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Event Service is running"));
    }

    @Nested
    @DisplayName("POST /api/events - Création")
    class CreateEvent {

        @Test
        @DisplayName("Doit créer un événement → 201")
        void shouldCreateEvent() throws Exception {
            when(eventService.createEvent(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.eventId").value(1L))
                    .andExpect(jsonPath("$.title").value("Formation DevOps"));
        }

        @Test
        @DisplayName("Doit retourner 400 si titre manquant")
        void shouldReturn400WhenTitleMissing() throws Exception {
            validRequest.setTitle("");

            mockMvc.perform(post("/api/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Doit retourner 400 si date de fin avant date de début")
        void shouldReturn400WhenEndDateBeforeStartDate() throws Exception {
            when(eventService.createEvent(any()))
                    .thenThrow(new RuntimeException("End date must be after start date"));

            mockMvc.perform(post("/api/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/events - Liste")
    class GetAllEvents {

        @Test
        @DisplayName("Doit retourner tous les événements → 200")
        void shouldReturnAllEvents() throws Exception {
            when(eventService.getAllEvents()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].eventId").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Formation DevOps"));
        }

        @Test
        @DisplayName("Doit retourner une liste vide")
        void shouldReturnEmptyList() throws Exception {
            when(eventService.getAllEvents()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/events/{id}")
    class GetEventById {

        @Test
        @DisplayName("Doit retourner l'événement par ID → 200")
        void shouldReturnEventById() throws Exception {
            when(eventService.getEventById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/events/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eventId").value(1L));
        }

        @Test
        @DisplayName("Doit retourner 400 si l'événement n'existe pas")
        void shouldReturn400WhenNotFound() throws Exception {
            when(eventService.getEventById(99L))
                    .thenThrow(new RuntimeException("Event not found with id: 99"));

            mockMvc.perform(get("/api/events/99"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/events/upcoming")
    class GetUpcomingEvents {

        @Test
        @DisplayName("Doit retourner les événements à venir → 200")
        void shouldReturnUpcomingEvents() throws Exception {
            when(eventService.getUpcomingEvents()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/events/upcoming"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Formation DevOps"));
        }
    }

    @Nested
    @DisplayName("PUT /api/events/{id} - Mise à jour")
    class UpdateEvent {

        @Test
        @DisplayName("Doit mettre à jour un événement → 200")
        void shouldUpdateEvent() throws Exception {
            when(eventService.updateEvent(eq(1L), any())).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/events/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eventId").value(1L));
        }
    }

    @Nested
    @DisplayName("DELETE /api/events/{id}")
    class DeleteEvent {

        @Test
        @DisplayName("Doit supprimer un événement → 204")
        void shouldDeleteEvent() throws Exception {
            doNothing().when(eventService).deleteEvent(1L);

            mockMvc.perform(delete("/api/events/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Doit retourner 400 si l'événement n'existe pas")
        void shouldReturn400WhenNotFound() throws Exception {
            doThrow(new RuntimeException("Event not found with id: 99"))
                    .when(eventService).deleteEvent(99L);

            mockMvc.perform(delete("/api/events/99"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/events/{id}/register - Inscription")
    class RegisterParticipation {

        @Test
        @DisplayName("Doit inscrire un participant → 200")
        void shouldRegisterParticipation() throws Exception {
            when(eventService.registerParticipation(1L)).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/events/1/register"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Doit retourner 400 si l'événement est complet")
        void shouldReturn400WhenEventFull() throws Exception {
            when(eventService.registerParticipation(1L))
                    .thenThrow(new RuntimeException("Event is full"));

            mockMvc.perform(post("/api/events/1/register"))
                    .andExpect(status().isBadRequest());
        }
    }
}
