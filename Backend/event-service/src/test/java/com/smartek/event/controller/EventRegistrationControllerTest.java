package com.smartek.event.controller;

import com.smartek.event.dto.EventAnalyticsResponse;
import com.smartek.event.dto.EventRegistrationRequest;
import com.smartek.event.dto.EventRegistrationResponse;
import com.smartek.event.dto.EventRevenueResponse;
import com.smartek.event.dto.EventStatusChangeRequest;
import com.smartek.event.model.EventMode;
import com.smartek.event.model.EventRegistration;
import com.smartek.event.model.PaymentStatus;
import com.smartek.event.model.RegistrationStatus;
import com.smartek.event.service.EventAnalyticsService;
import com.smartek.event.service.EventBusinessService;
import com.smartek.event.service.EventRegistrationService;
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
 * Tests unitaires pour EventBusinessController (gestion des inscriptions aux événements).
 * Utilise MockitoExtension pour éviter le chargement du contexte Spring.
 * Ce contrôleur gère les inscriptions, les statuts et les analytics des événements.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventBusinessController (EventRegistration) - Tests unitaires")
class EventRegistrationControllerTest {

    // ─── Mocks ───────────────────────────────────────────────────────────────

    @Mock
    private EventBusinessService eventBusinessService;

    @Mock
    private EventRegistrationService registrationService;

    @Mock
    private EventAnalyticsService analyticsService;

    @InjectMocks
    private EventBusinessController eventBusinessController;

    // ─── Données de test ─────────────────────────────────────────────────────

    private EventRegistrationRequest registrationRequest;
    private EventRegistrationResponse registrationResponse;
    private EventRegistration eventRegistration;

    @BeforeEach
    void setUp() {
        // Initialisation d'une requête d'inscription
        registrationRequest = new EventRegistrationRequest();
        registrationRequest.setEventId(1L);
        registrationRequest.setUserId(10L);
        registrationRequest.setParticipationMode(EventMode.PHYSICAL);

        // Initialisation d'une réponse d'inscription
        registrationResponse = new EventRegistrationResponse();
        registrationResponse.setRegistrationId(100L);
        registrationResponse.setEventId(1L);
        registrationResponse.setUserId(10L);
        registrationResponse.setStatus(RegistrationStatus.CONFIRMED);
        registrationResponse.setPaymentStatus(PaymentStatus.PENDING);
        registrationResponse.setParticipationMode(EventMode.PHYSICAL);
        registrationResponse.setRegisteredAt(LocalDateTime.now());
        registrationResponse.setMessage("Inscription confirmée");

        // Initialisation d'une entité EventRegistration
        eventRegistration = new EventRegistration();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/events/business/register - Inscription à un événement
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/events/business/register - Inscription à un événement")
    class RegisterForEvent {

        @Test
        @DisplayName("Doit inscrire un utilisateur à un événement avec succès → 200 OK")
        void shouldRegisterForEventSuccessfully() {
            // Arrange
            when(registrationService.register(any(EventRegistrationRequest.class)))
                    .thenReturn(registrationResponse);

            // Act
            ResponseEntity<EventRegistrationResponse> response =
                    eventBusinessController.registerForEvent(registrationRequest);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getRegistrationId()).isEqualTo(100L);
            assertThat(response.getBody().getEventId()).isEqualTo(1L);
            assertThat(response.getBody().getUserId()).isEqualTo(10L);
            assertThat(response.getBody().getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
            verify(registrationService, times(1)).register(any(EventRegistrationRequest.class));
        }

        @Test
        @DisplayName("Doit retourner le mode de participation correct dans la réponse")
        void shouldReturnCorrectParticipationMode() {
            // Arrange : inscription en mode en ligne
            registrationRequest.setParticipationMode(EventMode.ONLINE);
            registrationResponse.setParticipationMode(EventMode.ONLINE);
            when(registrationService.register(any(EventRegistrationRequest.class)))
                    .thenReturn(registrationResponse);

            // Act
            ResponseEntity<EventRegistrationResponse> response =
                    eventBusinessController.registerForEvent(registrationRequest);

            // Assert
            assertThat(response.getBody().getParticipationMode()).isEqualTo(EventMode.ONLINE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/events/business/registrations/{registrationId} - Annulation d'inscription
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/events/business/registrations/{registrationId} - Annulation")
    class CancelRegistration {

        @Test
        @DisplayName("Doit annuler une inscription avec succès → 200 OK")
        void shouldCancelRegistrationSuccessfully() {
            // Arrange : la réponse indique que l'inscription est annulée
            registrationResponse.setStatus(RegistrationStatus.CANCELLED);
            registrationResponse.setMessage("Inscription annulée");
            when(registrationService.cancelRegistration(100L, 10L)).thenReturn(registrationResponse);

            // Act
            ResponseEntity<EventRegistrationResponse> response =
                    eventBusinessController.cancelRegistration(100L, 10L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
            verify(registrationService, times(1)).cancelRegistration(100L, 10L);
        }

        @Test
        @DisplayName("Doit déléguer l'annulation au service avec les bons paramètres")
        void shouldDelegateCancellationWithCorrectParams() {
            // Arrange
            when(registrationService.cancelRegistration(eq(200L), eq(20L)))
                    .thenReturn(registrationResponse);

            // Act
            eventBusinessController.cancelRegistration(200L, 20L);

            // Assert : vérification que les bons paramètres sont passés au service
            verify(registrationService, times(1)).cancelRegistration(200L, 20L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/events/business/registrations/{registrationId}/confirm-payment - Confirmation paiement
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/events/business/registrations/{registrationId}/confirm-payment - Paiement")
    class ConfirmPayment {

        @Test
        @DisplayName("Doit confirmer le paiement d'une inscription → 200 OK")
        void shouldConfirmPaymentSuccessfully() {
            // Arrange : le paiement est confirmé
            registrationResponse.setPaymentStatus(PaymentStatus.PAID);
            when(registrationService.confirmPayment(100L)).thenReturn(registrationResponse);

            // Act
            ResponseEntity<EventRegistrationResponse> response =
                    eventBusinessController.confirmPayment(100L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
            verify(registrationService, times(1)).confirmPayment(100L);
        }

        @Test
        @DisplayName("Doit propager l'exception si l'inscription n'existe pas")
        void shouldPropagateExceptionWhenRegistrationNotFound() {
            // Arrange
            when(registrationService.confirmPayment(999L))
                    .thenThrow(new RuntimeException("Registration not found: 999"));

            // Act & Assert
            org.assertj.core.api.Assertions.assertThatThrownBy(
                    () -> eventBusinessController.confirmPayment(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Registration not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events/business/{eventId}/registrations - Inscriptions d'un événement
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/events/business/{eventId}/registrations - Inscriptions d'un événement")
    class GetEventRegistrations {

        @Test
        @DisplayName("Doit retourner les inscriptions d'un événement → 200 OK")
        void shouldReturnEventRegistrations() {
            // Arrange
            when(registrationService.getEventRegistrations(1L)).thenReturn(List.of(eventRegistration));

            // Act
            ResponseEntity<List<EventRegistration>> response =
                    eventBusinessController.getEventRegistrations(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            verify(registrationService, times(1)).getEventRegistrations(1L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune inscription → 200 OK")
        void shouldReturnEmptyListWhenNoRegistrations() {
            // Arrange
            when(registrationService.getEventRegistrations(99L)).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<EventRegistration>> response =
                    eventBusinessController.getEventRegistrations(99L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events/business/user/{userId}/registrations - Inscriptions d'un utilisateur
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/events/business/user/{userId}/registrations - Inscriptions d'un utilisateur")
    class GetUserRegistrations {

        @Test
        @DisplayName("Doit retourner les inscriptions de l'utilisateur → 200 OK")
        void shouldReturnUserRegistrations() {
            // Arrange
            when(registrationService.getUserRegistrations(10L)).thenReturn(List.of(eventRegistration));

            // Act
            ResponseEntity<List<EventRegistration>> response =
                    eventBusinessController.getUserRegistrations(10L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            verify(registrationService, times(1)).getUserRegistrations(10L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si l'utilisateur n'a aucune inscription → 200 OK")
        void shouldReturnEmptyListWhenNoUserRegistrations() {
            // Arrange
            when(registrationService.getUserRegistrations(99L)).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<EventRegistration>> response =
                    eventBusinessController.getUserRegistrations(99L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events/business/{eventId}/waiting-list - Liste d'attente
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/events/business/{eventId}/waiting-list - Liste d'attente")
    class GetWaitingList {

        @Test
        @DisplayName("Doit retourner la liste d'attente d'un événement → 200 OK")
        void shouldReturnWaitingList() {
            // Arrange
            when(registrationService.getWaitingList(1L)).thenReturn(List.of(eventRegistration));

            // Act
            ResponseEntity<List<EventRegistration>> response =
                    eventBusinessController.getWaitingList(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            verify(registrationService, times(1)).getWaitingList(1L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune liste d'attente → 200 OK")
        void shouldReturnEmptyWaitingList() {
            // Arrange
            when(registrationService.getWaitingList(1L)).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<EventRegistration>> response =
                    eventBusinessController.getWaitingList(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events/business/{eventId}/analytics - Analytics d'un événement
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/events/business/{eventId}/analytics - Analytics")
    class GetEventAnalytics {

        @Test
        @DisplayName("Doit retourner les analytics d'un événement → 200 OK")
        void shouldReturnEventAnalytics() {
            // Arrange : construction d'une réponse d'analytics
            EventAnalyticsResponse analytics = new EventAnalyticsResponse();
            when(analyticsService.getAnalytics(1L)).thenReturn(analytics);

            // Act
            ResponseEntity<EventAnalyticsResponse> response =
                    eventBusinessController.getEventAnalytics(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(analyticsService, times(1)).getAnalytics(1L);
        }

        @Test
        @DisplayName("Doit déléguer le calcul des analytics au service")
        void shouldDelegateAnalyticsToService() {
            // Arrange
            EventAnalyticsResponse analytics = new EventAnalyticsResponse();
            when(analyticsService.getAnalytics(eq(5L))).thenReturn(analytics);

            // Act
            eventBusinessController.getEventAnalytics(5L);

            // Assert : vérification de la délégation au service
            verify(analyticsService, times(1)).getAnalytics(5L);
            verifyNoMoreInteractions(analyticsService);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events/business/{eventId}/revenue - Revenus d'un événement
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/events/business/{eventId}/revenue - Revenus")
    class CalculateRevenue {

        @Test
        @DisplayName("Doit retourner les revenus d'un événement → 200 OK")
        void shouldReturnEventRevenue() {
            // Arrange : construction d'une réponse de revenus
            EventRevenueResponse revenue = new EventRevenueResponse();
            when(analyticsService.calculateRevenue(1L)).thenReturn(revenue);

            // Act
            ResponseEntity<EventRevenueResponse> response =
                    eventBusinessController.calculateRevenue(1L);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(analyticsService, times(1)).calculateRevenue(1L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/events/business/health - Vérification de santé
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/events/business/health - Doit retourner 200 OK avec message de santé")
    void healthEndpointShouldReturnOk() {
        // Act
        ResponseEntity<String> response = eventBusinessController.health();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Event Business Service is running");
    }
}
