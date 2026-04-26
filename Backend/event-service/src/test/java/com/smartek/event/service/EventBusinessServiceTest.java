package com.smartek.event.service;

import com.smartek.event.dto.EventStatusChangeRequest;
import com.smartek.event.model.*;
import com.smartek.event.repository.EventRegistrationRepository;
import com.smartek.event.repository.EventRepository;
import com.smartek.event.repository.EventStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventBusinessService - Tests unitaires")
class EventBusinessServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventStatusHistoryRepository statusHistoryRepository;

    @Mock
    private EventRegistrationRepository registrationRepository;

    @InjectMocks
    private EventBusinessService eventBusinessService;

    private Event draftEvent;
    private Event publishedEvent;
    private Event fullEvent;

    @BeforeEach
    void setUp() {
        draftEvent = new Event();
        draftEvent.setEventId(1L);
        draftEvent.setTitle("Formation DevOps");
        draftEvent.setLocation("Tunis");
        draftEvent.setStartDate(LocalDateTime.now().plusDays(5));
        draftEvent.setEndDate(LocalDateTime.now().plusDays(6));
        draftEvent.setMaxParticipations(50);
        draftEvent.setCurrentParticipations(0);
        draftEvent.setPhysicalCapacity(50);
        draftEvent.setOnlineCapacity(0);
        draftEvent.setPhysicalRegistered(0);
        draftEvent.setOnlineRegistered(0);
        draftEvent.setPrice(BigDecimal.ZERO);
        draftEvent.setIsPaid(false);
        draftEvent.setCreatedBy(1L);
        draftEvent.setStatus(EventStatus.DRAFT);
        draftEvent.setMode(EventMode.PHYSICAL);

        publishedEvent = new Event();
        publishedEvent.setEventId(2L);
        publishedEvent.setTitle("Formation Spring");
        publishedEvent.setLocation("Tunis");
        publishedEvent.setStartDate(LocalDateTime.now().plusDays(5));
        publishedEvent.setEndDate(LocalDateTime.now().plusDays(6));
        publishedEvent.setMaxParticipations(50);
        publishedEvent.setCurrentParticipations(10);
        publishedEvent.setPhysicalCapacity(50);
        publishedEvent.setOnlineCapacity(0);
        publishedEvent.setPhysicalRegistered(10);
        publishedEvent.setOnlineRegistered(0);
        publishedEvent.setPrice(BigDecimal.ZERO);
        publishedEvent.setIsPaid(false);
        publishedEvent.setCreatedBy(1L);
        publishedEvent.setStatus(EventStatus.PUBLISHED);
        publishedEvent.setMode(EventMode.PHYSICAL);

        fullEvent = new Event();
        fullEvent.setEventId(3L);
        fullEvent.setTitle("Formation Full");
        fullEvent.setLocation("Tunis");
        fullEvent.setStartDate(LocalDateTime.now().plusDays(5));
        fullEvent.setEndDate(LocalDateTime.now().plusDays(6));
        fullEvent.setMaxParticipations(10);
        fullEvent.setCurrentParticipations(10);
        fullEvent.setPhysicalCapacity(10);
        fullEvent.setOnlineCapacity(0);
        fullEvent.setPhysicalRegistered(10);
        fullEvent.setOnlineRegistered(0);
        fullEvent.setPrice(BigDecimal.ZERO);
        fullEvent.setIsPaid(false);
        fullEvent.setCreatedBy(1L);
        fullEvent.setStatus(EventStatus.FULL);
        fullEvent.setMode(EventMode.PHYSICAL);

        when(statusHistoryRepository.save(any())).thenReturn(null);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // changeStatus
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("changeStatus()")
    class ChangeStatus {

        @Test
        @DisplayName("Doit publier un événement DRAFT → PUBLISHED")
        void shouldPublishDraftEvent() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(draftEvent));
            EventStatusChangeRequest request = new EventStatusChangeRequest(EventStatus.PUBLISHED, "Prêt", 1L);

            Event result = eventBusinessService.changeStatus(1L, request);

            assertThat(result.getStatus()).isEqualTo(EventStatus.PUBLISHED);
            verify(statusHistoryRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Doit lever une exception pour une transition invalide")
        void shouldThrowForInvalidTransition() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(draftEvent));
            EventStatusChangeRequest request = new EventStatusChangeRequest(EventStatus.COMPLETED, "Terminé", 1L);

            assertThatThrownBy(() -> eventBusinessService.changeStatus(1L, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("Doit lever une exception pour une transition invalide depuis COMPLETED")
        void shouldThrowWhenEventIsCompleted() {
            Event completedEvent = new Event();
            completedEvent.setEventId(4L);
            completedEvent.setStatus(EventStatus.COMPLETED);
            completedEvent.setPhysicalCapacity(10);
            completedEvent.setOnlineCapacity(0);
            completedEvent.setPhysicalRegistered(10);
            completedEvent.setOnlineRegistered(0);
            completedEvent.setMaxParticipations(10);
            completedEvent.setCurrentParticipations(10);
            when(eventRepository.findById(4L)).thenReturn(Optional.of(completedEvent));
            // COMPLETED → CANCELLED est une transition invalide (canTransitionTo retourne false)
            EventStatusChangeRequest request = new EventStatusChangeRequest(EventStatus.CANCELLED, "Annulé", 1L);

            assertThatThrownBy(() -> eventBusinessService.changeStatus(4L, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("Doit lever une exception si l'événement n'existe pas")
        void shouldThrowWhenEventNotFound() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());
            EventStatusChangeRequest request = new EventStatusChangeRequest(EventStatus.PUBLISHED, null, 1L);

            assertThatThrownBy(() -> eventBusinessService.changeStatus(99L, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateStatusAutomatically
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateStatusAutomatically()")
    class UpdateStatusAutomatically {

        @Test
        @DisplayName("Doit passer à FULL si la capacité est atteinte")
        void shouldTransitionToFullWhenCapacityReached() {
            publishedEvent.setPhysicalRegistered(50); // capacité atteinte
            when(eventRepository.findById(2L)).thenReturn(Optional.of(publishedEvent));

            eventBusinessService.updateStatusAutomatically(2L);

            assertThat(publishedEvent.getStatus()).isEqualTo(EventStatus.FULL);
        }

        @Test
        @DisplayName("Doit passer à ONGOING si la date de début est passée")
        void shouldTransitionToOngoingWhenStartDatePassed() {
            publishedEvent.setStartDate(LocalDateTime.now().minusHours(1));
            when(eventRepository.findById(2L)).thenReturn(Optional.of(publishedEvent));

            eventBusinessService.updateStatusAutomatically(2L);

            assertThat(publishedEvent.getStatus()).isEqualTo(EventStatus.ONGOING);
        }

        @Test
        @DisplayName("Doit lever une exception si l'événement n'existe pas")
        void shouldThrowWhenEventNotFound() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventBusinessService.updateStatusAutomatically(99L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // canAutoDuplicate
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("canAutoDuplicate()")
    class CanAutoDuplicate {

        @Test
        @DisplayName("Doit retourner false si l'événement n'est pas FULL")
        void shouldReturnFalseWhenNotFull() {
            when(eventRepository.findById(2L)).thenReturn(Optional.of(publishedEvent));

            boolean result = eventBusinessService.canAutoDuplicate(2L);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Doit retourner false si la liste d'attente est insuffisante")
        void shouldReturnFalseWhenWaitingListTooSmall() {
            when(eventRepository.findById(3L)).thenReturn(Optional.of(fullEvent));
            when(registrationRepository.findWaitingListByEventId(3L)).thenReturn(Collections.emptyList());

            boolean result = eventBusinessService.canAutoDuplicate(3L);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Doit retourner true si la liste d'attente dépasse le seuil")
        void shouldReturnTrueWhenWaitingListExceedsThreshold() {
            when(eventRepository.findById(3L)).thenReturn(Optional.of(fullEvent));
            // Seuil = 10/2 = 5, on met 6 en attente
            EventRegistration r1 = new EventRegistration(); r1.setStatus(RegistrationStatus.WAITING);
            EventRegistration r2 = new EventRegistration(); r2.setStatus(RegistrationStatus.WAITING);
            EventRegistration r3 = new EventRegistration(); r3.setStatus(RegistrationStatus.WAITING);
            EventRegistration r4 = new EventRegistration(); r4.setStatus(RegistrationStatus.WAITING);
            EventRegistration r5 = new EventRegistration(); r5.setStatus(RegistrationStatus.WAITING);
            EventRegistration r6 = new EventRegistration(); r6.setStatus(RegistrationStatus.WAITING);
            when(registrationRepository.findWaitingListByEventId(3L))
                    .thenReturn(List.of(r1, r2, r3, r4, r5, r6));

            boolean result = eventBusinessService.canAutoDuplicate(3L);

            assertThat(result).isTrue();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // autoDuplicate
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("autoDuplicate()")
    class AutoDuplicate {

        @Test
        @DisplayName("Doit retourner empty si l'événement n'est pas FULL")
        void shouldReturnEmptyWhenNotFull() {
            when(eventRepository.findById(2L)).thenReturn(Optional.of(publishedEvent));

            var result = eventBusinessService.autoDuplicate(2L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Doit dupliquer l'événement si les conditions sont remplies")
        void shouldDuplicateWhenConditionsMet() {
            when(eventRepository.findById(3L)).thenReturn(Optional.of(fullEvent));
            EventRegistration r1 = new EventRegistration(); r1.setStatus(RegistrationStatus.WAITING);
            EventRegistration r2 = new EventRegistration(); r2.setStatus(RegistrationStatus.WAITING);
            EventRegistration r3 = new EventRegistration(); r3.setStatus(RegistrationStatus.WAITING);
            EventRegistration r4 = new EventRegistration(); r4.setStatus(RegistrationStatus.WAITING);
            EventRegistration r5 = new EventRegistration(); r5.setStatus(RegistrationStatus.WAITING);
            EventRegistration r6 = new EventRegistration(); r6.setStatus(RegistrationStatus.WAITING);
            when(registrationRepository.findWaitingListByEventId(3L))
                    .thenReturn(List.of(r1, r2, r3, r4, r5, r6));

            var result = eventBusinessService.autoDuplicate(3L);

            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).contains("Session 2");
            assertThat(result.get().getStatus()).isEqualTo(EventStatus.DRAFT);
        }
    }
}
