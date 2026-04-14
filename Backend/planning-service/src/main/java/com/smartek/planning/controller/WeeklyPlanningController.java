package com.smartek.planning.controller;

import com.smartek.planning.dto.WeeklyPlanningRequest;
import com.smartek.planning.dto.WeeklyPlanningResponse;
import com.smartek.planning.dto.WeeklyPlanningItem;
import com.smartek.planning.service.WeeklyPlanningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/plannings/weekly")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class WeeklyPlanningController {

    private final WeeklyPlanningService weeklyPlanningService;

    /**
     * Récupère le planning hebdomadaire d'un trainer
     */
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<WeeklyPlanningResponse> getWeeklyPlanning(
            @PathVariable Long trainerId,
            @RequestParam LocalDate weekStartDate) {
        WeeklyPlanningResponse response = weeklyPlanningService.getWeeklyPlanning(trainerId, weekStartDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère tous les plannings publiés pour une semaine donnée (pour les learners)
     */
    @GetMapping("/published")
    public ResponseEntity<List<WeeklyPlanningItem>> getPublishedPlannings(
            @RequestParam LocalDate weekStartDate) {
        List<WeeklyPlanningItem> publishedItems = weeklyPlanningService.getPublishedPlannings(weekStartDate);
        return ResponseEntity.ok(publishedItems);
    }

    /**
     * Récupère tous les trainings disponibles
     */
    @GetMapping("/trainings")
    public ResponseEntity<List<Object>> getAvailableTrainings() {
        List<Object> trainings = weeklyPlanningService.getAvailableTrainings();
        return ResponseEntity.ok(trainings);
    }

    /**
     * Récupère tous les examens disponibles
     */
    @GetMapping("/exams")
    public ResponseEntity<List<Object>> getAvailableExams() {
        List<Object> exams = weeklyPlanningService.getAvailableExams();
        return ResponseEntity.ok(exams);
    }

    /**
     * Récupère tous les événements disponibles
     */
    @GetMapping("/events")
    public ResponseEntity<List<Object>> getAvailableEvents() {
        List<Object> events = weeklyPlanningService.getAvailableEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Crée ou met à jour un planning hebdomadaire
     */
    @PostMapping
    public ResponseEntity<WeeklyPlanningResponse> createOrUpdateWeeklyPlanning(
            @RequestBody WeeklyPlanningRequest request) {
        WeeklyPlanningResponse response = weeklyPlanningService.createOrUpdateWeeklyPlanning(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Publie tous les éléments d'un planning hebdomadaire
     */
    @PostMapping("/publish")
    public ResponseEntity<WeeklyPlanningResponse> publishWeeklyPlanning(
            @RequestParam Long trainerId,
            @RequestParam LocalDate weekStartDate) {
        WeeklyPlanningResponse response = weeklyPlanningService.publishWeeklyPlanning(trainerId, weekStartDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Dépublie tous les éléments d'un planning hebdomadaire
     */
    @PostMapping("/unpublish")
    public ResponseEntity<WeeklyPlanningResponse> unpublishWeeklyPlanning(
            @RequestParam Long trainerId,
            @RequestParam LocalDate weekStartDate) {
        WeeklyPlanningResponse response = weeklyPlanningService.unpublishWeeklyPlanning(trainerId, weekStartDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Inscription d'un learner à une session de planning
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerToSession(
            @RequestParam Long planningId,
            @RequestParam Long learnerId) {
        try {
            String result = weeklyPlanningService.registerToSession(planningId, learnerId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    /**
     * Désincription d'un learner d'une session de planning
     */
    @PostMapping("/unregister")
    public ResponseEntity<String> unregisterFromSession(
            @RequestParam Long planningId,
            @RequestParam Long learnerId) {
        try {
            String result = weeklyPlanningService.unregisterFromSession(planningId, learnerId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la désinscription: " + e.getMessage());
        }
    }

    /**
     * Vérifier si un learner est inscrit à une session
     */
    @GetMapping("/is-registered")
    public ResponseEntity<Boolean> isRegistered(
            @RequestParam Long planningId,
            @RequestParam Long learnerId) {
        boolean isRegistered = weeklyPlanningService.isLearnerRegistered(planningId, learnerId);
        return ResponseEntity.ok(isRegistered);
    }

    /**
     * Récupérer les inscriptions d'un learner
     */
    @GetMapping("/learner/{learnerId}/registrations")
    public ResponseEntity<List<WeeklyPlanningItem>> getLearnerRegistrations(
            @PathVariable Long learnerId,
            @RequestParam LocalDate weekStartDate) {
        List<WeeklyPlanningItem> registrations = weeklyPlanningService.getLearnerRegistrations(learnerId, weekStartDate);
        return ResponseEntity.ok(registrations);
    }
}
