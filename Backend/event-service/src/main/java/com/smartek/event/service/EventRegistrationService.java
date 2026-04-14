package com.smartek.event.service;

import com.smartek.event.dto.EventRegistrationRequest;
import com.smartek.event.dto.EventRegistrationResponse;
import com.smartek.event.model.*;
import com.smartek.event.repository.EventRegistrationRepository;
import com.smartek.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventBusinessService eventBusinessService;

    /**
     * Inscrit un utilisateur à un événement
     */
    public EventRegistrationResponse register(EventRegistrationRequest request) {
        // Vérifier si l'événement existe
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + request.getEventId()));

        // Vérifier si l'utilisateur est déjà inscrit
        Optional<EventRegistration> existingRegistration = 
                registrationRepository.findByEventIdAndUserId(request.getEventId(), request.getUserId());
        
        if (existingRegistration.isPresent()) {
            EventRegistration existing = existingRegistration.get();
            if (!existing.isCancelled()) {
                throw new RuntimeException("User is already registered for this event");
            }
            // Si l'inscription était annulée, on peut la réactiver
            return reactivateRegistration(existing, request.getParticipationMode());
        }

        // Vérifier si l'événement accepte encore des inscriptions
        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new RuntimeException("Event is not accepting registrations");
        }

        // Créer la nouvelle inscription
        EventRegistration registration = new EventRegistration();
        registration.setEventId(request.getEventId());
        registration.setUserId(request.getUserId());
        registration.setParticipationMode(request.getParticipationMode());

        // Déterminer le statut selon la capacité disponible
        boolean hasCapacity = event.hasAvailableCapacity(request.getParticipationMode());
        
        if (hasCapacity) {
            registration.setStatus(RegistrationStatus.CONFIRMED);
            updateEventCapacity(event, request.getParticipationMode(), 1);
        } else {
            registration.setStatus(RegistrationStatus.WAITING);
            int waitingPosition = getNextWaitingPosition(request.getEventId());
            registration.setWaitingListPosition(waitingPosition);
        }

        // Gérer le paiement
        if (event.getIsPaid()) {
            registration.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            registration.setPaymentStatus(PaymentStatus.PAID); // Événement gratuit
        }

        registration = registrationRepository.save(registration);

        // Mettre à jour le statut de l'événement si nécessaire
        eventBusinessService.updateStatusAutomatically(request.getEventId());

        return buildRegistrationResponse(registration, event);
    }

    /**
     * Annule une inscription
     */
    public EventRegistrationResponse cancelRegistration(Long registrationId, Long userId) {
        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        // Vérifier que l'utilisateur peut annuler cette inscription
        if (!registration.getUserId().equals(userId)) {
            throw new RuntimeException("User not authorized to cancel this registration");
        }

        if (registration.isCancelled()) {
            throw new RuntimeException("Registration is already cancelled");
        }

        Event event = eventRepository.findById(registration.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Marquer comme annulé
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setWaitingListPosition(null);
        registrationRepository.save(registration);

        // Si c'était une inscription confirmée, libérer la place
        if (registration.isConfirmed()) {
            updateEventCapacity(event, registration.getParticipationMode(), -1);
            
            // Promouvoir le premier en liste d'attente
            promoteFromWaitingList(registration.getEventId(), registration.getParticipationMode());
        }

        return buildRegistrationResponse(registration, event);
    }

    /**
     * Confirme le paiement d'une inscription
     */
    public EventRegistrationResponse confirmPayment(Long registrationId) {
        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        registration.setPaymentStatus(PaymentStatus.PAID);
        registration = registrationRepository.save(registration);

        Event event = eventRepository.findById(registration.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return buildRegistrationResponse(registration, event);
    }

    /**
     * Récupère les inscriptions d'un événement
     */
    public List<EventRegistration> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventIdOrderByRegisteredAtAsc(eventId);
    }

    /**
     * Récupère la liste d'attente d'un événement
     */
    public List<EventRegistration> getWaitingList(Long eventId) {
        return registrationRepository.findWaitingListByEventId(eventId);
    }

    /**
     * Récupère les inscriptions d'un utilisateur
     */
    public List<EventRegistration> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserIdOrderByRegisteredAtDesc(userId);
    }

    private EventRegistrationResponse reactivateRegistration(EventRegistration registration, EventMode newMode) {
        Event event = eventRepository.findById(registration.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        registration.setParticipationMode(newMode);
        
        boolean hasCapacity = event.hasAvailableCapacity(newMode);
        if (hasCapacity) {
            registration.setStatus(RegistrationStatus.CONFIRMED);
            registration.setWaitingListPosition(null);
            updateEventCapacity(event, newMode, 1);
        } else {
            registration.setStatus(RegistrationStatus.WAITING);
            int waitingPosition = getNextWaitingPosition(registration.getEventId());
            registration.setWaitingListPosition(waitingPosition);
        }

        registration = registrationRepository.save(registration);
        return buildRegistrationResponse(registration, event);
    }

    private void promoteFromWaitingList(Long eventId, EventMode mode) {
        List<EventRegistration> waitingList = registrationRepository.findWaitingListByEventId(eventId);
        
        // Chercher le premier en attente pour ce mode de participation
        Optional<EventRegistration> nextInLine = waitingList.stream()
                .filter(r -> r.getParticipationMode() == mode)
                .findFirst();

        if (nextInLine.isPresent()) {
            EventRegistration registration = nextInLine.get();
            registration.setStatus(RegistrationStatus.CONFIRMED);
            registration.setWaitingListPosition(null);
            registrationRepository.save(registration);

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            updateEventCapacity(event, mode, 1);

            // TODO: Envoyer notification à l'utilisateur promu
        }
    }

    private void updateEventCapacity(Event event, EventMode mode, int delta) {
        if (mode == EventMode.PHYSICAL) {
            event.setPhysicalRegistered(event.getPhysicalRegistered() + delta);
        } else if (mode == EventMode.ONLINE) {
            event.setOnlineRegistered(event.getOnlineRegistered() + delta);
        }
        
        // Mettre à jour aussi les anciens champs pour compatibilité
        event.setCurrentParticipations(event.getTotalRegistered());
        eventRepository.save(event);
    }

    private int getNextWaitingPosition(Long eventId) {
        List<EventRegistration> waitingList = registrationRepository.findWaitingListByEventId(eventId);
        return waitingList.size() + 1;
    }

    private EventRegistrationResponse buildRegistrationResponse(EventRegistration registration, Event event) {
        EventRegistrationResponse response = new EventRegistrationResponse();
        response.setRegistrationId(registration.getRegistrationId());
        response.setEventId(registration.getEventId());
        response.setUserId(registration.getUserId());
        response.setStatus(registration.getStatus());
        response.setPaymentStatus(registration.getPaymentStatus());
        response.setParticipationMode(registration.getParticipationMode());
        response.setRegisteredAt(registration.getRegisteredAt());
        response.setWaitingListPosition(registration.getWaitingListPosition());

        // Message informatif
        String message = switch (registration.getStatus()) {
            case CONFIRMED -> "Inscription confirmée avec succès";
            case WAITING -> "Ajouté à la liste d'attente (position " + registration.getWaitingListPosition() + ")";
            case CANCELLED -> "Inscription annulée";
        };
        response.setMessage(message);

        return response;
    }
}