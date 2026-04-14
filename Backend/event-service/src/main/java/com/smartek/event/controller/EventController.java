package com.smartek.event.controller;

import com.smartek.event.dto.EventRequest;
import com.smartek.event.dto.EventResponse;
import com.smartek.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponse>> getUpcomingEvents() {
        List<EventResponse> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.updateEvent(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<EventResponse> registerParticipation(@PathVariable Long id) {
        EventResponse response = eventService.registerParticipation(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EventResponse> cancelParticipation(@PathVariable Long id) {
        EventResponse response = eventService.cancelParticipation(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Event Service is running");
    }
}
