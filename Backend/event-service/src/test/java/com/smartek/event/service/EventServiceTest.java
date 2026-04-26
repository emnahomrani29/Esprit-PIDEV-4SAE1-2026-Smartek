package com.smartek.event.service;

import com.smartek.event.dto.EventRequest;
import com.smartek.event.dto.EventResponse;
import com.smartek.event.model.Event;
import com.smartek.event.model.EventMode;
import com.smartek.event.model.EventStatus;
import com.smartek.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService - Tests unitaires")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event event;
    private EventRequest request;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setEventId(1L);
        event.setTitle("Formation DevOps");
        event.setDescription("Sprint 3 DevOps");
        event.setLocation("Tunis");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setMaxParticipations(50);
        event.setCurrentParticipations(0);
        event.setPhysicalCapacity(50);
        event.setOnlineCapacity(0);
        event.setPhysicalRegistered(0);
        event.setOnlineRegistered(0);
        event.setPrice(BigDecimal.ZERO);
        event.setIsPaid(false);
        event.setCreatedBy(1L);
        event.setStatus(EventStatus.DRAFT);
        event.setMode(EventMode.PHYSICAL);

        request = new EventRequest();
        request.setTitle("Formation DevOps");
        request.setDescription("Sprint 3 DevOps");
        request.setLocation("Tunis");
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(2));
        request.setMaxParticipations(50);
        request.setPhysicalCapacity(50);
        request.setOnlineCapacity(0);
        request.setPrice(BigDecimal.ZERO);
        request.setIsPaid(false);
        request.setCreatedBy(1L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createEvent
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createEvent()")
    class CreateEvent {

        @Test
        @DisplayName("Doit créer un événement avec succès")
        void shouldCreateEventSuccessfully() {
            when(eventRepository.save(any(Event.class))).thenReturn(event);

            EventResponse response = eventService.createEvent(request);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Formation DevOps");
            assertThat(response.getLocation()).isEqualTo("Tunis");
            verify(eventRepository, times(1)).save(any(Event.class));
        }

        @Test
        @DisplayName("Doit lever une exception si la date de fin est avant la date de début")
        void shouldThrowWhenEndDateBeforeStartDate() {
            request.setEndDate(LocalDateTime.now().minusDays(1));

            assertThatThrownBy(() -> eventService.createEvent(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("End date must be after start date");

            verify(eventRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit utiliser PHYSICAL comme mode par défaut si mode invalide")
        void shouldDefaultToPhysicalModeWhenInvalid() {
            request.setMode("INVALID_MODE");
            when(eventRepository.save(any(Event.class))).thenReturn(event);

            EventResponse response = eventService.createEvent(request);

            assertThat(response).isNotNull();
            verify(eventRepository, times(1)).save(any(Event.class));
        }

        @Test
        @DisplayName("Doit créer un événement gratuit avec price=0")
        void shouldCreateFreeEvent() {
            request.setIsPaid(false);
            request.setPrice(BigDecimal.ZERO);
            when(eventRepository.save(any(Event.class))).thenReturn(event);

            EventResponse response = eventService.createEvent(request);

            assertThat(response.getIsPaid()).isFalse();
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getEventById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getEventById()")
    class GetEventById {

        @Test
        @DisplayName("Doit retourner l'événement correspondant à l'ID")
        void shouldReturnEventById() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            EventResponse response = eventService.getEventById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getEventId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Formation DevOps");
        }

        @Test
        @DisplayName("Doit lever RuntimeException si l'événement n'existe pas")
        void shouldThrowWhenEventNotFound() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.getEventById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found with id: 99");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllEvents
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllEvents()")
    class GetAllEvents {

        @Test
        @DisplayName("Doit retourner tous les événements")
        void shouldReturnAllEvents() {
            when(eventRepository.findAll()).thenReturn(List.of(event));

            List<EventResponse> result = eventService.getAllEvents();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Formation DevOps");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun événement n'existe")
        void shouldReturnEmptyListWhenNoEvents() {
            when(eventRepository.findAll()).thenReturn(Collections.emptyList());

            List<EventResponse> result = eventService.getAllEvents();

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUpcomingEvents
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUpcomingEvents()")
    class GetUpcomingEvents {

        @Test
        @DisplayName("Doit retourner les événements à venir")
        void shouldReturnUpcomingEvents() {
            when(eventRepository.findByStartDateAfter(any(LocalDateTime.class)))
                    .thenReturn(List.of(event));

            List<EventResponse> result = eventService.getUpcomingEvents();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStartDate()).isAfter(LocalDateTime.now().minusSeconds(1));
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun événement à venir")
        void shouldReturnEmptyWhenNoUpcomingEvents() {
            when(eventRepository.findByStartDateAfter(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            List<EventResponse> result = eventService.getUpcomingEvents();

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateEvent
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateEvent()")
    class UpdateEvent {

        @Test
        @DisplayName("Doit mettre à jour un événement existant")
        void shouldUpdateEventSuccessfully() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(event);

            EventResponse response = eventService.updateEvent(1L, request);

            assertThat(response).isNotNull();
            verify(eventRepository, times(1)).save(any(Event.class));
        }

        @Test
        @DisplayName("Doit lever une exception si l'événement à mettre à jour n'existe pas")
        void shouldThrowWhenUpdatingNonExistentEvent() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.updateEvent(99L, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found");
        }

        @Test
        @DisplayName("Doit lever une exception si les dates sont invalides lors de la mise à jour")
        void shouldThrowWhenInvalidDatesOnUpdate() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            request.setEndDate(LocalDateTime.now().minusDays(5));

            assertThatThrownBy(() -> eventService.updateEvent(1L, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("End date must be after start date");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteEvent
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteEvent()")
    class DeleteEvent {

        @Test
        @DisplayName("Doit supprimer un événement existant")
        void shouldDeleteEventSuccessfully() {
            when(eventRepository.existsById(1L)).thenReturn(true);

            eventService.deleteEvent(1L);

            verify(eventRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Doit lever RuntimeException si l'événement à supprimer n'existe pas")
        void shouldThrowWhenDeletingNonExistentEvent() {
            when(eventRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> eventService.deleteEvent(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found with id: 99");

            verify(eventRepository, never()).deleteById(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // registerParticipation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("registerParticipation()")
    class RegisterParticipation {

        @Test
        @DisplayName("Doit incrémenter le nombre de participants")
        void shouldIncrementParticipationCount() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(event);

            EventResponse response = eventService.registerParticipation(1L);

            assertThat(response).isNotNull();
            verify(eventRepository, times(1)).save(any(Event.class));
        }

        @Test
        @DisplayName("Doit lever une exception si l'événement est complet")
        void shouldThrowWhenEventIsFull() {
            event.setCurrentParticipations(50);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.registerParticipation(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event is full");
        }

        @Test
        @DisplayName("Doit lever une exception si l'événement n'existe pas")
        void shouldThrowWhenEventNotFound() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.registerParticipation(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // cancelParticipation
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("cancelParticipation()")
    class CancelParticipation {

        @Test
        @DisplayName("Doit décrémenter le nombre de participants")
        void shouldDecrementParticipationCount() {
            event.setCurrentParticipations(5);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(event);

            EventResponse response = eventService.cancelParticipation(1L);

            assertThat(response).isNotNull();
            verify(eventRepository, times(1)).save(any(Event.class));
        }

        @Test
        @DisplayName("Doit lever une exception si aucune participation à annuler")
        void shouldThrowWhenNoParticipationsToCancel() {
            event.setCurrentParticipations(0);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.cancelParticipation(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No participations to cancel");
        }
    }
}
