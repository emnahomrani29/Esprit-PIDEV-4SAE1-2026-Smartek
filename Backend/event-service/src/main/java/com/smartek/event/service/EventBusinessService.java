package com.smartek.event.service;

import com.smartek.event.dto.*;
import com.smartek.event.model.*;
import com.smartek.event.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventBusinessService {

    private final EventRepository eventRepository;
    private final EventStatusHistoryRepository statusHistoryRepository;
    private final EventRegistrationRepository registrationRepository;

    /**
     * Change le statut d'un événement avec validation des transitions
     */
    public Event changeStatus(Long eventId, EventStatusChangeRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        EventStatus currentStatus = event.getStatus();
        EventStatus newStatus = request.getNewStatus();

        // Vérifier si la transition est autorisée
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new RuntimeException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }

        // Bloquer les modifications si l'événement est terminé
        if (currentStatus == EventStatus.COMPLETED) {
            throw new RuntimeException("Cannot modify completed event");
        }

        // Sauvegarder l'historique
        EventStatusHistory history = new EventStatusHistory(
            eventId, currentStatus, newStatus, request.getChangedBy(), request.getReason()
        );
        statusHistoryRepository.save(history);

        // Mettre à jour le statut
        event.setStatus(newStatus);
        return eventRepository.save(event);
    }

    /**
     * Met à jour automatiquement le statut selon les règles métier
     */
    public void updateStatusAutomatically(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        LocalDateTime now = LocalDateTime.now();
        EventStatus currentStatus = event.getStatus();
        Long systemUserId = 0L; // ID système pour les changements automatiques

        // Si capacité atteinte → passer à FULL
        if (currentStatus == EventStatus.PUBLISHED && event.isFull()) {
            event.setStatus(EventStatus.FULL);
            saveStatusHistory(eventId, currentStatus, EventStatus.FULL, systemUserId, "Capacité maximale atteinte");
        }
        // Si date actuelle > date début → ONGOING
        else if ((currentStatus == EventStatus.PUBLISHED || currentStatus == EventStatus.FULL) 
                 && now.isAfter(event.getStartDate())) {
            event.setStatus(EventStatus.ONGOING);
            saveStatusHistory(eventId, currentStatus, EventStatus.ONGOING, systemUserId, "Événement démarré automatiquement");
        }
        // Si date fin passée → COMPLETED
        else if (currentStatus == EventStatus.ONGOING && now.isAfter(event.getEndDate())) {
            event.setStatus(EventStatus.COMPLETED);
            saveStatusHistory(eventId, currentStatus, EventStatus.COMPLETED, systemUserId, "Événement terminé automatiquement");
        }

        eventRepository.save(event);
    }

    /**
     * Duplique automatiquement un événement si les conditions sont remplies
     */
    public Optional<Event> autoDuplicate(Long eventId) {
        Event originalEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Vérifier les conditions de duplication
        if (originalEvent.getStatus() != EventStatus.FULL) {
            return Optional.empty();
        }

        long waitingListSize = registrationRepository.findWaitingListByEventId(eventId).size();
        int duplicateThreshold = originalEvent.getTotalCapacity() / 2; // Seuil: 50% de la capacité

        if (waitingListSize < duplicateThreshold) {
            return Optional.empty();
        }

        // Créer l'événement dupliqué
        Event duplicatedEvent = new Event();
        duplicatedEvent.setTitle(originalEvent.getTitle() + " - Session 2");
        duplicatedEvent.setDescription(originalEvent.getDescription());
        duplicatedEvent.setLocation(originalEvent.getLocation());
        duplicatedEvent.setPhysicalCapacity(originalEvent.getPhysicalCapacity());
        duplicatedEvent.setOnlineCapacity(originalEvent.getOnlineCapacity());
        duplicatedEvent.setMaxParticipations(originalEvent.getMaxParticipations());
        duplicatedEvent.setPrice(originalEvent.getPrice());
        duplicatedEvent.setIsPaid(originalEvent.getIsPaid());
        duplicatedEvent.setMode(originalEvent.getMode());
        duplicatedEvent.setCreatedBy(originalEvent.getCreatedBy());
        duplicatedEvent.setStatus(EventStatus.DRAFT);

        // Proposer une nouvelle date (1 semaine après l'original)
        duplicatedEvent.setStartDate(originalEvent.getStartDate().plusWeeks(1));
        duplicatedEvent.setEndDate(originalEvent.getEndDate().plusWeeks(1));

        return Optional.of(eventRepository.save(duplicatedEvent));
    }

    /**
     * Vérifie si un événement peut être dupliqué
     */
    public boolean canAutoDuplicate(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (event.getStatus() != EventStatus.FULL) {
            return false;
        }

        long waitingListSize = registrationRepository.findWaitingListByEventId(eventId).size();
        int duplicateThreshold = event.getTotalCapacity() / 2;

        return waitingListSize >= duplicateThreshold;
    }

    private void saveStatusHistory(Long eventId, EventStatus previousStatus, EventStatus newStatus, Long changedBy, String reason) {
        EventStatusHistory history = new EventStatusHistory(eventId, previousStatus, newStatus, changedBy, reason);
        statusHistoryRepository.save(history);
    }
}