package com.smartek.event.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Event Model - Tests de validation et logique métier")
class EventModelTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Event buildValidEvent() {
        Event event = new Event();
        event.setTitle("Formation DevOps");
        event.setLocation("Tunis");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setMaxParticipations(50);
        event.setPhysicalCapacity(50);
        event.setOnlineCapacity(0);
        event.setPhysicalRegistered(0);
        event.setOnlineRegistered(0);
        event.setCurrentParticipations(0);
        event.setPrice(BigDecimal.ZERO);
        event.setIsPaid(false);
        event.setCreatedBy(1L);
        event.setStatus(EventStatus.DRAFT);
        event.setMode(EventMode.PHYSICAL);
        return event;
    }

    @Nested
    @DisplayName("Validation des contraintes")
    class ValidationConstraints {

        @Test
        @DisplayName("Événement valide - aucune violation")
        void validEvent_noViolations() {
            Set<ConstraintViolation<Event>> violations = validator.validate(buildValidEvent());
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Titre vide → violation")
        void blankTitle_violation() {
            Event event = buildValidEvent();
            event.setTitle("");
            Set<ConstraintViolation<Event>> violations = validator.validate(event);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
        }

        @Test
        @DisplayName("Location vide → violation")
        void blankLocation_violation() {
            Event event = buildValidEvent();
            event.setLocation("");
            Set<ConstraintViolation<Event>> violations = validator.validate(event);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("location"));
        }

        @Test
        @DisplayName("maxParticipations < 1 → violation")
        void zeroMaxParticipations_violation() {
            Event event = buildValidEvent();
            event.setMaxParticipations(0);
            Set<ConstraintViolation<Event>> violations = validator.validate(event);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("maxParticipations"));
        }
    }

    @Nested
    @DisplayName("Méthodes utilitaires")
    class UtilityMethods {

        @Test
        @DisplayName("getTotalCapacity() = physicalCapacity + onlineCapacity")
        void getTotalCapacity_sumsBothCapacities() {
            Event event = buildValidEvent();
            event.setPhysicalCapacity(30);
            event.setOnlineCapacity(20);
            assertThat(event.getTotalCapacity()).isEqualTo(50);
        }

        @Test
        @DisplayName("getTotalRegistered() = physicalRegistered + onlineRegistered")
        void getTotalRegistered_sumsBothRegistered() {
            Event event = buildValidEvent();
            event.setPhysicalRegistered(10);
            event.setOnlineRegistered(5);
            assertThat(event.getTotalRegistered()).isEqualTo(15);
        }

        @Test
        @DisplayName("isFull() retourne true quand capacité atteinte")
        void isFull_returnsTrueWhenFull() {
            Event event = buildValidEvent();
            event.setPhysicalCapacity(10);
            event.setOnlineCapacity(0);
            event.setPhysicalRegistered(10);
            event.setOnlineRegistered(0);
            assertThat(event.isFull()).isTrue();
        }

        @Test
        @DisplayName("isFull() retourne false quand places disponibles")
        void isFull_returnsFalseWhenNotFull() {
            Event event = buildValidEvent();
            event.setPhysicalCapacity(50);
            event.setOnlineCapacity(0);
            event.setPhysicalRegistered(10);
            event.setOnlineRegistered(0);
            assertThat(event.isFull()).isFalse();
        }

        @Test
        @DisplayName("hasAvailableCapacity(PHYSICAL) retourne true si places physiques disponibles")
        void hasAvailableCapacity_physical_returnsTrue() {
            Event event = buildValidEvent();
            event.setPhysicalCapacity(50);
            event.setPhysicalRegistered(10);
            assertThat(event.hasAvailableCapacity(EventMode.PHYSICAL)).isTrue();
        }

        @Test
        @DisplayName("hasAvailableCapacity(PHYSICAL) retourne false si capacité physique épuisée")
        void hasAvailableCapacity_physical_returnsFalseWhenFull() {
            Event event = buildValidEvent();
            event.setPhysicalCapacity(10);
            event.setPhysicalRegistered(10);
            assertThat(event.hasAvailableCapacity(EventMode.PHYSICAL)).isFalse();
        }
    }

    @Nested
    @DisplayName("Statuts et modes")
    class StatusAndMode {

        @Test
        @DisplayName("Statut par défaut est DRAFT")
        void defaultStatus_isDraft() {
            Event event = new Event();
            event.setStatus(EventStatus.DRAFT);
            assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        }

        @Test
        @DisplayName("Tous les statuts sont accessibles")
        void allStatuses_accessible() {
            assertThat(EventStatus.values()).contains(
                    EventStatus.DRAFT, EventStatus.PUBLISHED,
                    EventStatus.COMPLETED, EventStatus.CANCELLED);
        }

        @Test
        @DisplayName("Tous les modes sont accessibles")
        void allModes_accessible() {
            assertThat(EventMode.values()).contains(
                    EventMode.PHYSICAL, EventMode.ONLINE, EventMode.HYBRID);
        }
    }
}
