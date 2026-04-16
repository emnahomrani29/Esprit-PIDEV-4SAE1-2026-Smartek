package com.smartek.planning.service;

import com.smartek.planning.dto.*;
import com.smartek.planning.model.Planning;
import com.smartek.planning.repository.PlanningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PlanningBusinessService.
 * Couvre la logique métier avancée : détection de conflits, suggestions de créneaux,
 * calcul de charge de travail.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlanningBusinessService - Logique métier avancée")
class PlanningBusinessServiceTest {

    @Mock
    private PlanningRepository planningRepository;

    @InjectMocks
    private PlanningBusinessService planningBusinessService;

    private Planning existingPlanning;

    @BeforeEach
    void setUp() {
        existingPlanning = new Planning();
        existingPlanning.setPlanningId(1L);
        existingPlanning.setDate(LocalDate.of(2026, 5, 15));
        existingPlanning.setStartTime(LocalTime.of(9, 0));
        existingPlanning.setEndTime(LocalTime.of(11, 0));
        existingPlanning.setTitle("Session existante");
        existingPlanning.setTrainerId(10L);
        existingPlanning.setRoomId("SALLE_A");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // checkConflicts
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("checkConflicts() - Détection de conflits")
    class CheckConflicts {

        @Test
        @DisplayName("Doit retourner aucun conflit quand le créneau est libre")
        void shouldReturnNoConflictsWhenSlotIsFree() {
            when(planningRepository.findPlanningsByDate(any())).thenReturn(Collections.emptyList());

            ConflictCheckRequest request = new ConflictCheckRequest();
            request.setDate(LocalDate.of(2026, 5, 15));
            request.setStartTime(LocalTime.of(14, 0));
            request.setEndTime(LocalTime.of(16, 0));
            request.setTrainerId(10L);

            ConflictCheckResponse result = planningBusinessService.checkConflicts(request);

            assertThat(result.isHasConflict()).isFalse();
            assertThat(result.getConflicts()).isEmpty();
        }

        @Test
        @DisplayName("Doit détecter un conflit de formateur sur le même créneau")
        void shouldDetectTrainerConflict() {
            when(planningRepository.findPlanningsByDate(any())).thenReturn(List.of(existingPlanning));

            ConflictCheckRequest request = new ConflictCheckRequest();
            request.setDate(LocalDate.of(2026, 5, 15));
            request.setStartTime(LocalTime.of(9, 30));  // chevauchement avec 9h-11h
            request.setEndTime(LocalTime.of(10, 30));
            request.setTrainerId(10L);  // même trainer

            ConflictCheckResponse result = planningBusinessService.checkConflicts(request);

            assertThat(result.isHasConflict()).isTrue();
            assertThat(result.getConflicts()).hasSize(1);
            assertThat(result.getConflicts().get(0).getType()).isEqualTo("TRAINER");
        }

        @Test
        @DisplayName("Doit détecter un conflit de salle sur le même créneau")
        void shouldDetectRoomConflict() {
            when(planningRepository.findPlanningsByDate(any())).thenReturn(List.of(existingPlanning));

            ConflictCheckRequest request = new ConflictCheckRequest();
            request.setDate(LocalDate.of(2026, 5, 15));
            request.setStartTime(LocalTime.of(9, 30));
            request.setEndTime(LocalTime.of(10, 30));
            request.setTrainerId(99L);   // trainer différent
            request.setRoomId("SALLE_A"); // même salle

            ConflictCheckResponse result = planningBusinessService.checkConflicts(request);

            assertThat(result.isHasConflict()).isTrue();
            assertThat(result.getConflicts().get(0).getType()).isEqualTo("ROOM");
        }

        @Test
        @DisplayName("Doit exclure le planning en cours de modification lors de la vérification")
        void shouldExcludeCurrentPlanningFromConflictCheck() {
            when(planningRepository.findPlanningsByDate(any())).thenReturn(List.of(existingPlanning));

            ConflictCheckRequest request = new ConflictCheckRequest();
            request.setDate(LocalDate.of(2026, 5, 15));
            request.setStartTime(LocalTime.of(9, 30));
            request.setEndTime(LocalTime.of(10, 30));
            request.setTrainerId(10L);
            request.setExcludePlanningId(1L); // exclure le planning existant (modification)

            ConflictCheckResponse result = planningBusinessService.checkConflicts(request);

            assertThat(result.isHasConflict()).isFalse();
        }

        @Test
        @DisplayName("Doit détecter plusieurs conflits (trainer + salle) simultanément")
        void shouldDetectMultipleConflictsSimultaneously() {
            when(planningRepository.findPlanningsByDate(any())).thenReturn(List.of(existingPlanning));

            ConflictCheckRequest request = new ConflictCheckRequest();
            request.setDate(LocalDate.of(2026, 5, 15));
            request.setStartTime(LocalTime.of(9, 30));
            request.setEndTime(LocalTime.of(10, 30));
            request.setTrainerId(10L);    // même trainer
            request.setRoomId("SALLE_A"); // même salle

            ConflictCheckResponse result = planningBusinessService.checkConflicts(request);

            assertThat(result.isHasConflict()).isTrue();
            assertThat(result.getConflicts()).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTrainerWorkload
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTrainerWorkload() - Calcul de charge")
    class GetTrainerWorkload {

        @Test
        @DisplayName("Doit calculer correctement la charge de travail (2h de session)")
        void shouldCalculateWorkloadCorrectly() {
            existingPlanning.setTrainerId(10L);
            when(planningRepository.findPlanningsByDate(any())).thenReturn(List.of(existingPlanning));

            TrainerWorkloadResponse result = planningBusinessService.getTrainerWorkload(10L, LocalDate.of(2026, 5, 15));

            assertThat(result.getTrainerId()).isEqualTo(10L);
            assertThat(result.getTotalHours()).isEqualTo(2);
            assertThat(result.getTotalMinutes()).isZero();
            assertThat(result.getSessionCount()).isEqualTo(1);
            assertThat(result.isOverloaded()).isFalse();
        }

        @Test
        @DisplayName("Doit signaler une surcharge si le formateur dépasse 8h par jour")
        void shouldFlagOverloadWhenExceeding8Hours() {
            // Créer 5 sessions de 2h = 10h > 8h max
            List<Planning> sessions = new java.util.ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Planning p = new Planning();
                p.setTrainerId(10L);
                p.setStartTime(LocalTime.of(8 + (i * 2), 0));
                p.setEndTime(LocalTime.of(10 + (i * 2), 0));
                sessions.add(p);
            }
            when(planningRepository.findPlanningsByDate(any())).thenReturn(sessions);

            TrainerWorkloadResponse result = planningBusinessService.getTrainerWorkload(10L, LocalDate.of(2026, 5, 15));

            assertThat(result.getTotalHours()).isGreaterThanOrEqualTo(8);
            assertThat(result.isOverloaded()).isTrue();
            assertThat(result.getWarning()).isNotNull();
        }

        @Test
        @DisplayName("Doit retourner 0 session si le formateur n'a rien ce jour-là")
        void shouldReturnZeroSessionsWhenNoPlanning() {
            when(planningRepository.findPlanningsByDate(any())).thenReturn(Collections.emptyList());

            TrainerWorkloadResponse result = planningBusinessService.getTrainerWorkload(10L, LocalDate.of(2026, 5, 15));

            assertThat(result.getSessionCount()).isZero();
            assertThat(result.getTotalHours()).isZero();
            assertThat(result.isOverloaded()).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // suggestTimeSlots
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("suggestTimeSlots() - Suggestions de créneaux")
    class SuggestTimeSlots {

        @Test
        @DisplayName("Doit suggérer des créneaux libres sur une journée sans planning")
        void shouldSuggestFreeSlotsOnEmptyDay() {
            when(planningRepository.findPlanningsByDate(any())).thenReturn(Collections.emptyList());

            TimeSlotSuggestionRequest request = new TimeSlotSuggestionRequest();
            request.setStartDate(LocalDate.of(2026, 5, 18)); // lundi
            request.setEndDate(LocalDate.of(2026, 5, 18));
            request.setDurationMinutes(60);
            request.setMaxSuggestions(3);

            List<TimeSlotSuggestion> suggestions = planningBusinessService.suggestTimeSlots(request);

            assertThat(suggestions).isNotEmpty();
            assertThat(suggestions.size()).isLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Doit ignorer les week-ends lors des suggestions")
        void shouldIgnoreWeekends() {
            // 16 mai 2026 = samedi, 17 mai = dimanche
            TimeSlotSuggestionRequest request = new TimeSlotSuggestionRequest();
            request.setStartDate(LocalDate.of(2026, 5, 16)); // samedi
            request.setEndDate(LocalDate.of(2026, 5, 17));   // dimanche
            request.setDurationMinutes(60);
            request.setMaxSuggestions(5);

            List<TimeSlotSuggestion> suggestions = planningBusinessService.suggestTimeSlots(request);

            assertThat(suggestions).isEmpty();
        }

        @Test
        @DisplayName("Doit respecter la limite maxSuggestions")
        void shouldRespectMaxSuggestionsLimit() {
            when(planningRepository.findPlanningsByDate(any())).thenReturn(Collections.emptyList());

            TimeSlotSuggestionRequest request = new TimeSlotSuggestionRequest();
            request.setStartDate(LocalDate.of(2026, 5, 18));
            request.setEndDate(LocalDate.of(2026, 5, 22)); // 5 jours ouvrables
            request.setDurationMinutes(30);
            request.setMaxSuggestions(2);

            List<TimeSlotSuggestion> suggestions = planningBusinessService.suggestTimeSlots(request);

            assertThat(suggestions.size()).isLessThanOrEqualTo(2);
        }
    }
}
