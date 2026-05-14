package com.smartek.planning.service;

import com.smartek.planning.dto.PlanningRequest;
import com.smartek.planning.dto.PlanningResponse;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PlanningService.
 * Couvre la logique métier : CRUD, validation des horaires, détection de conflits,
 * publication/dépublication.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlanningService - Tests unitaires")
class PlanningServiceTest {

    @Mock
    private PlanningRepository planningRepository;

    @Mock
    private PlanningNotificationService notificationService;

    @InjectMocks
    private PlanningService planningService;

    private Planning samplePlanning;
    private PlanningRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePlanning = new Planning();
        samplePlanning.setPlanningId(1L);
        samplePlanning.setDate(LocalDate.of(2026, 5, 15));
        samplePlanning.setStartTime(LocalTime.of(9, 0));
        samplePlanning.setEndTime(LocalTime.of(11, 0));
        samplePlanning.setTitle("Formation Spring Boot");
        samplePlanning.setDescription("Session de formation");
        samplePlanning.setEventType("TRAINING");
        samplePlanning.setLocation("Salle A");
        samplePlanning.setColor("#3498db");
        samplePlanning.setStatus("DRAFT");

        sampleRequest = new PlanningRequest(
                LocalDate.of(2026, 5, 15),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                "Formation Spring Boot",
                "Session de formation",
                "TRAINING",
                "Salle A",
                "#3498db"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createPlanning
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createPlanning()")
    class CreatePlanning {

        @Test
        @DisplayName("Doit créer un planning avec succès quand aucun conflit n'existe")
        void shouldCreatePlanningSuccessfully() {
            when(planningRepository.findConflictingPlannings(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(planningRepository.save(any(Planning.class))).thenReturn(samplePlanning);

            PlanningResponse result = planningService.createPlanning(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getPlanningId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Formation Spring Boot");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            verify(planningRepository).save(any(Planning.class));
        }

        @Test
        @DisplayName("Doit lever une exception si l'heure de fin est avant l'heure de début")
        void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
            PlanningRequest invalidRequest = new PlanningRequest(
                    LocalDate.of(2026, 5, 15),
                    LocalTime.of(11, 0),  // start
                    LocalTime.of(9, 0),   // end < start → invalide
                    "Titre",
                    null,
                    "TRAINING",
                    null,
                    "#fff"
            );

            assertThatThrownBy(() -> planningService.createPlanning(invalidRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("End time must be after start time");

            verify(planningRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever une exception si l'heure de début et de fin sont identiques")
        void shouldThrowExceptionWhenStartTimeEqualsEndTime() {
            PlanningRequest invalidRequest = new PlanningRequest(
                    LocalDate.of(2026, 5, 15),
                    LocalTime.of(10, 0),
                    LocalTime.of(10, 0),  // identique → invalide
                    "Titre",
                    null,
                    "TRAINING",
                    null,
                    "#fff"
            );

            assertThatThrownBy(() -> planningService.createPlanning(invalidRequest))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Doit lever une exception si un conflit de créneau existe")
        void shouldThrowExceptionWhenTimeSlotConflicts() {
            Planning conflicting = new Planning();
            conflicting.setPlanningId(2L);
            conflicting.setTitle("Autre session");

            when(planningRepository.findConflictingPlannings(any(), any(), any()))
                    .thenReturn(List.of(conflicting));

            assertThatThrownBy(() -> planningService.createPlanning(sampleRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("conflicts with existing planning");

            verify(planningRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPlanningById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPlanningById()")
    class GetPlanningById {

        @Test
        @DisplayName("Doit retourner le planning correspondant à l'ID")
        void shouldReturnPlanningById() {
            when(planningRepository.findById(1L)).thenReturn(Optional.of(samplePlanning));

            PlanningResponse result = planningService.getPlanningById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getPlanningId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Formation Spring Boot");
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le planning n'existe pas")
        void shouldThrowExceptionWhenPlanningNotFound() {
            when(planningRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> planningService.getPlanningById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllPlannings
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllPlannings()")
    class GetAllPlannings {

        @Test
        @DisplayName("Doit retourner tous les plannings")
        void shouldReturnAllPlannings() {
            when(planningRepository.findAll()).thenReturn(List.of(samplePlanning));

            List<PlanningResponse> result = planningService.getAllPlannings();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun planning n'existe")
        void shouldReturnEmptyListWhenNoPlannings() {
            when(planningRepository.findAll()).thenReturn(Collections.emptyList());

            List<PlanningResponse> result = planningService.getAllPlannings();

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUpcomingPlannings
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUpcomingPlannings()")
    class GetUpcomingPlannings {

        @Test
        @DisplayName("Doit retourner les plannings à venir")
        void shouldReturnUpcomingPlannings() {
            when(planningRepository.findUpcomingPlannings(any(LocalDate.class)))
                    .thenReturn(List.of(samplePlanning));

            List<PlanningResponse> result = planningService.getUpcomingPlannings();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDate()).isAfterOrEqualTo(LocalDate.now());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPlanningsByDateRange - logique métier
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPlanningsByDateRange() - Validation des dates")
    class GetPlanningsByDateRange {

        @Test
        @DisplayName("Doit retourner les plannings dans la plage de dates")
        void shouldReturnPlanningsInDateRange() {
            LocalDate start = LocalDate.of(2026, 5, 1);
            LocalDate end = LocalDate.of(2026, 5, 31);
            when(planningRepository.findPlanningsByDateRange(start, end))
                    .thenReturn(List.of(samplePlanning));

            List<PlanningResponse> result = planningService.getPlanningsByDateRange(start, end);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Doit lever une exception si la date de fin est avant la date de début")
        void shouldThrowExceptionWhenEndDateBeforeStartDate() {
            LocalDate start = LocalDate.of(2026, 5, 31);
            LocalDate end = LocalDate.of(2026, 5, 1); // end < start

            assertThatThrownBy(() -> planningService.getPlanningsByDateRange(start, end))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("End date cannot be before start date");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deletePlanning
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deletePlanning()")
    class DeletePlanning {

        @Test
        @DisplayName("Doit supprimer un planning existant")
        void shouldDeletePlanningSuccessfully() {
            when(planningRepository.existsById(1L)).thenReturn(true);

            planningService.deletePlanning(1L);

            verify(planningRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le planning à supprimer n'existe pas")
        void shouldThrowExceptionWhenDeletingNonExistentPlanning() {
            when(planningRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> planningService.deletePlanning(99L))
                    .isInstanceOf(RuntimeException.class);

            verify(planningRepository, never()).deleteById(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // publishPlanning - logique métier complexe
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("publishPlanning() - Logique métier")
    class PublishPlanning {

        @Test
        @DisplayName("Doit passer le statut à PUBLISHED et notifier les learners")
        void shouldPublishPlanningAndNotifyLearners() {
            when(planningRepository.findById(1L)).thenReturn(Optional.of(samplePlanning));
            when(planningRepository.save(any(Planning.class))).thenAnswer(inv -> inv.getArgument(0));

            PlanningResponse result = planningService.publishPlanning(1L);

            assertThat(result.getStatus()).isEqualTo("PUBLISHED");
            verify(notificationService).notifyLearnersForPublishedPlanning(any(Planning.class));
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le planning à publier n'existe pas")
        void shouldThrowExceptionWhenPublishingNonExistentPlanning() {
            when(planningRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> planningService.publishPlanning(99L))
                    .isInstanceOf(RuntimeException.class);

            verify(notificationService, never()).notifyLearnersForPublishedPlanning(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // unpublishPlanning
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("unpublishPlanning()")
    class UnpublishPlanning {

        @Test
        @DisplayName("Doit repasser le statut à DRAFT")
        void shouldUnpublishPlanning() {
            samplePlanning.setStatus("PUBLISHED");
            when(planningRepository.findById(1L)).thenReturn(Optional.of(samplePlanning));
            when(planningRepository.save(any(Planning.class))).thenAnswer(inv -> inv.getArgument(0));

            PlanningResponse result = planningService.unpublishPlanning(1L);

            assertThat(result.getStatus()).isEqualTo("DRAFT");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPublishedPlannings
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPublishedPlannings()")
    class GetPublishedPlannings {

        @Test
        @DisplayName("Doit retourner uniquement les plannings publiés")
        void shouldReturnOnlyPublishedPlannings() {
            samplePlanning.setStatus("PUBLISHED");
            when(planningRepository.findPublishedPlannings()).thenReturn(List.of(samplePlanning));

            List<PlanningResponse> result = planningService.getPublishedPlannings();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("PUBLISHED");
        }
    }
}
