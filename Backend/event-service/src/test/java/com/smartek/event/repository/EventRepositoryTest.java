package com.smartek.event.repository;

import com.smartek.event.model.Event;
import com.smartek.event.model.EventMode;
import com.smartek.event.model.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de repository pour EventRepository.
 * Teste les méthodes dérivées Spring Data JPA.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("EventRepository - Tests @DataJpaTest")
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    private Event pastEvent;
    private Event futureEvent1;
    private Event futureEvent2;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();

        pastEvent = new Event();
        pastEvent.setTitle("Formation Passée");
        pastEvent.setLocation("Tunis");
        pastEvent.setStartDate(LocalDateTime.now().minusDays(10));
        pastEvent.setEndDate(LocalDateTime.now().minusDays(9));
        pastEvent.setMaxParticipations(50);
        pastEvent.setPhysicalCapacity(50);
        pastEvent.setOnlineCapacity(0);
        pastEvent.setPhysicalRegistered(30);
        pastEvent.setOnlineRegistered(0);
        pastEvent.setCurrentParticipations(30);
        pastEvent.setPrice(BigDecimal.ZERO);
        pastEvent.setIsPaid(false);
        pastEvent.setCreatedBy(1L);
        pastEvent.setStatus(EventStatus.COMPLETED);
        pastEvent.setMode(EventMode.PHYSICAL);

        futureEvent1 = new Event();
        futureEvent1.setTitle("Formation DevOps");
        futureEvent1.setLocation("Tunis Centre");
        futureEvent1.setStartDate(LocalDateTime.now().plusDays(5));
        futureEvent1.setEndDate(LocalDateTime.now().plusDays(6));
        futureEvent1.setMaxParticipations(100);
        futureEvent1.setPhysicalCapacity(100);
        futureEvent1.setOnlineCapacity(0);
        futureEvent1.setPhysicalRegistered(20);
        futureEvent1.setOnlineRegistered(0);
        futureEvent1.setCurrentParticipations(20);
        futureEvent1.setPrice(BigDecimal.ZERO);
        futureEvent1.setIsPaid(false);
        futureEvent1.setCreatedBy(1L);
        futureEvent1.setStatus(EventStatus.PUBLISHED);
        futureEvent1.setMode(EventMode.PHYSICAL);

        futureEvent2 = new Event();
        futureEvent2.setTitle("Conférence Cloud");
        futureEvent2.setLocation("Tunis Lac");
        futureEvent2.setStartDate(LocalDateTime.now().plusDays(15));
        futureEvent2.setEndDate(LocalDateTime.now().plusDays(16));
        futureEvent2.setMaxParticipations(200);
        futureEvent2.setPhysicalCapacity(100);
        futureEvent2.setOnlineCapacity(100);
        futureEvent2.setPhysicalRegistered(50);
        futureEvent2.setOnlineRegistered(30);
        futureEvent2.setCurrentParticipations(80);
        futureEvent2.setPrice(new BigDecimal("50.00"));
        futureEvent2.setIsPaid(true);
        futureEvent2.setCreatedBy(2L);
        futureEvent2.setStatus(EventStatus.PUBLISHED);
        futureEvent2.setMode(EventMode.HYBRID);

        eventRepository.saveAll(List.of(pastEvent, futureEvent1, futureEvent2));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByStartDateAfter
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByStartDateAfter()")
    class FindByStartDateAfter {

        @Test
        @DisplayName("Retourne uniquement les événements futurs")
        void now_returnsFutureEvents() {
            List<Event> result = eventRepository.findByStartDateAfter(LocalDateTime.now());

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Event::getTitle)
                    .containsExactlyInAnyOrder("Formation DevOps", "Conférence Cloud");
        }

        @Test
        @DisplayName("Retourne tous les événements si date très ancienne")
        void veryOldDate_returnsAll() {
            List<Event> result = eventRepository.findByStartDateAfter(
                    LocalDateTime.now().minusYears(10));
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Retourne liste vide si date dans le futur lointain")
        void farFutureDate_returnsEmpty() {
            List<Event> result = eventRepository.findByStartDateAfter(
                    LocalDateTime.now().plusYears(10));
            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByStartDateBetween
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByStartDateBetween()")
    class FindByStartDateBetween {

        @Test
        @DisplayName("Retourne les événements dans la plage de dates")
        void rangeIncludesBothFutureEvents() {
            List<Event> result = eventRepository.findByStartDateBetween(
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(20));

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Retourne uniquement l'événement dans la plage courte")
        void shortRange_returnsOne() {
            List<Event> result = eventRepository.findByStartDateBetween(
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(10));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Formation DevOps");
        }

        @Test
        @DisplayName("Retourne liste vide pour une plage sans événement")
        void emptyRange_returnsEmpty() {
            List<Event> result = eventRepository.findByStartDateBetween(
                    LocalDateTime.now().plusDays(50),
                    LocalDateTime.now().plusDays(60));
            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByLocationContainingIgnoreCase
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findByLocationContainingIgnoreCase()")
    class FindByLocationContainingIgnoreCase {

        @Test
        @DisplayName("Recherche insensible à la casse — 'tunis' trouve tous les événements")
        void caseInsensitive_findsAll() {
            List<Event> result = eventRepository.findByLocationContainingIgnoreCase("tunis");
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Recherche partielle — 'Lac' trouve la Conférence Cloud")
        void partialMatch_findsOne() {
            List<Event> result = eventRepository.findByLocationContainingIgnoreCase("Lac");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Conférence Cloud");
        }

        @Test
        @DisplayName("Retourne liste vide pour une localisation inexistante")
        void unknownLocation_returnsEmpty() {
            List<Event> result = eventRepository.findByLocationContainingIgnoreCase("Paris");
            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD de base
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CRUD de base")
    class BasicCrud {

        @Test
        @DisplayName("findAll retourne tous les événements")
        void findAll_returnsAll() {
            assertThat(eventRepository.findAll()).hasSize(3);
        }

        @Test
        @DisplayName("Sauvegarde et retrouve un événement")
        void saveAndFind() {
            Event newEvent = new Event();
            newEvent.setTitle("Hackathon 2026");
            newEvent.setLocation("Sfax");
            newEvent.setStartDate(LocalDateTime.now().plusDays(30));
            newEvent.setEndDate(LocalDateTime.now().plusDays(31));
            newEvent.setMaxParticipations(50);
            newEvent.setPhysicalCapacity(50);
            newEvent.setOnlineCapacity(0);
            newEvent.setPhysicalRegistered(0);
            newEvent.setOnlineRegistered(0);
            newEvent.setCurrentParticipations(0);
            newEvent.setPrice(BigDecimal.ZERO);
            newEvent.setIsPaid(false);
            newEvent.setCreatedBy(3L);
            newEvent.setStatus(EventStatus.DRAFT);
            newEvent.setMode(EventMode.PHYSICAL);

            Event saved = eventRepository.save(newEvent);
            assertThat(saved.getEventId()).isNotNull();
            assertThat(eventRepository.findById(saved.getEventId())).isPresent();
        }

        @Test
        @DisplayName("Supprime un événement")
        void deleteById() {
            Long id = pastEvent.getEventId();
            eventRepository.deleteById(id);
            assertThat(eventRepository.findById(id)).isEmpty();
        }
    }
}
