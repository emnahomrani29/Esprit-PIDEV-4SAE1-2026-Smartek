package com.smartek.planning.service;

import com.smartek.planning.dto.WeeklyPlanningRequest;
import com.smartek.planning.dto.WeeklyPlanningResponse;
import com.smartek.planning.dto.WeeklyPlanningItem;
import com.smartek.planning.model.Planning;
import com.smartek.planning.model.PlanningRegistration;
import com.smartek.planning.repository.PlanningRepository;
import com.smartek.planning.repository.PlanningRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WeeklyPlanningService {

    private final PlanningRepository planningRepository;
    private final PlanningRegistrationRepository registrationRepository;
    private final RestTemplate restTemplate;

    public WeeklyPlanningResponse getWeeklyPlanning(Long trainerId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        
        List<Planning> plannings = planningRepository.findByTrainerIdAndDateBetween(
            trainerId, weekStartDate, weekEndDate);
        
        List<WeeklyPlanningResponse.WeeklyPlanningItem> items = plannings.stream()
            .map(this::mapToWeeklyPlanningItem)
            .collect(Collectors.toList());
        
        WeeklyPlanningResponse.WeeklyStats stats = calculateWeeklyStats(plannings);
        
        return new WeeklyPlanningResponse(weekStartDate, weekEndDate, trainerId, items, stats);
    }

    /**
     * Récupère tous les plannings publiés pour une semaine donnée (pour les learners)
     */
    public List<WeeklyPlanningItem> getPublishedPlannings(LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        
        List<Planning> publishedPlannings = planningRepository.findByStatusAndDateBetween(
            "PUBLISHED", weekStartDate, weekEndDate);
        
        return publishedPlannings.stream()
            .map(this::mapToWeeklyPlanningItemForLearner)
            .collect(Collectors.toList());
    }

    /**
     * Récupère tous les trainings disponibles depuis le training-service
     */
    public List<Object> getAvailableTrainings() {
        try {
            ResponseEntity<Object[]> response = restTemplate.getForEntity(
                "http://localhost:8084/api/trainings", Object[].class);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des trainings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupère tous les examens disponibles depuis le exam-service
     */
    public List<Object> getAvailableExams() {
        try {
            ResponseEntity<Object[]> response = restTemplate.getForEntity(
                "http://localhost:8085/api/exams", Object[].class);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des examens: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupère tous les événements disponibles depuis le event-service
     */
    public List<Object> getAvailableEvents() {
        try {
            ResponseEntity<Object[]> response = restTemplate.getForEntity(
                "http://localhost:8082/events", Object[].class);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des événements: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public WeeklyPlanningResponse createOrUpdateWeeklyPlanning(WeeklyPlanningRequest request) {
        // Supprimer les plannings existants pour cette semaine
        LocalDate weekEndDate = request.getWeekStartDate().plusDays(6);
        planningRepository.deleteByTrainerIdAndDateBetween(
            request.getTrainerId(), request.getWeekStartDate(), weekEndDate);
        
        // Créer les nouveaux plannings
        List<Planning> plannings = request.getItems().stream()
            .map(item -> mapToPlanning(item, request.getTrainerId()))
            .collect(Collectors.toList());
        
        plannings = planningRepository.saveAll(plannings);
        
        List<WeeklyPlanningResponse.WeeklyPlanningItem> items = plannings.stream()
            .map(this::mapToWeeklyPlanningItem)
            .collect(Collectors.toList());
        
        WeeklyPlanningResponse.WeeklyStats stats = calculateWeeklyStats(plannings);
        
        return new WeeklyPlanningResponse(
            request.getWeekStartDate(), weekEndDate, request.getTrainerId(), items, stats);
    }

    public WeeklyPlanningResponse publishWeeklyPlanning(Long trainerId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        
        List<Planning> plannings = planningRepository.findByTrainerIdAndDateBetween(
            trainerId, weekStartDate, weekEndDate);
        
        plannings.forEach(planning -> planning.setStatus("PUBLISHED"));
        plannings = planningRepository.saveAll(plannings);
        
        List<WeeklyPlanningResponse.WeeklyPlanningItem> items = plannings.stream()
            .map(this::mapToWeeklyPlanningItem)
            .collect(Collectors.toList());
        
        WeeklyPlanningResponse.WeeklyStats stats = calculateWeeklyStats(plannings);
        
        return new WeeklyPlanningResponse(weekStartDate, weekEndDate, trainerId, items, stats);
    }

    public WeeklyPlanningResponse unpublishWeeklyPlanning(Long trainerId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        
        List<Planning> plannings = planningRepository.findByTrainerIdAndDateBetween(
            trainerId, weekStartDate, weekEndDate);
        
        plannings.forEach(planning -> planning.setStatus("DRAFT"));
        plannings = planningRepository.saveAll(plannings);
        
        List<WeeklyPlanningResponse.WeeklyPlanningItem> items = plannings.stream()
            .map(this::mapToWeeklyPlanningItem)
            .collect(Collectors.toList());
        
        WeeklyPlanningResponse.WeeklyStats stats = calculateWeeklyStats(plannings);
        
        return new WeeklyPlanningResponse(weekStartDate, weekEndDate, trainerId, items, stats);
    }

    private WeeklyPlanningResponse.WeeklyPlanningItem mapToWeeklyPlanningItem(Planning planning) {
        return new WeeklyPlanningResponse.WeeklyPlanningItem(
            planning.getPlanningId(),
            planning.getEventType(),
            extractItemId(planning),
            planning.getTitle(),
            planning.getDescription(),
            planning.getDate(),
            planning.getStartTime().toString(),
            planning.getEndTime().toString(),
            planning.getLocation(),
            planning.getColor(),
            planning.getMaxParticipants(),
            planning.getCurrentParticipants(),
            planning.getStatus(),
            "PUBLISHED".equals(planning.getStatus())
        );
    }

    private WeeklyPlanningItem mapToWeeklyPlanningItemForLearner(Planning planning) {
        WeeklyPlanningItem item = new WeeklyPlanningItem();
        item.setPlanningId(planning.getPlanningId());
        item.setType(planning.getEventType());
        item.setItemId(extractItemId(planning));
        item.setTitle(planning.getTitle());
        item.setDescription(cleanDescription(planning.getDescription()));
        item.setDate(planning.getDate());
        item.setStartTime(planning.getStartTime().toString());
        item.setEndTime(planning.getEndTime().toString());
        item.setLocation(planning.getLocation());
        item.setColor(planning.getColor());
        item.setMaxParticipants(planning.getMaxParticipants());
        item.setCurrentParticipants(planning.getCurrentParticipants());
        item.setStatus(planning.getStatus());
        item.setPublished("PUBLISHED".equals(planning.getStatus()));
        return item;
    }

    private String cleanDescription(String description) {
        if (description != null && description.contains("[ID:")) {
            int idIndex = description.lastIndexOf("[ID:");
            return description.substring(0, idIndex).trim();
        }
        return description;
    }

    private Planning mapToPlanning(WeeklyPlanningRequest.WeeklyPlanningItem item, Long trainerId) {
        Planning planning = new Planning();
        planning.setTitle(item.getTitle());
        planning.setDescription(item.getDescription());
        planning.setEventType(item.getType());
        planning.setDate(item.getDate());
        planning.setStartTime(LocalTime.parse(item.getStartTime()));
        planning.setEndTime(LocalTime.parse(item.getEndTime()));
        planning.setLocation(item.getLocation());
        planning.setColor(item.getColor());
        planning.setMaxParticipants(item.getMaxParticipants());
        planning.setCurrentParticipants(0);
        planning.setStatus(item.getStatus() != null ? item.getStatus() : "DRAFT");
        planning.setTrainerId(trainerId);
        planning.setCreatedBy(trainerId);
        
        // Stocker l'ID de l'item dans la description pour le retrouver
        if (item.getItemId() != null) {
            String desc = planning.getDescription() != null ? planning.getDescription() : "";
            planning.setDescription(desc + " [ID:" + item.getItemId() + "]");
        }
        
        return planning;
    }

    private Long extractItemId(Planning planning) {
        if (planning.getDescription() != null && planning.getDescription().contains("[ID:")) {
            try {
                String desc = planning.getDescription();
                int start = desc.lastIndexOf("[ID:") + 4;
                int end = desc.indexOf("]", start);
                return Long.parseLong(desc.substring(start, end));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private WeeklyPlanningResponse.WeeklyStats calculateWeeklyStats(List<Planning> plannings) {
        int totalSessions = plannings.size();
        int trainingSessions = (int) plannings.stream().filter(p -> "TRAINING".equals(p.getEventType())).count();
        int examSessions = (int) plannings.stream().filter(p -> "EXAM".equals(p.getEventType())).count();
        int eventSessions = (int) plannings.stream().filter(p -> "EVENT".equals(p.getEventType())).count();
        int publishedSessions = (int) plannings.stream().filter(p -> "PUBLISHED".equals(p.getStatus())).count();
        int draftSessions = totalSessions - publishedSessions;
        
        double totalHours = plannings.stream()
            .mapToDouble(p -> ChronoUnit.MINUTES.between(p.getStartTime(), p.getEndTime()) / 60.0)
            .sum();
        
        return new WeeklyPlanningResponse.WeeklyStats(
            totalSessions, trainingSessions, examSessions, eventSessions,
            publishedSessions, draftSessions, totalHours);
    }

    /**
     * Inscription d'un learner à une session
     */
    public String registerToSession(Long planningId, Long learnerId) {
        // Vérifier si la session existe
        Optional<Planning> planningOpt = planningRepository.findById(planningId);
        if (planningOpt.isEmpty()) {
            throw new RuntimeException("Session non trouvée");
        }
        
        Planning planning = planningOpt.get();
        
        // Vérifier si la session est publiée
        if (!"PUBLISHED".equals(planning.getStatus())) {
            throw new RuntimeException("Cette session n'est pas encore disponible pour inscription");
        }
        
        // Vérifier si le learner n'est pas déjà inscrit
        Optional<PlanningRegistration> existingRegistration = 
            registrationRepository.findActiveRegistration(planningId, learnerId);
        if (existingRegistration.isPresent()) {
            throw new RuntimeException("Vous êtes déjà inscrit à cette session");
        }
        
        // Compter les inscriptions actuelles
        Long currentRegistrations = registrationRepository.countActiveRegistrations(planningId);
        
        PlanningRegistration registration = new PlanningRegistration();
        registration.setPlanningId(planningId);
        registration.setLearnerId(learnerId);
        
        // Vérifier la capacité
        if (planning.getMaxParticipants() != null && currentRegistrations >= planning.getMaxParticipants()) {
            // Mettre en liste d'attente
            registration.setStatus("WAITING_LIST");
            registration.setWaitingListPosition(registrationRepository.getNextWaitingListPosition(planningId));
            registrationRepository.save(registration);
            return "Inscription réussie - Vous êtes en liste d'attente (position " + registration.getWaitingListPosition() + ")";
        } else {
            // Inscription directe
            registration.setStatus("REGISTERED");
            registrationRepository.save(registration);
            
            // Mettre à jour le nombre de participants
            planning.setCurrentParticipants((int) (currentRegistrations + 1));
            planningRepository.save(planning);
            
            // Si c'est une formation (TRAINING), créer automatiquement l'inscription dans le training service
            if ("TRAINING".equals(planning.getEventType())) {
                createTrainingEnrollment(planning, learnerId);
            }
            
            return "Inscription réussie !";
        }
    }
    
    /**
     * Créer une inscription dans le training service
     */
    private void createTrainingEnrollment(Planning planning, Long learnerId) {
        try {
            Long trainingId = extractItemId(planning);
            if (trainingId != null) {
                // Créer la requête d'inscription avec les headers appropriés
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                
                String enrollmentRequest = String.format(
                    "{\"trainingId\": %d, \"userId\": %d}", 
                    trainingId, learnerId
                );
                
                org.springframework.http.HttpEntity<String> entity = 
                    new org.springframework.http.HttpEntity<>(enrollmentRequest, headers);
                
                // Envoyer la requête au training service
                ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:8084/api/trainings/enrollments",
                    entity,
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Inscription automatique créée dans le training service pour training ID: " + trainingId + ", learner ID: " + learnerId);
                } else {
                    System.err.println("Erreur lors de l'inscription au training service: " + response.getStatusCode());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'inscription dans le training service: " + e.getMessage());
            // Ne pas faire échouer l'inscription au planning si l'inscription au training échoue
        }
    }

    /**
     * Désinscription d'un learner d'une session
     */
    public String unregisterFromSession(Long planningId, Long learnerId) {
        Optional<PlanningRegistration> registrationOpt = 
            registrationRepository.findActiveRegistration(planningId, learnerId);
        
        if (registrationOpt.isEmpty()) {
            throw new RuntimeException("Aucune inscription trouvée pour cette session");
        }
        
        PlanningRegistration registration = registrationOpt.get();
        String originalStatus = registration.getStatus();
        registration.setStatus("CANCELLED");
        registrationRepository.save(registration);
        
        // Récupérer les informations du planning pour la désinscription du training service
        Optional<Planning> planningOpt = planningRepository.findById(planningId);
        if (planningOpt.isPresent()) {
            Planning planning = planningOpt.get();
            
            // Si c'était une inscription confirmée, libérer une place
            if ("REGISTERED".equals(originalStatus)) {
                planning.setCurrentParticipants(Math.max(0, planning.getCurrentParticipants() - 1));
                planningRepository.save(planning);
                
                // Promouvoir quelqu'un de la liste d'attente
                promoteFromWaitingList(planningId);
            }
            
            // Si c'est une formation (TRAINING), supprimer l'inscription du training service
            if ("TRAINING".equals(planning.getEventType())) {
                removeTrainingEnrollment(planning, learnerId);
            }
        }
        
        return "Désinscription réussie";
    }
    
    /**
     * Supprimer une inscription du training service
     */
    private void removeTrainingEnrollment(Planning planning, Long learnerId) {
        try {
            Long trainingId = extractItemId(planning);
            if (trainingId != null) {
                // Supprimer l'inscription du training service
                restTemplate.delete(
                    "http://localhost:8084/api/trainings/enrollments/user/" + learnerId + "/training/" + trainingId
                );
                
                System.out.println("Inscription supprimée du training service pour training ID: " + trainingId + ", learner ID: " + learnerId);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'inscription du training service: " + e.getMessage());
            // Ne pas faire échouer la désinscription du planning si la suppression du training échoue
        }
    }

    /**
     * Vérifier si un learner est inscrit à une session
     */
    public boolean isLearnerRegistered(Long planningId, Long learnerId) {
        return registrationRepository.findActiveRegistration(planningId, learnerId).isPresent();
    }

    /**
     * Récupérer les inscriptions d'un learner pour une semaine
     */
    public List<WeeklyPlanningItem> getLearnerRegistrations(Long learnerId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        
        List<PlanningRegistration> registrations = registrationRepository
            .findByLearnerIdAndDateRange(learnerId, weekStartDate, weekEndDate);
        
        return registrations.stream()
            .map(this::mapRegistrationToWeeklyPlanningItem)
            .collect(Collectors.toList());
    }

    /**
     * Promouvoir quelqu'un de la liste d'attente
     */
    private void promoteFromWaitingList(Long planningId) {
        List<PlanningRegistration> waitingList = registrationRepository.findByPlanningId(planningId)
            .stream()
            .filter(r -> "WAITING_LIST".equals(r.getStatus()))
            .sorted((a, b) -> a.getWaitingListPosition().compareTo(b.getWaitingListPosition()))
            .collect(Collectors.toList());
        
        if (!waitingList.isEmpty()) {
            PlanningRegistration firstInLine = waitingList.get(0);
            firstInLine.setStatus("REGISTERED");
            firstInLine.setWaitingListPosition(null);
            registrationRepository.save(firstInLine);
            
            // Mettre à jour le planning
            Optional<Planning> planningOpt = planningRepository.findById(planningId);
            if (planningOpt.isPresent()) {
                Planning planning = planningOpt.get();
                planning.setCurrentParticipants(planning.getCurrentParticipants() + 1);
                planningRepository.save(planning);
                
                // Si c'est une formation (TRAINING), créer l'inscription dans le training service
                if ("TRAINING".equals(planning.getEventType())) {
                    createTrainingEnrollment(planning, firstInLine.getLearnerId());
                }
            }
        }
    }

    /**
     * Mapper une inscription vers WeeklyPlanningItem
     */
    private WeeklyPlanningItem mapRegistrationToWeeklyPlanningItem(PlanningRegistration registration) {
        Optional<Planning> planningOpt = planningRepository.findById(registration.getPlanningId());
        if (planningOpt.isEmpty()) {
            return null;
        }
        
        Planning planning = planningOpt.get();
        WeeklyPlanningItem item = mapToWeeklyPlanningItemForLearner(planning);
        
        // Ajouter les informations d'inscription
        item.setRegistrationStatus(registration.getStatus());
        item.setWaitingListPosition(registration.getWaitingListPosition());
        
        return item;
    }
}