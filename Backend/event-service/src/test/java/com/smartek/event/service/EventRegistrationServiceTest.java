package com.smartek.event.service;

import com.smartek.event.dto.EventRegistrationRequest;
import com.smartek.event.dto.EventRegistrationResponse;
import com.smartek.event.model.*;
import com.smartek.event.repository.EventRegistrationRepository;
import com.smartek.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventRegistrationService - Tests unitaires")
class EventRegistrationServiceTest {

    @Mock
    private EventRegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventBusinessService eventBusinessService;

    @InjectMocks
    private EventRegistrationService registrationService;

    private Event event;
    private EventRegistrationRequest request;
    private EventRegistration registration;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setEventId(1L);
        event.setTitle("Formation DevOps");
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
        event.setStatus(EventStatus.PUBLISHED);
        event.setMode(EventMode.PHYSICAL);

        request = new EventRegistrationRequest();
        request.setEventId(1L);
        request.setUserId(10L);
        request.setParticipationMode(EventMode.PHYSICAL);

        registration = new EventRegistration();
        registration.setRegistrationId(1L);
        registration.setEventId(1L);
        registration.setUserId(10L);
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.setPaymentStatus(PaymentStatus.PAID);
        registration.setParticipationMode(EventMode.PHYSICAL);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // register
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("Doit inscrire un utilisateur avec succès")
        void shouldRegisterUserSuccessfully() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(registrationRepository.findByEventIdAndUserId(1L, 10L)).thenReturn(Optional.empty());
            when(registrationRepository.findWaitingListByEventId(1L)).thenReturn(Collections.emptyList());
            when(registrationRepository.save(any(EventRegistration.class))).thenReturn(registration);
            doNothing().when(eventBusinessService).updateStatusAutomatically(1L);

            EventRegistrationResponse response = registrationService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
            verify(registrationRepository, times(1)).save(any(EventRegistration.class));
        }

        @Test
        @DisplayName("Doit lever une exception si l'événement n'existe pas")
        void shouldThrowWhenEventNotFound() {
            when(eventRepository.findById(99L)).thenReturn(Optional.empty());
            request.setEventId(99L);

            assertThatThrownBy(() -> registrationService.register(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Event not found");
        }

        @Test
        @DisplayName("Doit lever une exception si l'utilisateur est déjà inscrit")
        void shouldThrowWhenUserAlreadyRegistered() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(registrationRepository.findByEventIdAndUserId(1L, 10L))
                    .thenReturn(Optional.of(registration));

            assertThatThrownBy(() -> registrationService.register(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already registered");
        }

        @Test
        @DisplayName("Doit lever une exception si l'événement est terminé ou annulé")
        void shouldThrowWhenEventNotAcceptingRegistrations() {
            event.setStatus(EventStatus.COMPLETED);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(registrationRepository.findByEventIdAndUserId(1L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> registrationService.register(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not accepting registrations");
        }

        @Test
        @DisplayName("Doit mettre en liste d'attente si la capacité est atteinte")
        void shouldAddToWaitingListWhenFull() {
            event.setPhysicalCapacity(0); // capacité physique épuisée
            event.setPhysicalRegistered(0);

            EventRegistration waitingRegistration = new EventRegistration();
            waitingRegistration.setRegistrationId(2L);
            waitingRegistration.setEventId(1L);
            waitingRegistration.setUserId(10L);
            waitingRegistration.setStatus(RegistrationStatus.WAITING);
            waitingRegistration.setWaitingListPosition(1);
            waitingRegistration.setPaymentStatus(PaymentStatus.PAID);
            waitingRegistration.setParticipationMode(EventMode.PHYSICAL);

            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(registrationRepository.findByEventIdAndUserId(1L, 10L)).thenReturn(Optional.empty());
            when(registrationRepository.findWaitingListByEventId(1L)).thenReturn(Collections.emptyList());
            when(registrationRepository.save(any(EventRegistration.class))).thenReturn(waitingRegistration);
            doNothing().when(eventBusinessService).updateStatusAutomatically(1L);

            EventRegistrationResponse response = registrationService.register(request);

            assertThat(response.getStatus()).isEqualTo(RegistrationStatus.WAITING);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // cancelRegistration
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("cancelRegistration()")
    class CancelRegistration {

        @Test
        @DisplayName("Doit annuler une inscription existante")
        void shouldCancelRegistrationSuccessfully() {
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(registrationRepository.save(any(EventRegistration.class))).thenReturn(registration);
            when(registrationRepository.findWaitingListByEventId(1L)).thenReturn(Collections.emptyList());

            EventRegistrationResponse response = registrationService.cancelRegistration(1L, 10L);

            assertThat(response).isNotNull();
            verify(registrationRepository, times(1)).save(any(EventRegistration.class));
        }

        @Test
        @DisplayName("Doit lever une exception si l'inscription n'existe pas")
        void shouldThrowWhenRegistrationNotFound() {
            when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> registrationService.cancelRegistration(99L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Registration not found");
        }

        @Test
        @DisplayName("Doit lever une exception si l'utilisateur n'est pas autorisé")
        void shouldThrowWhenUserNotAuthorized() {
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));

            assertThatThrownBy(() -> registrationService.cancelRegistration(1L, 99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("Doit lever une exception si l'inscription est déjà annulée")
        void shouldThrowWhenAlreadyCancelled() {
            registration.setStatus(RegistrationStatus.CANCELLED);
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));

            assertThatThrownBy(() -> registrationService.cancelRegistration(1L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already cancelled");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // confirmPayment
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("confirmPayment()")
    class ConfirmPayment {

        @Test
        @DisplayName("Doit confirmer le paiement d'une inscription")
        void shouldConfirmPaymentSuccessfully() {
            registration.setPaymentStatus(PaymentStatus.PENDING);
            when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
            when(registrationRepository.save(any(EventRegistration.class))).thenReturn(registration);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            EventRegistrationResponse response = registrationService.confirmPayment(1L);

            assertThat(response).isNotNull();
            verify(registrationRepository, times(1)).save(any(EventRegistration.class));
        }

        @Test
        @DisplayName("Doit lever une exception si l'inscription n'existe pas")
        void shouldThrowWhenRegistrationNotFound() {
            when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> registrationService.confirmPayment(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Registration not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getEventRegistrations / getUserRegistrations
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getEventRegistrations() / getUserRegistrations()")
    class GetRegistrations {

        @Test
        @DisplayName("Doit retourner les inscriptions d'un événement")
        void shouldReturnEventRegistrations() {
            when(registrationRepository.findByEventIdOrderByRegisteredAtAsc(1L))
                    .thenReturn(List.of(registration));

            List<EventRegistration> result = registrationService.getEventRegistrations(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Doit retourner les inscriptions d'un utilisateur")
        void shouldReturnUserRegistrations() {
            when(registrationRepository.findByUserIdOrderByRegisteredAtDesc(10L))
                    .thenReturn(List.of(registration));

            List<EventRegistration> result = registrationService.getUserRegistrations(10L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Doit retourner la liste d'attente d'un événement")
        void shouldReturnWaitingList() {
            EventRegistration waiting = new EventRegistration();
            waiting.setStatus(RegistrationStatus.WAITING);
            when(registrationRepository.findWaitingListByEventId(1L)).thenReturn(List.of(waiting));

            List<EventRegistration> result = registrationService.getWaitingList(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(RegistrationStatus.WAITING);
        }
    }
}
