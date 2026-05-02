package com.smartek.planning.repository;

import com.smartek.planning.model.Planning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de repository pour PlanningRepository.
 * Teste les requêtes JPQL custom : conflits, plages de dates, statuts.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("PlanningRepository - Tests @DataJpaTest")
class PlanningRepositoryTest {

    @Autowired
    private PlanningRepository planningRepository;

    private Planning morning;
    private Planning afternoon;
    private Planning tomorrow;
    private Planning published;

    @BeforeEach
    void setUp() {
        planningRepository.deleteAll();

        morning = new Planning();
        morning.setDate(LocalDate.of(2026, 5, 15));
        morning.setStartTime(LocalTime.of(9, 0));
        morning.setEndTime(LocalTime.of(11, 0));
        morning.setTitle("Formation Spring Boot - Matin");
        morning.setEventType("TRAINING");
        morning.setColor("#3498db");
        morning.setStatus("DRAFT");
        morning.setTrainerId(10L);
        morning.setRoomId("SALLE_A");
        morning.setCurrentParticipants(0);

        afternoon = new Planning();
        afternoon.setDate(LocalDate.of(2026, 5, 15));
        afternoon.setStartTime(LocalTime.of(14, 0));
        afternoon.setEndTime(LocalTime.of(16, 0));
        afternoon.setTitle("Formation Spring Boot - Après-midi");
        afternoon.setEventType("TRAINING");
        afternoon.setColor("#2ecc71");
        afternoon.setStatus("DRAFT");
        afternoon.setTrainerId(10L);
        afternoon.setRoomId("SALLE_B");
        afternoon.setCurrentParticipants(0);

        tomorrow = new Planning();
        tomorrow.setDate(LocalDate.of(2026, 5, 16));
        tomorrow.setStartTime(LocalTime.of(10, 0));
        tomorrow.setEndTime(LocalTime.of(12, 0));
        tomorrow.setTitle("Examen Final");
        tomorrow.setEventType("EXAM");
        tomorrow.setColor("#e74c3c");
        tomorrow.setStatus("DRAFT");
        tomorrow.setTrainerId(20L);
        tomorrow.setRoomId("SALLE_C");
        tomorrow.setCurrentParticipants(0);

        published = new Planning();
        published.setDate(LocalDate.of(2026, 5, 20));
        published.setStartTime(LocalTime.of(9, 0));
        published.setEndTime(LocalTime.of(11, 0));
        published.setTitle("Session Publiée");
        published.setEventType("COURSE");
        published.setColor("#9b59b6");
        published.setStatus("PUBLISHED");
        published.setTrainerId(10L);
        published.setCurrentParticipants(5);

        planningRepository.saveAll(List.of(morning, afternoon, tomorrow, published));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findUpcomingPlannings
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findUpcomingPlannings()")
    class FindUpcomingPlannings {

        @Test
        @DisplayName("Retourne les plannings à partir d'une date donnée")
        void fromDate_returnsUpcoming() {
            List<Planning> result = planningRepository.findUpcomingPlannings(LocalDate.of(2026, 5, 16));

            assertThat(result).hasSize(2); // tomorrow + published
            assertThat(result).extracting(Planning::getTitle)
                    .containsExactlyInAnyOrder("Examen Final", "Session Publiée");
        }

        @Test
        @DisplayName("Retourne tous les plannings si date très ancienne")
        void veryOldDate_returnsAll() {
            List<Planning> result = planningRepository.findUpcomingPlannings(LocalDate.of(2020, 1, 1));
            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("Retourne liste vide si date dans le futur lointain")
        void farFutureDate_returnsEmpty() {
            List<Planning> result = planningRepository.findUpcomingPlannings(LocalDate.of(2030, 1, 1));
            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findPlanningsByDate
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findPlanningsByDate()")
    class FindPlanningsByDate {

        @Test
        @DisplayName("Retourne les plannings du 15 mai triés par heure")
        void may15_returnsTwoSortedByTime() {
            List<Planning> result = planningRepository.findPlanningsByDate(LocalDate.of(2026, 5, 15));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.get(1).getStartTime()).isEqualTo(LocalTime.of(14, 0));
        }

        @Test
        @DisplayName("Retourne liste vide pour une date sans planning")
        void dateWithNoPlanning_returnsEmpty() {
            List<Planning> result = planningRepository.findPlanningsByDate(LocalDate.of(2026, 6, 1));
            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findPlanningsByDateRange
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findPlanningsByDateRange()")
    class FindPlanningsByDateRange {

        @Test
        @DisplayName("Retourne les plannings dans la plage 15-16 mai")
        void may15to16_returnsThree() {
            List<Planning> result = planningRepository.findPlanningsByDateRange(
                    LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 16));

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Retourne liste vide pour une plage sans planning")
        void emptyRange_returnsEmpty() {
            List<Planning> result = planningRepository.findPlanningsByDateRange(
                    LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Plage d'un seul jour retourne les plannings de ce jour")
        void singleDay_returnsDayPlannings() {
            List<Planning> result = planningRepository.findPlanningsByDateRange(
                    LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 15));
            assertThat(result).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findConflictingPlannings — logique métier critique
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findConflictingPlannings() - Détection de conflits")
    class FindConflictingPlannings {

        @Test
        @DisplayName("Détecte un chevauchement partiel (9h30-10h30 vs 9h-11h)")
        void partialOverlap_detected() {
            List<Planning> conflicts = planningRepository.findConflictingPlannings(
                    LocalDate.of(2026, 5, 15),
                    LocalTime.of(9, 30),
                    LocalTime.of(10, 30));

            assertThat(conflicts).isNotEmpty();
            assertThat(conflicts).anyMatch(p -> p.getTitle().equals("Formation Spring Boot - Matin"));
        }

        @Test
        @DisplayName("Détecte un chevauchement total (8h-12h englobe 9h-11h)")
        void totalOverlap_detected() {
            List<Planning> conflicts = planningRepository.findConflictingPlannings(
                    LocalDate.of(2026, 5, 15),
                    LocalTime.of(8, 0),
                    LocalTime.of(12, 0));

            // 8h-12h chevauche morning (9h-11h) mais pas afternoon (14h-16h)
            assertThat(conflicts).hasSize(1);
            assertThat(conflicts.get(0).getTitle()).isEqualTo("Formation Spring Boot - Matin");
        }

        @Test
        @DisplayName("Aucun conflit pour un créneau libre (12h-13h)")
        void freeSlot_noConflict() {
            List<Planning> conflicts = planningRepository.findConflictingPlannings(
                    LocalDate.of(2026, 5, 15),
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0));

            assertThat(conflicts).isEmpty();
        }

        @Test
        @DisplayName("Aucun conflit sur une autre date")
        void differentDate_noConflict() {
            List<Planning> conflicts = planningRepository.findConflictingPlannings(
                    LocalDate.of(2026, 6, 1),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0));

            assertThat(conflicts).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findPublishedPlannings
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findPublishedPlannings()")
    class FindPublishedPlannings {

        @Test
        @DisplayName("Retourne uniquement les plannings PUBLISHED")
        void returnsOnlyPublished() {
            List<Planning> result = planningRepository.findPublishedPlannings();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Session Publiée");
            assertThat(result.get(0).getStatus()).isEqualTo("PUBLISHED");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByTrainerIdAndDateBetween
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByTrainerIdAndDateBetween()")
    class FindByTrainerIdAndDateBetween {

        @Test
        @DisplayName("Retourne les plannings du trainer 10 sur la semaine du 15 mai")
        void trainer10_may15to20_returnsThree() {
            List<Planning> result = planningRepository.findByTrainerIdAndDateBetween(
                    10L,
                    LocalDate.of(2026, 5, 15),
                    LocalDate.of(2026, 5, 20));

            assertThat(result).hasSize(3); // morning + afternoon + published
        }

        @Test
        @DisplayName("Retourne liste vide pour un trainer sans planning dans la période")
        void unknownTrainer_returnsEmpty() {
            List<Planning> result = planningRepository.findByTrainerIdAndDateBetween(
                    999L,
                    LocalDate.of(2026, 5, 15),
                    LocalDate.of(2026, 5, 20));

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findPlanningsByDateAndType
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findPlanningsByDateAndType()")
    class FindPlanningsByDateAndType {

        @Test
        @DisplayName("Retourne les TRAINING du 15 mai")
        void trainingOnMay15_returnsTwo() {
            List<Planning> result = planningRepository.findPlanningsByDateAndType(
                    LocalDate.of(2026, 5, 15), "TRAINING");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Retourne liste vide pour un type absent ce jour")
        void examOnMay15_returnsEmpty() {
            List<Planning> result = planningRepository.findPlanningsByDateAndType(
                    LocalDate.of(2026, 5, 15), "EXAM");

            assertThat(result).isEmpty();
        }
    }
}
