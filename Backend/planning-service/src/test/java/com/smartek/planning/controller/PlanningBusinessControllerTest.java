package com.smartek.planning.controller;

import com.smartek.planning.dto.*;
import com.smartek.planning.service.PlanningBusinessService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PlanningBusinessController (sans Spring context).
 * Verifie la delegation au service et les codes HTTP retournes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlanningBusinessController - Tests unitaires")
class PlanningBusinessControllerTest {

    @Mock
    private PlanningBusinessService businessService;

    @InjectMocks
    private PlanningBusinessController planningBusinessController;

    private ConflictCheckRequest conflictCheckRequest;
    private ConflictCheckResponse noConflictResponse;
    private ConflictCheckResponse conflictResponse;
    private TimeSlotSuggestionRequest suggestionRequest;
    private TimeSlotSuggestion sampleSuggestion;

    @BeforeEach
    void setUp() {
        // Requete de verification de conflits
        conflictCheckRequest = new ConflictCheckRequest(
                LocalDate.of(2026, 6, 15),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                10L,
                "salle-A",
                null
        );

        // Reponse sans conflit
        noConflictResponse = new ConflictCheckResponse(false, Collections.emptyList());

        // Reponse avec conflit
        ConflictCheckResponse.ConflictDetail detail = new ConflictCheckResponse.ConflictDetail(
                "TRAINER",
                "Le formateur a deja une session a ce creneau",
                5L,
                "Formation Java Avancee"
        );
        conflictResponse = new ConflictCheckResponse(true, List.of(detail));

        // Requete de suggestion de creneaux
        suggestionRequest = new TimeSlotSuggestionRequest(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                120,
                10L,
                "salle-A",
                3
        );

        // Suggestion de creneau
        sampleSuggestion = new TimeSlotSuggestion(
                LocalDate.of(2026, 6, 16),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                95,
                "Pas de conflit, creneau optimal"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // checkConflicts
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/plannings/business/check-conflicts - Verification des conflits")
    class CheckConflicts {

        @Test
        @DisplayName("Doit retourner 200 sans conflit si le creneau est libre")
        void shouldReturn200WithNoConflictWhenSlotIsFree() {
            when(businessService.checkConflicts(any(ConflictCheckRequest.class))).thenReturn(noConflictResponse);

            ResponseEntity<ConflictCheckResponse> response =
                    planningBusinessController.checkConflicts(conflictCheckRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isHasConflict()).isFalse();
            assertThat(response.getBody().getConflicts()).isEmpty();
            verify(businessService, times(1)).checkConflicts(any(ConflictCheckRequest.class));
        }

        @Test
        @DisplayName("Doit retourner 200 avec les details du conflit si le creneau est occupe")
        void shouldReturn200WithConflictDetailsWhenSlotIsOccupied() {
            when(businessService.checkConflicts(any(ConflictCheckRequest.class))).thenReturn(conflictResponse);

            ResponseEntity<ConflictCheckResponse> response =
                    planningBusinessController.checkConflicts(conflictCheckRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().isHasConflict()).isTrue();
            assertThat(response.getBody().getConflicts()).hasSize(1);
            assertThat(response.getBody().getConflicts().get(0).getType()).isEqualTo("TRAINER");
            assertThat(response.getBody().getConflicts().get(0).getConflictingPlanningId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Doit deleguer la verification au service avec la requete correcte")
        void shouldDelegateToServiceWithCorrectRequest() {
            when(businessService.checkConflicts(conflictCheckRequest)).thenReturn(noConflictResponse);

            planningBusinessController.checkConflicts(conflictCheckRequest);

            verify(businessService).checkConflicts(conflictCheckRequest);
        }

        @Test
        @DisplayName("Doit propager l'exception si le service echoue")
        void shouldPropagateExceptionWhenServiceFails() {
            when(businessService.checkConflicts(any()))
                    .thenThrow(new RuntimeException("Erreur lors de la verification des conflits"));

            assertThatThrownBy(() -> planningBusinessController.checkConflicts(conflictCheckRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erreur lors de la verification");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // suggestTimeSlots
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/plannings/business/suggest-slots - Suggestion de creneaux")
    class SuggestTimeSlots {

        @Test
        @DisplayName("Doit retourner 200 avec une liste de suggestions")
        void shouldReturn200WithSuggestions() {
            when(businessService.suggestTimeSlots(any(TimeSlotSuggestionRequest.class)))
                    .thenReturn(List.of(sampleSuggestion));

            ResponseEntity<List<TimeSlotSuggestion>> response =
                    planningBusinessController.suggestTimeSlots(suggestionRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getScore()).isEqualTo(95);
            assertThat(response.getBody().get(0).getReason()).contains("optimal");
            verify(businessService, times(1)).suggestTimeSlots(any(TimeSlotSuggestionRequest.class));
        }

        @Test
        @DisplayName("Doit retourner 200 avec une liste vide si aucun creneau disponible")
        void shouldReturn200WithEmptyListWhenNoSlotsAvailable() {
            when(businessService.suggestTimeSlots(any())).thenReturn(Collections.emptyList());

            ResponseEntity<List<TimeSlotSuggestion>> response =
                    planningBusinessController.suggestTimeSlots(suggestionRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner plusieurs suggestions triees par score")
        void shouldReturnMultipleSuggestions() {
            TimeSlotSuggestion suggestion2 = new TimeSlotSuggestion(
                    LocalDate.of(2026, 6, 17),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0),
                    80,
                    "Creneau acceptable"
            );
            when(businessService.suggestTimeSlots(any()))
                    .thenReturn(List.of(sampleSuggestion, suggestion2));

            ResponseEntity<List<TimeSlotSuggestion>> response =
                    planningBusinessController.suggestTimeSlots(suggestionRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getScore()).isEqualTo(95);
            assertThat(response.getBody().get(1).getScore()).isEqualTo(80);
        }

        @Test
        @DisplayName("Doit deleguer la suggestion au service avec la requete correcte")
        void shouldDelegateToServiceWithCorrectRequest() {
            when(businessService.suggestTimeSlots(suggestionRequest)).thenReturn(List.of(sampleSuggestion));

            planningBusinessController.suggestTimeSlots(suggestionRequest);

            verify(businessService).suggestTimeSlots(suggestionRequest);
        }

        @Test
        @DisplayName("Doit propager l'exception si le service echoue")
        void shouldPropagateExceptionWhenServiceFails() {
            when(businessService.suggestTimeSlots(any()))
                    .thenThrow(new RuntimeException("Erreur lors de la suggestion de creneaux"));

            assertThatThrownBy(() -> planningBusinessController.suggestTimeSlots(suggestionRequest))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTrainerWorkload
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/plannings/business/trainer-workload/{trainerId} - Charge de travail")
    class GetTrainerWorkload {

        @Test
        @DisplayName("Doit retourner la charge de travail du formateur avec 200")
        void shouldReturnTrainerWorkloadWith200() {
            TrainerWorkloadResponse workload = new TrainerWorkloadResponse(
                    10L,
                    LocalDate.of(2026, 6, 15),
                    6,
                    0,
                    3,
                    false,
                    8,
                    null
            );
            when(businessService.getTrainerWorkload(eq(10L), eq(LocalDate.of(2026, 6, 15))))
                    .thenReturn(workload);

            ResponseEntity<TrainerWorkloadResponse> response =
                    planningBusinessController.getTrainerWorkload(10L, LocalDate.of(2026, 6, 15));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTrainerId()).isEqualTo(10L);
            assertThat(response.getBody().getTotalHours()).isEqualTo(6);
            assertThat(response.getBody().getSessionCount()).isEqualTo(3);
            assertThat(response.getBody().isOverloaded()).isFalse();
            verify(businessService).getTrainerWorkload(10L, LocalDate.of(2026, 6, 15));
        }

        @Test
        @DisplayName("Doit retourner un avertissement si le formateur est surcharge")
        void shouldReturnWarningWhenTrainerIsOverloaded() {
            TrainerWorkloadResponse overloadedWorkload = new TrainerWorkloadResponse(
                    10L,
                    LocalDate.of(2026, 6, 15),
                    10,
                    0,
                    5,
                    true,
                    8,
                    "Attention: le formateur depasse la limite de 8 heures par jour"
            );
            when(businessService.getTrainerWorkload(eq(10L), any(LocalDate.class)))
                    .thenReturn(overloadedWorkload);

            ResponseEntity<TrainerWorkloadResponse> response =
                    planningBusinessController.getTrainerWorkload(10L, LocalDate.of(2026, 6, 15));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().isOverloaded()).isTrue();
            assertThat(response.getBody().getWarning()).contains("depasse");
        }

        @Test
        @DisplayName("Doit retourner une charge nulle si le formateur n'a pas de sessions ce jour")
        void shouldReturnZeroWorkloadWhenNoSessions() {
            TrainerWorkloadResponse emptyWorkload = new TrainerWorkloadResponse(
                    10L,
                    LocalDate.of(2026, 6, 20),
                    0,
                    0,
                    0,
                    false,
                    8,
                    null
            );
            when(businessService.getTrainerWorkload(eq(10L), eq(LocalDate.of(2026, 6, 20))))
                    .thenReturn(emptyWorkload);

            ResponseEntity<TrainerWorkloadResponse> response =
                    planningBusinessController.getTrainerWorkload(10L, LocalDate.of(2026, 6, 20));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getTotalHours()).isEqualTo(0);
            assertThat(response.getBody().getSessionCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Doit deleguer au service avec trainerId et date corrects")
        void shouldDelegateWithCorrectParams() {
            TrainerWorkloadResponse workload = new TrainerWorkloadResponse();
            workload.setTrainerId(10L);
            when(businessService.getTrainerWorkload(10L, LocalDate.of(2026, 6, 15))).thenReturn(workload);

            planningBusinessController.getTrainerWorkload(10L, LocalDate.of(2026, 6, 15));

            verify(businessService).getTrainerWorkload(10L, LocalDate.of(2026, 6, 15));
        }

        @Test
        @DisplayName("Doit propager l'exception si le formateur n'existe pas")
        void shouldPropagateExceptionWhenTrainerNotFound() {
            when(businessService.getTrainerWorkload(eq(999L), any()))
                    .thenThrow(new RuntimeException("Formateur non trouve avec l'ID: 999"));

            assertThatThrownBy(() ->
                    planningBusinessController.getTrainerWorkload(999L, LocalDate.of(2026, 6, 15)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // health
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/plannings/business/health - Sante du service")
    class Health {

        @Test
        @DisplayName("Doit retourner 200 avec le message de sante")
        void shouldReturn200WithHealthMessage() {
            ResponseEntity<String> response = planningBusinessController.health();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("Planning Business Service is running");
        }

        @Test
        @DisplayName("Doit retourner le message exact sans appel au service")
        void shouldReturnExactMessageWithoutServiceCall() {
            ResponseEntity<String> response = planningBusinessController.health();

            assertThat(response.getBody()).contains("Planning Business Service");
            verifyNoInteractions(businessService);
        }

        @Test
        @DisplayName("Doit toujours retourner 200 (endpoint de sante stable)")
        void shouldAlwaysReturn200() {
            // Appel multiple pour verifier la stabilite
            ResponseEntity<String> response1 = planningBusinessController.health();
            ResponseEntity<String> response2 = planningBusinessController.health();

            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response1.getBody()).isEqualTo(response2.getBody());
        }
    }
}
