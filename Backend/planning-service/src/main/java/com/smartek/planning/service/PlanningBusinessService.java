package com.smartek.planning.service;

import com.smartek.planning.dto.*;
import com.smartek.planning.model.Planning;
import com.smartek.planning.repository.PlanningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanningBusinessService {

    private final PlanningRepository planningRepository;
    private static final int MAX_DAILY_HOURS = 8;
    private static final LocalTime WORK_START = LocalTime.of(8, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    /**
     * Vérifie les conflits pour un créneau donné
     */
    public ConflictCheckResponse checkConflicts(ConflictCheckRequest request) {
        List<ConflictCheckResponse.ConflictDetail> conflicts = new ArrayList<>();
        
        // Récupérer tous les plannings pour la date donnée
        List<Planning> existingPlannings = planningRepository.findPlanningsByDate(request.getDate());
        
        // Exclure le planning en cours de modification si nécessaire
        if (request.getExcludePlanningId() != null) {
            existingPlannings = existingPlannings.stream()
                    .filter(p -> !p.getPlanningId().equals(request.getExcludePlanningId()))
                    .collect(Collectors.toList());
        }
        
        for (Planning existing : existingPlannings) {
            // Vérifier chevauchement de temps
            if (timesOverlap(request.getStartTime(), request.getEndTime(), 
                           existing.getStartTime(), existing.getEndTime())) {
                
                // Conflit de formateur
                if (request.getTrainerId() != null && 
                    request.getTrainerId().equals(existing.getTrainerId())) {
                    conflicts.add(new ConflictCheckResponse.ConflictDetail(
                        "TRAINER",
                        "Le formateur est déjà occupé sur ce créneau",
                        existing.getPlanningId(),
                        existing.getTitle()
                    ));
                }
                
                // Conflit de salle
                if (request.getRoomId() != null && 
                    request.getRoomId().equals(existing.getRoomId())) {
                    conflicts.add(new ConflictCheckResponse.ConflictDetail(
                        "ROOM",
                        "La salle est déjà réservée sur ce créneau",
                        existing.getPlanningId(),
                        existing.getTitle()
                    ));
                }
            }
        }
        
        return new ConflictCheckResponse(!conflicts.isEmpty(), conflicts);
    }

    /**
     * Suggère des créneaux horaires optimaux
     */
    public List<TimeSlotSuggestion> suggestTimeSlots(TimeSlotSuggestionRequest request) {
        List<TimeSlotSuggestion> suggestions = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();
        
        while (currentDate.isBefore(request.getEndDate().plusDays(1)) && 
               suggestions.size() < request.getMaxSuggestions()) {
            
            // Ignorer les week-ends
            if (currentDate.getDayOfWeek().getValue() >= 6) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            List<Planning> dayPlannings = planningRepository.findPlanningsByDate(currentDate);
            
            // Filtrer par formateur si spécifié
            if (request.getTrainerId() != null) {
                dayPlannings = dayPlannings.stream()
                        .filter(p -> request.getTrainerId().equals(p.getTrainerId()))
                        .collect(Collectors.toList());
            }
            
            // Filtrer par salle si spécifié
            if (request.getRoomId() != null) {
                dayPlannings = dayPlannings.stream()
                        .filter(p -> request.getRoomId().equals(p.getRoomId()))
                        .collect(Collectors.toList());
            }
            
            // Trouver les créneaux libres
            List<TimeSlotSuggestion> daySlots = findFreeSlots(
                currentDate, dayPlannings, request.getDurationMinutes()
            );
            
            suggestions.addAll(daySlots);
            currentDate = currentDate.plusDays(1);
        }
        
        return suggestions.stream()
                .limit(request.getMaxSuggestions())
                .collect(Collectors.toList());
    }

    /**
     * Calcule la charge de travail d'un formateur pour une date donnée
     */
    public TrainerWorkloadResponse getTrainerWorkload(Long trainerId, LocalDate date) {
        List<Planning> trainerPlannings = planningRepository.findPlanningsByDate(date).stream()
                .filter(p -> trainerId.equals(p.getTrainerId()))
                .collect(Collectors.toList());
        
        int totalMinutes = 0;
        for (Planning planning : trainerPlannings) {
            Duration duration = Duration.between(planning.getStartTime(), planning.getEndTime());
            totalMinutes += duration.toMinutes();
        }
        
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        boolean isOverloaded = hours >= MAX_DAILY_HOURS;
        
        String warning = null;
        if (isOverloaded) {
            warning = String.format("Le formateur dépasse la limite de %d heures par jour", MAX_DAILY_HOURS);
        }
        
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();
        response.setTrainerId(trainerId);
        response.setDate(date);
        response.setTotalHours(hours);
        response.setTotalMinutes(minutes);
        response.setSessionCount(trainerPlannings.size());
        response.setOverloaded(isOverloaded);
        response.setMaxDailyHours(MAX_DAILY_HOURS);
        response.setWarning(warning);
        
        return response;
    }

    /**
     * Vérifie si deux créneaux horaires se chevauchent
     */
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    /**
     * Trouve les créneaux libres dans une journée
     */
    private List<TimeSlotSuggestion> findFreeSlots(LocalDate date, List<Planning> existingPlannings, int durationMinutes) {
        List<TimeSlotSuggestion> freeSlots = new ArrayList<>();
        LocalTime currentTime = WORK_START;
        
        // Trier les plannings par heure de début
        List<Planning> sortedPlannings = existingPlannings.stream()
                .sorted((p1, p2) -> p1.getStartTime().compareTo(p2.getStartTime()))
                .collect(Collectors.toList());
        
        for (Planning planning : sortedPlannings) {
            // Vérifier s'il y a un créneau libre avant ce planning
            if (currentTime.plusMinutes(durationMinutes).isBefore(planning.getStartTime()) ||
                currentTime.plusMinutes(durationMinutes).equals(planning.getStartTime())) {
                
                LocalTime slotEnd = currentTime.plusMinutes(durationMinutes);
                int score = calculateSlotScore(currentTime, slotEnd);
                
                freeSlots.add(new TimeSlotSuggestion(
                    date,
                    currentTime,
                    slotEnd,
                    score,
                    "Créneau disponible"
                ));
            }
            
            // Avancer au-delà du planning actuel
            currentTime = planning.getEndTime();
        }
        
        // Vérifier s'il reste un créneau libre en fin de journée
        if (currentTime.plusMinutes(durationMinutes).isBefore(WORK_END) ||
            currentTime.plusMinutes(durationMinutes).equals(WORK_END)) {
            
            LocalTime slotEnd = currentTime.plusMinutes(durationMinutes);
            int score = calculateSlotScore(currentTime, slotEnd);
            
            freeSlots.add(new TimeSlotSuggestion(
                date,
                currentTime,
                slotEnd,
                score,
                "Créneau disponible"
            ));
        }
        
        return freeSlots;
    }

    /**
     * Calcule un score de qualité pour un créneau (0-100)
     * Les créneaux en milieu de matinée/après-midi ont un meilleur score
     */
    private int calculateSlotScore(LocalTime start, LocalTime end) {
        int score = 50; // Score de base
        
        // Bonus pour les créneaux en milieu de matinée (9h-11h)
        if (start.isAfter(LocalTime.of(9, 0)) && end.isBefore(LocalTime.of(11, 30))) {
            score += 30;
        }
        
        // Bonus pour les créneaux en début d'après-midi (14h-16h)
        if (start.isAfter(LocalTime.of(14, 0)) && end.isBefore(LocalTime.of(16, 30))) {
            score += 20;
        }
        
        // Malus pour les créneaux très tôt ou très tard
        if (start.isBefore(LocalTime.of(8, 30)) || start.isAfter(LocalTime.of(17, 0))) {
            score -= 20;
        }
        
        return Math.max(0, Math.min(100, score));
    }
}
