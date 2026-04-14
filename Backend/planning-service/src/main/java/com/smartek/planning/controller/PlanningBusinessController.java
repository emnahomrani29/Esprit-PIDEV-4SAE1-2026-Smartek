package com.smartek.planning.controller;

import com.smartek.planning.dto.*;
import com.smartek.planning.service.PlanningBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/plannings/business")
@RequiredArgsConstructor
public class PlanningBusinessController {

    private final PlanningBusinessService businessService;

    /**
     * Vérifie les conflits pour un créneau donné
     * POST /plannings/business/check-conflicts
     */
    @PostMapping("/check-conflicts")
    public ResponseEntity<ConflictCheckResponse> checkConflicts(@RequestBody ConflictCheckRequest request) {
        ConflictCheckResponse response = businessService.checkConflicts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Suggère des créneaux horaires optimaux
     * POST /plannings/business/suggest-slots
     */
    @PostMapping("/suggest-slots")
    public ResponseEntity<List<TimeSlotSuggestion>> suggestTimeSlots(@RequestBody TimeSlotSuggestionRequest request) {
        List<TimeSlotSuggestion> suggestions = businessService.suggestTimeSlots(request);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Calcule la charge de travail d'un formateur pour une date donnée
     * GET /plannings/business/trainer-workload/{trainerId}?date=2026-02-25
     */
    @GetMapping("/trainer-workload/{trainerId}")
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(
            @PathVariable Long trainerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TrainerWorkloadResponse response = businessService.getTrainerWorkload(trainerId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Planning Business Service is running");
    }
}
