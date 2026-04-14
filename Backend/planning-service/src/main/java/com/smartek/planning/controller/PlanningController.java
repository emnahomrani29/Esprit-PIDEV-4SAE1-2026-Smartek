package com.smartek.planning.controller;

import com.smartek.planning.dto.PlanningRequest;
import com.smartek.planning.dto.PlanningResponse;
import com.smartek.planning.service.PlanningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/plannings")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;

    @PostMapping
    public ResponseEntity<PlanningResponse> createPlanning(@Valid @RequestBody PlanningRequest request) {
        PlanningResponse response = planningService.createPlanning(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanningResponse> getPlanningById(@PathVariable Long id) {
        PlanningResponse response = planningService.getPlanningById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PlanningResponse>> getAllPlannings() {
        List<PlanningResponse> plannings = planningService.getAllPlannings();
        return ResponseEntity.ok(plannings);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<PlanningResponse>> getUpcomingPlannings() {
        List<PlanningResponse> plannings = planningService.getUpcomingPlannings();
        return ResponseEntity.ok(plannings);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<PlanningResponse>> getPlanningsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<PlanningResponse> plannings = planningService.getPlanningsByDate(date);
        return ResponseEntity.ok(plannings);
    }

    @GetMapping("/range")
    public ResponseEntity<List<PlanningResponse>> getPlanningsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PlanningResponse> plannings = planningService.getPlanningsByDateRange(startDate, endDate);
        return ResponseEntity.ok(plannings);
    }

    @GetMapping("/date/{date}/type/{eventType}")
    public ResponseEntity<List<PlanningResponse>> getPlanningsByDateAndType(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String eventType) {
        List<PlanningResponse> plannings = planningService.getPlanningsByDateAndType(date, eventType);
        return ResponseEntity.ok(plannings);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanningResponse> updatePlanning(
            @PathVariable Long id,
            @Valid @RequestBody PlanningRequest request) {
        PlanningResponse response = planningService.updatePlanning(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlanning(@PathVariable Long id) {
        planningService.deletePlanning(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Planning Service is running");
    }
}
