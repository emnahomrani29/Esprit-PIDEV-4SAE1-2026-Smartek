package com.smartek.event.controller;

import com.smartek.event.dto.*;
import com.smartek.event.model.Event;
import com.smartek.event.model.EventRegistration;
import com.smartek.event.service.EventAnalyticsService;
import com.smartek.event.service.EventBusinessService;
import com.smartek.event.service.EventRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/events/business")
@RequiredArgsConstructor
public class EventBusinessController {

    private final EventBusinessService eventBusinessService;
    private final EventRegistrationService registrationService;
    private final EventAnalyticsService analyticsService;

    /**
     * Change le statut d'un événement
     * POST /events/business/{eventId}/status
     */
    @PostMapping("/{eventId}/status")
    public ResponseEntity<Event> changeEventStatus(
            @PathVariable Long eventId,
            @Valid @RequestBody EventStatusChangeRequest request) {
        Event updatedEvent = eventBusinessService.changeStatus(eventId, request);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Met à jour automatiquement le statut d'un événement
     * POST /events/business/{eventId}/auto-update-status
     */
    @PostMapping("/{eventId}/auto-update-status")
    public ResponseEntity<String> autoUpdateStatus(@PathVariable Long eventId) {
        eventBusinessService.updateStatusAutomatically(eventId);
        return ResponseEntity.ok("Status updated automatically");
    }

    /**
     * Duplique automatiquement un événement
     * POST /events/business/{eventId}/auto-duplicate
     */
    @PostMapping("/{eventId}/auto-duplicate")
    public ResponseEntity<Event> autoDuplicateEvent(@PathVariable Long eventId) {
        Optional<Event> duplicatedEvent = eventBusinessService.autoDuplicate(eventId);
        if (duplicatedEvent.isPresent()) {
            return ResponseEntity.ok(duplicatedEvent.get());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Vérifie si un événement peut être dupliqué
     * GET /events/business/{eventId}/can-duplicate
     */
    @GetMapping("/{eventId}/can-duplicate")
    public ResponseEntity<Boolean> canAutoDuplicate(@PathVariable Long eventId) {
        boolean canDuplicate = eventBusinessService.canAutoDuplicate(eventId);
        return ResponseEntity.ok(canDuplicate);
    }

    /**
     * Inscrit un utilisateur à un événement
     * POST /events/business/register
     */
    @PostMapping("/register")
    public ResponseEntity<EventRegistrationResponse> registerForEvent(
            @Valid @RequestBody EventRegistrationRequest request) {
        EventRegistrationResponse response = registrationService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Annule une inscription
     * DELETE /events/business/registrations/{registrationId}
     */
    @DeleteMapping("/registrations/{registrationId}")
    public ResponseEntity<EventRegistrationResponse> cancelRegistration(
            @PathVariable Long registrationId,
            @RequestParam Long userId) {
        EventRegistrationResponse response = registrationService.cancelRegistration(registrationId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirme le paiement d'une inscription
     * POST /events/business/registrations/{registrationId}/confirm-payment
     */
    @PostMapping("/registrations/{registrationId}/confirm-payment")
    public ResponseEntity<EventRegistrationResponse> confirmPayment(@PathVariable Long registrationId) {
        EventRegistrationResponse response = registrationService.confirmPayment(registrationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les inscriptions d'un événement
     * GET /events/business/{eventId}/registrations
     */
    @GetMapping("/{eventId}/registrations")
    public ResponseEntity<List<EventRegistration>> getEventRegistrations(@PathVariable Long eventId) {
        List<EventRegistration> registrations = registrationService.getEventRegistrations(eventId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Récupère les inscriptions d'un utilisateur
     * GET /events/business/user/{userId}/registrations
     */
    @GetMapping("/user/{userId}/registrations")
    public ResponseEntity<List<EventRegistration>> getUserRegistrations(@PathVariable Long userId) {
        List<EventRegistration> registrations = registrationService.getUserRegistrations(userId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Récupère la liste d'attente d'un événement
     * GET /events/business/{eventId}/waiting-list
     */
    @GetMapping("/{eventId}/waiting-list")
    public ResponseEntity<List<EventRegistration>> getWaitingList(@PathVariable Long eventId) {
        List<EventRegistration> waitingList = registrationService.getWaitingList(eventId);
        return ResponseEntity.ok(waitingList);
    }

    /**
     * Récupère les analytics d'un événement
     * GET /events/business/{eventId}/analytics
     */
    @GetMapping("/{eventId}/analytics")
    public ResponseEntity<EventAnalyticsResponse> getEventAnalytics(@PathVariable Long eventId) {
        EventAnalyticsResponse analytics = analyticsService.getAnalytics(eventId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Calcule les revenus d'un événement
     * GET /events/business/{eventId}/revenue
     */
    @GetMapping("/{eventId}/revenue")
    public ResponseEntity<EventRevenueResponse> calculateRevenue(@PathVariable Long eventId) {
        EventRevenueResponse revenue = analyticsService.calculateRevenue(eventId);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Event Business Service is running");
    }
}