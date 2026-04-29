package com.smartek.event.controller;

import com.smartek.event.dto.EventRequest;
import com.smartek.event.dto.EventResponse;
import com.smartek.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EventController.
 * Utilise MockitoExtension pour éviter le chargement du contexte Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventController - Tests unitaires")
class EventControllerTest {

    // ─── Mocks ───────────────────────────────────────────────────────────────

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    // ─── Données de test ─────────────────────────────────────────────────────

    private EventRequest validRequest;
    private EventResponse sampleResponse;

    @BeforeEach
    void setUp() {
        // Initialisation d'une requête d'événement valide
        validRequest = new EventRequest();
        validRequest.setTitle("Formation DevOps");
        validRequest.setDescription("Atelier pratique DevOps");
        validRequest.setLocation("Tunis");
        validRequest.setStartDate(LocalDateTime.now().plusDays(1));
        validRequest.setEndDate(LocalDateTime.now().plusDays(2));
        validRequest.setMaxParticipations(50);
        validRequest.setPhysicalCapacity(50);
        validRequest.setOnlineCapacity(0);
        validRequest.setPrice(BigDecimal.ZERO);
        validRequest.setIsPaid(false);
        validRequest.setCreatedBy(1L);

        // Initialisation d'une réponse d'événement
        sampleResponse = new EventResponse();
        sampleResponse.setEventId(1L);
        sampleResponse.setTitle("Formation DevOps");
        sampleResponse.setDescription("Atelier pratique DevOps");
        sampleResponse.setLocation("Tunis");
        sampleResponse.setStartDate(LocalDateTime.now().plusDays(1));
        sampleResponse.setEndDate(LocalDateTime.now().plusDays(2));
        sampleResponse.setMaxParticipations(50);
        sampleResponse.setCurrentParticipations(0);
        sampleResponse.setPhysicalCapacity(50);
        sampleResponse.setOnlineCapacity(0);
        sampleResponse.setPhysicalRegistered(0);
        sampleResponse.setOnlineRegistered(0);
        sampleResponse.setPrice(BigDecimal.ZERO);
        sampleResponse.setIsPaid(false);
        sampleResponse.setStatus("DRAFT");
        sampleResponse.setMode("PHYSICAL");
        sampleResponse.setCreatedBy(1L);
        sampleResponse.setIsAvailable(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/events - Création d'événement
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/events - Création d'événement")
    class CreateEvent {

        @Test
        @DisplayName("Doit créer un événement avec succès → 201 CREATED")
        void shouldCreateEventSuccessfully() {
            // Arrange
            when(eventService.createEvent(any(EventRequest.class))).thenReturn(sampleResponse);

            // Act
            ResponseEntity<EventResponse> response = eventController.createEvent(validRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Formation DevOps");
            assertThat(response.getBody().getLocation()).isEqualTo("Tunis");
            verify(eventService, times(1)).createEvent(any(EventRequest.class));
        }

        @Test
        @DisplayName("Doit déléguer la création au service et retourner la réponse du service")
        void shouldDelegateCreationToService() {
            // Arrange : vérification que le contrôleur délègue bien au service
            EventResponse customResponse = new EventResponse();
            customResponse.setEventId(42L);
            customResponse.setTitle("Conférence IA");
            when(eventService.createEvent(any(EventRequest.class))).thenReturn(customResponse);

            // Act
            ResponseEntity<EventResponse> response = eventController.createEvent(validRequest);

            // Assert
            assertThat(response.getBody().getEventId()).isEqualTo(42L);
            assertThat(response.getBody().getTitle()).isEqualTo("Conférence IA");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events/{id} - Récupération par ID
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/events/{id} - Récupération par ID")
    class GetEventById {

        @Test
        @DisplayName("Doit retourner l'événement correspondant à l'ID → 200 OK")
        void shouldReturnEventById() {
            // Arrange
            when(eventService.getEventById(1L)).thenReturn(sampleResponse);

            // Act
            ResponseEntity<EventResponse> response = eventController.getEventById(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getEventId()).isEqualTo(1L);
            assertThat(response.getBody().getTitle()).isEqualTo("Formation DevOps");
        }

        @Test
        @DisplayName("Doit propager l'exception si l'événement n'existe pas")
        void shouldPropagateExceptionWhenEventNotFound() {
            // Arrange : le service lève une exception
            when(eventService.getEventById(99L))
                    .thenThrow(new RuntimeException("Event not found with id: 99"));

            // Act & Assert : le contrôleur propage l'exception (pas de gestion d'erreur dans ce contrôleur)
            org.assertj.core.api.Assertions.assertThatThrownBy(
                    () -> eventController.getEventById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found with id: 99");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events - Liste de tous les événements
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/events - Liste des événements")
    class GetAllEvents {

        @Test
        @DisplayName("Doit retourner la liste de tous les événements → 200 OK")
        void shouldReturnAllEvents() {
            // Arrange
            when(eventService.getAllEvents()).thenReturn(List.of(sampleResponse));

            // Act
            ResponseEntity<List<EventResponse>> response = eventController.getAllEvents();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTitle()).isEqualTo("Formation DevOps");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun événement n'existe → 200 OK")
        void shouldReturnEmptyListWhenNoEvents() {
            // Arrange
            when(eventService.getAllEvents()).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<EventResponse>> response = eventController.getAllEvents();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner plusieurs événements → 200 OK")
        void shouldReturnMultipleEvents() {
            // Arrange : deux événements dans la liste
            EventResponse secondEvent = new EventResponse();
            secondEvent.setEventId(2L);
            secondEvent.setTitle("Hackathon 2025");
            when(eventService.getAllEvents()).thenReturn(List.of(sampleResponse, secondEvent));

            // Act
            ResponseEntity<List<EventResponse>> response = eventController.getAllEvents();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events/upcoming - Événements à venir
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/events/upcoming - Événements à venir")
    class GetUpcomingEvents {

        @Test
        @DisplayName("Doit retourner les événements à venir → 200 OK")
        void shouldReturnUpcomingEvents() {
            // Arrange
            when(eventService.getUpcomingEvents()).thenReturn(List.of(sampleResponse));

            // Act
            ResponseEntity<List<EventResponse>> response = eventController.getUpcomingEvents();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            verify(eventService, times(1)).getUpcomingEvents();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun événement à venir → 200 OK")
        void shouldReturnEmptyListWhenNoUpcomingEvents() {
            // Arrange
            when(eventService.getUpcomingEvents()).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<EventResponse>> response = eventController.getUpcomingEvents();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/events/{id} - Mise à jour d'événement
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/events/{id} - Mise à jour d'événement")
    class UpdateEvent {

        @Test
        @DisplayName("Doit mettre à jour un événement existant → 200 OK")
        void shouldUpdateEventSuccessfully() {
            // Arrange
            when(eventService.updateEvent(eq(1L), any(EventRequest.class))).thenReturn(sampleResponse);

            // Act
            ResponseEntity<EventResponse> response = eventController.updateEvent(1L, validRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getEventId()).isEqualTo(1L);
            verify(eventService, times(1)).updateEvent(eq(1L), any(EventRequest.class));
        }

        @Test
        @DisplayName("Doit propager l'exception si l'événement à mettre à jour n'existe pas")
        void shouldPropagateExceptionWhenEventNotFound() {
            // Arrange
            when(eventService.updateEvent(eq(99L), any(EventRequest.class)))
                    .thenThrow(new RuntimeException("Event not found with id: 99"));

            // Act & Assert
            org.assertj.core.api.Assertions.assertThatThrownBy(
                    () -> eventController.updateEvent(99L, validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/events/{id} - Suppression d'événement
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/events/{id} - Suppression d'événement")
    class DeleteEvent {

        @Test
        @DisplayName("Doit supprimer un événement existant → 204 NO CONTENT")
        void shouldDeleteEventSuccessfully() {
            // Arrange : le service ne lève pas d'exception
            doNothing().when(eventService).deleteEvent(1L);

            // Act
            ResponseEntity<Void> response = eventController.deleteEvent(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(eventService, times(1)).deleteEvent(1L);
        }

        @Test
        @DisplayName("Doit propager l'exception si l'événement à supprimer n'existe pas")
        void shouldPropagateExceptionWhenEventNotFound() {
            // Arrange
            doThrow(new RuntimeException("Event not found with id: 99"))
                    .when(eventService).deleteEvent(99L);

            // Act & Assert
            org.assertj.core.api.Assertions.assertThatThrownBy(
                    () -> eventController.deleteEvent(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/events/{id}/register - Inscription à un événement
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/events/{id}/register - Inscription à un événement")
    class RegisterParticipation {

        @Test
        @DisplayName("Doit inscrire un participant à l'événement → 200 OK")
        void shouldRegisterParticipationSuccessfully() {
            // Arrange : l'événement a de la place disponible
            sampleResponse.setCurrentParticipations(1);
            when(eventService.registerParticipation(1L)).thenReturn(sampleResponse);

            // Act
            ResponseEntity<EventResponse> response = eventController.registerParticipation(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(eventService, times(1)).registerParticipation(1L);
        }

        @Test
        @DisplayName("Doit propager l'exception si l'événement est complet")
        void shouldPropagateExceptionWhenEventIsFull() {
            // Arrange : l'événement est complet
            when(eventService.registerParticipation(1L))
                    .thenThrow(new RuntimeException("Event is full"));

            // Act & Assert
            org.assertj.core.api.Assertions.assertThatThrownBy(
                    () -> eventController.registerParticipation(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event is full");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/events/{id}/cancel - Annulation de participation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/events/{id}/cancel - Annulation de participation")
    class CancelParticipation {

        @Test
        @DisplayName("Doit annuler la participation à l'événement → 200 OK")
        void shouldCancelParticipationSuccessfully() {
            // Arrange
            sampleResponse.setCurrentParticipations(4);
            when(eventService.cancelParticipation(1L)).thenReturn(sampleResponse);

            // Act
            ResponseEntity<EventResponse> response = eventController.cancelParticipation(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(eventService, times(1)).cancelParticipation(1L);
        }

        @Test
        @DisplayName("Doit propager l'exception si aucune participation à annuler")
        void shouldPropagateExceptionWhenNoParticipationsToCancel() {
            // Arrange
            when(eventService.cancelParticipation(1L))
                    .thenThrow(new RuntimeException("No participations to cancel"));

            // Act & Assert
            org.assertj.core.api.Assertions.assertThatThrownBy(
                    () -> eventController.cancelParticipation(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No participations to cancel");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events/health - Vérification de santé
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/events/health - Doit retourner 200 OK avec message de santé")
    void healthEndpointShouldReturnOk() {
        // Act
        ResponseEntity<String> response = eventController.health();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Event Service is running");
    }
}
