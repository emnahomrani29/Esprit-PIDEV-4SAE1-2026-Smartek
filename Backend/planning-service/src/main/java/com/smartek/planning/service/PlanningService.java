package com.smartek.planning.service;

import com.smartek.planning.dto.PlanningRequest;
import com.smartek.planning.dto.PlanningResponse;
import com.smartek.planning.model.Planning;
import com.smartek.planning.repository.PlanningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanningService {

    private final PlanningRepository planningRepository;

    public PlanningResponse createPlanning(PlanningRequest request) {
        validateTimes(request.getStartTime(), request.getEndTime());
        checkForConflicts(null, request.getDate(), request.getStartTime(), request.getEndTime());
        
        Planning planning = new Planning();
        planning.setDate(request.getDate());
        planning.setStartTime(request.getStartTime());
        planning.setEndTime(request.getEndTime());
        planning.setTitle(request.getTitle());
        planning.setDescription(request.getDescription());
        planning.setEventType(request.getEventType());
        planning.setLocation(request.getLocation());
        planning.setColor(request.getColor());
        
        Planning savedPlanning = planningRepository.save(planning);
        return mapToResponse(savedPlanning);
    }

    public PlanningResponse getPlanningById(Long id) {
        Planning planning = planningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planning not found with id: " + id));
        return mapToResponse(planning);
    }

    public List<PlanningResponse> getAllPlannings() {
        return planningRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanningResponse> getUpcomingPlannings() {
        LocalDate today = LocalDate.now();
        return planningRepository.findUpcomingPlannings(today).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanningResponse> getPlanningsByDate(LocalDate date) {
        return planningRepository.findPlanningsByDate(date).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanningResponse> getPlanningsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }
        return planningRepository.findPlanningsByDateRange(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PlanningResponse> getPlanningsByDateAndType(LocalDate date, String eventType) {
        return planningRepository.findPlanningsByDateAndType(date, eventType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PlanningResponse updatePlanning(Long id, PlanningRequest request) {
        validateTimes(request.getStartTime(), request.getEndTime());
        
        Planning planning = planningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planning not found with id: " + id));
        
        checkForConflicts(id, request.getDate(), request.getStartTime(), request.getEndTime());
        
        planning.setDate(request.getDate());
        planning.setStartTime(request.getStartTime());
        planning.setEndTime(request.getEndTime());
        planning.setTitle(request.getTitle());
        planning.setDescription(request.getDescription());
        planning.setEventType(request.getEventType());
        planning.setLocation(request.getLocation());
        planning.setColor(request.getColor());
        
        Planning updatedPlanning = planningRepository.save(planning);
        return mapToResponse(updatedPlanning);
    }

    public void deletePlanning(Long id) {
        if (!planningRepository.existsById(id)) {
            throw new RuntimeException("Planning not found with id: " + id);
        }
        planningRepository.deleteById(id);
    }

    private void validateTimes(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new RuntimeException("End time must be after start time");
        }
    }

    private void checkForConflicts(Long excludeId, LocalDate date, java.time.LocalTime startTime, java.time.LocalTime endTime) {
        List<Planning> conflicts = planningRepository.findConflictingPlannings(date, startTime, endTime);
        
        if (excludeId != null) {
            conflicts = conflicts.stream()
                    .filter(p -> !p.getPlanningId().equals(excludeId))
                    .collect(Collectors.toList());
        }
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Time slot conflicts with existing planning: " + conflicts.get(0).getTitle());
        }
    }

    private PlanningResponse mapToResponse(Planning planning) {
        return new PlanningResponse(
                planning.getPlanningId(),
                planning.getDate(),
                planning.getStartTime(),
                planning.getEndTime(),
                planning.getTitle(),
                planning.getDescription(),
                planning.getEventType(),
                planning.getLocation(),
                planning.getColor()
        );
    }
}
