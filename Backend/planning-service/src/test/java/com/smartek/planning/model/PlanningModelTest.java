package com.smartek.planning.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Planning Model - Tests")
class PlanningModelTest {

    private Planning buildValidPlanning() {
        Planning p = new Planning();
        p.setPlanningId(1L);
        p.setDate(LocalDate.of(2026, 5, 15));
        p.setStartTime(LocalTime.of(9, 0));
        p.setEndTime(LocalTime.of(11, 0));
        p.setTitle("Formation Spring Boot");
        p.setDescription("Session de formation");
        p.setEventType("TRAINING");
        p.setLocation("Salle A");
        p.setColor("#3498db");
        p.setStatus("SCHEDULED");
        p.setCurrentParticipants(0);
        return p;
    }

    @Nested
    @DisplayName("Création et valeurs")
    class Creation {

        @Test
        @DisplayName("Constructeur par défaut initialise currentParticipants à 0")
        void defaultCurrentParticipants_isZero() {
            Planning p = new Planning();
            assertThat(p.getCurrentParticipants()).isEqualTo(0);
        }

        @Test
        @DisplayName("Constructeur par défaut initialise status à SCHEDULED")
        void defaultStatus_isScheduled() {
            Planning p = new Planning();
            assertThat(p.getStatus()).isEqualTo("SCHEDULED");
        }

        @Test
        @DisplayName("Tous les champs sont correctement assignés")
        void allFields_assignedCorrectly() {
            Planning p = buildValidPlanning();

            assertThat(p.getPlanningId()).isEqualTo(1L);
            assertThat(p.getTitle()).isEqualTo("Formation Spring Boot");
            assertThat(p.getDate()).isEqualTo(LocalDate.of(2026, 5, 15));
            assertThat(p.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(p.getEndTime()).isEqualTo(LocalTime.of(11, 0));
            assertThat(p.getEventType()).isEqualTo("TRAINING");
            assertThat(p.getColor()).isEqualTo("#3498db");
        }
    }

    @Nested
    @DisplayName("Logique temporelle")
    class TemporalLogic {

        @Test
        @DisplayName("endTime après startTime - configuration valide")
        void endTimeAfterStartTime_isValid() {
            Planning p = buildValidPlanning();
            assertThat(p.getEndTime()).isAfter(p.getStartTime());
        }

        @Test
        @DisplayName("Durée calculable depuis startTime et endTime")
        void duration_calculable() {
            Planning p = buildValidPlanning();
            long minutes = java.time.Duration.between(p.getStartTime(), p.getEndTime()).toMinutes();
            assertThat(minutes).isEqualTo(120); // 2 heures
        }
    }

    @Nested
    @DisplayName("Types d'événements")
    class EventTypes {

        @Test
        @DisplayName("Tous les types d'événements sont supportés")
        void allEventTypes_supported() {
            String[] types = {"COURSE", "TRAINING", "EXAM", "MEETING", "OTHER"};
            for (String type : types) {
                Planning p = buildValidPlanning();
                p.setEventType(type);
                assertThat(p.getEventType()).isEqualTo(type);
            }
        }
    }

    @Nested
    @DisplayName("Statuts")
    class Statuses {

        @Test
        @DisplayName("Statut peut être SCHEDULED, COMPLETED ou CANCELLED")
        void allStatuses_supported() {
            Planning p = buildValidPlanning();

            p.setStatus("SCHEDULED");
            assertThat(p.getStatus()).isEqualTo("SCHEDULED");

            p.setStatus("COMPLETED");
            assertThat(p.getStatus()).isEqualTo("COMPLETED");

            p.setStatus("CANCELLED");
            assertThat(p.getStatus()).isEqualTo("CANCELLED");
        }
    }
}
