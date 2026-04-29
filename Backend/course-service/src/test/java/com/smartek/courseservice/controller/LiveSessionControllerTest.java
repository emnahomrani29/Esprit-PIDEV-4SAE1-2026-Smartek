package com.smartek.courseservice.controller;

import com.smartek.courseservice.dto.LiveSessionRequest;
import com.smartek.courseservice.dto.LiveSessionResponse;
import com.smartek.courseservice.entity.SessionStatus;
import com.smartek.courseservice.service.LiveSessionService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour LiveSessionController (sans Spring context).
 * Verifie la delegation au service et les codes HTTP retournes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LiveSessionController - Tests unitaires")
class LiveSessionControllerTest {

    @Mock
    private LiveSessionService liveSessionService;

    @InjectMocks
    private LiveSessionController liveSessionController;

    private LiveSessionRequest validRequest;
    private LiveSessionResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = LiveSessionRequest.builder()
                .courseId(5L)
                .title("Session Live Spring Boot")
                .description("Session de formation en direct")
                .trainerId(10L)
                .startTime(LocalDateTime.of(2026, 6, 15, 9, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 11, 0))
                .maxParticipants(30)
                .build();

        sampleResponse = LiveSessionResponse.builder()
                .sessionId(1L)
                .courseId(5L)
                .title("Session Live Spring Boot")
                .description("Session de formation en direct")
                .trainerId(10L)
                .startTime(LocalDateTime.of(2026, 6, 15, 9, 0))
                .endTime(LocalDateTime.of(2026, 6, 15, 11, 0))
                .roomId("room-abc123")
                .status(SessionStatus.SCHEDULED)
                .maxParticipants(30)
                .currentParticipants(0)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createSession
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/courses/sessions - Creation de session live")
    class CreateSession {

        @Test
        @DisplayName("Doit creer une session live et retourner 201")
        void shouldCreateSessionAndReturn201() {
            when(liveSessionService.createSession(any(LiveSessionRequest.class))).thenReturn(sampleResponse);

            ResponseEntity<LiveSessionResponse> response = liveSessionController.createSession(validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSessionId()).isEqualTo(1L);
            assertThat(response.getBody().getTitle()).isEqualTo("Session Live Spring Boot");
            assertThat(response.getBody().getRoomId()).isEqualTo("room-abc123");
            verify(liveSessionService, times(1)).createSession(any(LiveSessionRequest.class));
        }

        @Test
        @DisplayName("Doit deleguer la creation au service")
        void shouldDelegateToService() {
            when(liveSessionService.createSession(validRequest)).thenReturn(sampleResponse);

            liveSessionController.createSession(validRequest);

            verify(liveSessionService).createSession(validRequest);
        }

        @Test
        @DisplayName("Doit retourner 400 si le cours n'existe pas")
        void shouldReturn400WhenCourseNotFound() {
            when(liveSessionService.createSession(any()))
                    .thenThrow(new RuntimeException("Cours non trouve avec l'ID: 999"));

            ResponseEntity<LiveSessionResponse> response = liveSessionController.createSession(validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).contains("999");
        }

        @Test
        @DisplayName("Doit retourner 400 si le cours n'est pas en mode EN_LIGNE")
        void shouldReturn400WhenCourseIsNotOnline() {
            when(liveSessionService.createSession(any()))
                    .thenThrow(new RuntimeException("Les sessions live ne sont disponibles que pour les cours en ligne"));

            ResponseEntity<LiveSessionResponse> response = liveSessionController.createSession(validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).contains("en ligne");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSessionsByCourseId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/course/{courseId}/sessions - Sessions d'un cours")
    class GetSessionsByCourseId {

        @Test
        @DisplayName("Doit retourner les sessions d'un cours avec 200")
        void shouldReturnSessionsByCourseWith200() {
            when(liveSessionService.getSessionsByCourseId(5L)).thenReturn(List.of(sampleResponse));

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getSessionsByCourseId(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getCourseId()).isEqualTo(5L);
            verify(liveSessionService).getSessionsByCourseId(5L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune session")
        void shouldReturnEmptyListWhenNoSessions() {
            when(liveSessionService.getSessionsByCourseId(5L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getSessionsByCourseId(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si le cours n'existe pas (exception geree)")
        void shouldReturnEmptyListWhenCourseNotFound() {
            when(liveSessionService.getSessionsByCourseId(999L))
                    .thenThrow(new RuntimeException("Cours non trouve"));

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getSessionsByCourseId(999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUpcomingSessionsByCourseId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/course/{courseId}/sessions/upcoming - Sessions a venir")
    class GetUpcomingSessionsByCourseId {

        @Test
        @DisplayName("Doit retourner les sessions a venir avec 200")
        void shouldReturnUpcomingSessionsWith200() {
            when(liveSessionService.getUpcomingSessionsByCourseId(5L)).thenReturn(List.of(sampleResponse));

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getUpcomingSessionsByCourseId(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getStatus()).isEqualTo(SessionStatus.SCHEDULED);
            verify(liveSessionService).getUpcomingSessionsByCourseId(5L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune session a venir")
        void shouldReturnEmptyListWhenNoUpcomingSessions() {
            when(liveSessionService.getUpcomingSessionsByCourseId(5L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getUpcomingSessionsByCourseId(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si une exception est levee")
        void shouldReturnEmptyListOnException() {
            when(liveSessionService.getUpcomingSessionsByCourseId(999L))
                    .thenThrow(new RuntimeException("Cours non trouve"));

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getUpcomingSessionsByCourseId(999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getOngoingSessionsByCourseId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/course/{courseId}/sessions/ongoing - Sessions en cours")
    class GetOngoingSessionsByCourseId {

        @Test
        @DisplayName("Doit retourner les sessions en cours avec 200")
        void shouldReturnOngoingSessionsWith200() {
            LiveSessionResponse ongoingSession = LiveSessionResponse.builder()
                    .sessionId(2L)
                    .courseId(5L)
                    .title("Session en cours")
                    .status(SessionStatus.ONGOING)
                    .build();
            when(liveSessionService.getOngoingSessionsByCourseId(5L)).thenReturn(List.of(ongoingSession));

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getOngoingSessionsByCourseId(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getStatus()).isEqualTo(SessionStatus.ONGOING);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucune session en cours")
        void shouldReturnEmptyListWhenNoOngoingSessions() {
            when(liveSessionService.getOngoingSessionsByCourseId(5L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getOngoingSessionsByCourseId(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si une exception est levee")
        void shouldReturnEmptyListOnException() {
            when(liveSessionService.getOngoingSessionsByCourseId(999L))
                    .thenThrow(new RuntimeException("Cours non trouve"));

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getOngoingSessionsByCourseId(999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSessionById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/sessions/{sessionId}")
    class GetSessionById {

        @Test
        @DisplayName("Doit retourner la session par ID avec 200")
        void shouldReturnSessionByIdWith200() {
            when(liveSessionService.getSessionById(1L)).thenReturn(sampleResponse);

            ResponseEntity<LiveSessionResponse> response = liveSessionController.getSessionById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSessionId()).isEqualTo(1L);
            verify(liveSessionService).getSessionById(1L);
        }

        @Test
        @DisplayName("Doit retourner 404 si la session n'existe pas")
        void shouldReturn404WhenSessionNotFound() {
            when(liveSessionService.getSessionById(999L))
                    .thenThrow(new RuntimeException("Session non trouvee avec l'ID: 999"));

            ResponseEntity<LiveSessionResponse> response = liveSessionController.getSessionById(999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().getMessage()).contains("999");
        }

        @Test
        @DisplayName("Doit deleguer au service avec le sessionId correct")
        void shouldDelegateWithCorrectSessionId() {
            when(liveSessionService.getSessionById(1L)).thenReturn(sampleResponse);

            liveSessionController.getSessionById(1L);

            verify(liveSessionService).getSessionById(1L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateSession
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/courses/sessions/{sessionId} - Mise a jour")
    class UpdateSession {

        @Test
        @DisplayName("Doit mettre a jour une session et retourner 200")
        void shouldUpdateSessionAndReturn200() {
            when(liveSessionService.updateSession(eq(1L), any(LiveSessionRequest.class))).thenReturn(sampleResponse);

            ResponseEntity<LiveSessionResponse> response = liveSessionController.updateSession(1L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(liveSessionService).updateSession(eq(1L), any(LiveSessionRequest.class));
        }

        @Test
        @DisplayName("Doit retourner 400 si la session n'existe pas")
        void shouldReturn400WhenSessionNotFound() {
            when(liveSessionService.updateSession(eq(999L), any()))
                    .thenThrow(new RuntimeException("Session non trouvee avec l'ID: 999"));

            ResponseEntity<LiveSessionResponse> response = liveSessionController.updateSession(999L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).contains("999");
        }

        @Test
        @DisplayName("Doit retourner 400 si l'heure de fin est avant l'heure de debut")
        void shouldReturn400WhenEndTimeBeforeStartTime() {
            when(liveSessionService.updateSession(eq(1L), any()))
                    .thenThrow(new RuntimeException("L'heure de fin doit etre apres l'heure de debut"));

            ResponseEntity<LiveSessionResponse> response = liveSessionController.updateSession(1L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateSessionStatus
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH /api/courses/sessions/{sessionId}/status - Changement de statut")
    class UpdateSessionStatus {

        @Test
        @DisplayName("Doit changer le statut vers ONGOING et retourner 200")
        void shouldUpdateStatusToOngoingAndReturn200() {
            LiveSessionResponse ongoingResponse = LiveSessionResponse.builder()
                    .sessionId(1L)
                    .status(SessionStatus.ONGOING)
                    .build();
            when(liveSessionService.updateSessionStatus(1L, SessionStatus.ONGOING)).thenReturn(ongoingResponse);

            ResponseEntity<LiveSessionResponse> response =
                    liveSessionController.updateSessionStatus(1L, SessionStatus.ONGOING);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getStatus()).isEqualTo(SessionStatus.ONGOING);
            verify(liveSessionService).updateSessionStatus(1L, SessionStatus.ONGOING);
        }

        @Test
        @DisplayName("Doit changer le statut vers COMPLETED et retourner 200")
        void shouldUpdateStatusToCompletedAndReturn200() {
            LiveSessionResponse completedResponse = LiveSessionResponse.builder()
                    .sessionId(1L)
                    .status(SessionStatus.COMPLETED)
                    .build();
            when(liveSessionService.updateSessionStatus(1L, SessionStatus.COMPLETED)).thenReturn(completedResponse);

            ResponseEntity<LiveSessionResponse> response =
                    liveSessionController.updateSessionStatus(1L, SessionStatus.COMPLETED);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getStatus()).isEqualTo(SessionStatus.COMPLETED);
        }

        @Test
        @DisplayName("Doit retourner 400 si la session n'existe pas")
        void shouldReturn400WhenSessionNotFound() {
            when(liveSessionService.updateSessionStatus(999L, SessionStatus.CANCELLED))
                    .thenThrow(new RuntimeException("Session non trouvee avec l'ID: 999"));

            ResponseEntity<LiveSessionResponse> response =
                    liveSessionController.updateSessionStatus(999L, SessionStatus.CANCELLED);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteSession
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/courses/sessions/{sessionId}")
    class DeleteSession {

        @Test
        @DisplayName("Doit supprimer une session et retourner 204")
        void shouldDeleteSessionAndReturn204() {
            doNothing().when(liveSessionService).deleteSession(1L);

            ResponseEntity<Void> response = liveSessionController.deleteSession(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(liveSessionService).deleteSession(1L);
        }

        @Test
        @DisplayName("Doit retourner 404 si la session n'existe pas")
        void shouldReturn404WhenSessionNotFound() {
            doThrow(new RuntimeException("Session non trouvee avec l'ID: 999"))
                    .when(liveSessionService).deleteSession(999L);

            ResponseEntity<Void> response = liveSessionController.deleteSession(999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Doit deleguer la suppression au service avec le sessionId correct")
        void shouldDelegateWithCorrectSessionId() {
            doNothing().when(liveSessionService).deleteSession(1L);

            liveSessionController.deleteSession(1L);

            verify(liveSessionService).deleteSession(1L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSessionsByTrainerId
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/trainer/{trainerId}/sessions - Sessions d'un trainer")
    class GetSessionsByTrainerId {

        @Test
        @DisplayName("Doit retourner les sessions d'un trainer avec 200")
        void shouldReturnSessionsByTrainerWith200() {
            when(liveSessionService.getSessionsByTrainerId(10L)).thenReturn(List.of(sampleResponse));

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getSessionsByTrainerId(10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTrainerId()).isEqualTo(10L);
            verify(liveSessionService).getSessionsByTrainerId(10L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si le trainer n'a pas de sessions")
        void shouldReturnEmptyListWhenNoSessions() {
            when(liveSessionService.getSessionsByTrainerId(99L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getSessionsByTrainerId(99L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner plusieurs sessions pour un trainer")
        void shouldReturnMultipleSessionsForTrainer() {
            LiveSessionResponse session2 = LiveSessionResponse.builder()
                    .sessionId(2L)
                    .trainerId(10L)
                    .title("Session 2")
                    .status(SessionStatus.COMPLETED)
                    .build();
            when(liveSessionService.getSessionsByTrainerId(10L)).thenReturn(List.of(sampleResponse, session2));

            ResponseEntity<List<LiveSessionResponse>> response =
                    liveSessionController.getSessionsByTrainerId(10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
        }
    }
}
