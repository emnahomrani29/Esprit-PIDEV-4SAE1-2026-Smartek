package com.smartek.event.service;

import com.smartek.event.dto.EventRequest;
import com.smartek.event.dto.EventResponse;
import com.smartek.event.model.Event;
import com.smartek.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        validateEventDates(request.getStartDate(), request.getEndDate());
        
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setMaxParticipations(request.getMaxParticipations());
        event.setCurrentParticipations(0);
        
        // Nouveaux champs
        event.setPhysicalCapacity(request.getPhysicalCapacity() != null ? request.getPhysicalCapacity() : 0);
        event.setOnlineCapacity(request.getOnlineCapacity() != null ? request.getOnlineCapacity() : 0);
        event.setPhysicalRegistered(0);
        event.setOnlineRegistered(0);
        
        // Mode et paiement
        if (request.getMode() != null) {
            try {
                event.setMode(com.smartek.event.model.EventMode.valueOf(request.getMode()));
            } catch (IllegalArgumentException e) {
                event.setMode(com.smartek.event.model.EventMode.PHYSICAL);
            }
        }
        
        event.setPrice(request.getPrice() != null ? request.getPrice() : java.math.BigDecimal.ZERO);
        event.setIsPaid(request.getIsPaid() != null ? request.getIsPaid() : false);
        event.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : 1L);
        
        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents() {
        return eventRepository.findByStartDateAfter(LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        validateEventDates(request.getStartDate(), request.getEndDate());
        
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setMaxParticipations(request.getMaxParticipations());
        
        // Nouveaux champs
        if (request.getPhysicalCapacity() != null) {
            event.setPhysicalCapacity(request.getPhysicalCapacity());
        }
        if (request.getOnlineCapacity() != null) {
            event.setOnlineCapacity(request.getOnlineCapacity());
        }
        
        // Mode et paiement
        if (request.getMode() != null) {
            try {
                event.setMode(com.smartek.event.model.EventMode.valueOf(request.getMode()));
            } catch (IllegalArgumentException e) {
                // Garder le mode existant si invalide
            }
        }
        
        if (request.getPrice() != null) {
            event.setPrice(request.getPrice());
        }
        if (request.getIsPaid() != null) {
            event.setIsPaid(request.getIsPaid());
        }
        
        Event updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Transactional
    public EventResponse registerParticipation(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        if (event.getCurrentParticipations() >= event.getMaxParticipations()) {
            throw new RuntimeException("Event is full");
        }
        
        event.setCurrentParticipations(event.getCurrentParticipations() + 1);
        Event updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    @Transactional
    public EventResponse cancelParticipation(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        if (event.getCurrentParticipations() <= 0) {
            throw new RuntimeException("No participations to cancel");
        }
        
        event.setCurrentParticipations(event.getCurrentParticipations() - 1);
        Event updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    private EventResponse mapToResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());
        response.setLocation(event.getLocation());
        
        // Capacités hybrides
        response.setPhysicalCapacity(event.getPhysicalCapacity());
        response.setOnlineCapacity(event.getOnlineCapacity());
        response.setPhysicalRegistered(event.getPhysicalRegistered());
        response.setOnlineRegistered(event.getOnlineRegistered());
        
        // Anciens champs pour compatibilité
        response.setMaxParticipations(event.getMaxParticipations());
        response.setCurrentParticipations(event.getCurrentParticipations());
        
        // Workflow et métadonnées
        response.setStatus(event.getStatus() != null ? event.getStatus().name() : "DRAFT");
        response.setMode(event.getMode() != null ? event.getMode().name() : "PHYSICAL");
        response.setPrice(event.getPrice());
        response.setIsPaid(event.getIsPaid());
        response.setCreatedBy(event.getCreatedBy());
        
        response.setIsAvailable(event.getCurrentParticipations() < event.getMaxParticipations());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        return response;
    }

    private void validateEventDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date must be after start date");
        }
    }
}
