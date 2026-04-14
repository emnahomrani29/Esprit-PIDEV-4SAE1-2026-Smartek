package com.smartek.planning.config;

import com.smartek.planning.model.Planning;
import com.smartek.planning.repository.PlanningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PlanningRepository planningRepository;

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si des données existent déjà
        if (planningRepository.count() > 0) {
            System.out.println("Données de test déjà présentes, pas d'initialisation nécessaire.");
            return;
        }

        System.out.println("Initialisation des données de test pour le planning...");

        List<Planning> testPlannings = Arrays.asList(
            // Semaine du 2024-01-01 (Lundi)
            createPlanning("Formation Angular Avancé", "Concepts avancés d'Angular et TypeScript", 
                "TRAINING", LocalDate.of(2024, 1, 1), LocalTime.of(9, 0), LocalTime.of(11, 0),
                "Salle A101", "#3B82F6", 20, "PUBLISHED"),

            createPlanning("Examen JavaScript", "Évaluation des compétences JavaScript", 
                "EXAM", LocalDate.of(2024, 1, 3), LocalTime.of(14, 0), LocalTime.of(16, 0),
                "Salle B202", "#EF4444", 30, "PUBLISHED"),

            createPlanning("Conférence Tech 2024", "Dernières tendances technologiques", 
                "EVENT", LocalDate.of(2024, 1, 5), LocalTime.of(10, 0), LocalTime.of(12, 0),
                "Amphithéâtre", "#10B981", 100, "PUBLISHED"),

            // Semaine suivante
            createPlanning("Formation React", "Introduction à React et Redux", 
                "TRAINING", LocalDate.of(2024, 1, 8), LocalTime.of(9, 0), LocalTime.of(12, 0),
                "Salle C301", "#3B82F6", 15, "PUBLISHED"),

            createPlanning("Examen Python", "Test de programmation Python", 
                "EXAM", LocalDate.of(2024, 1, 10), LocalTime.of(13, 0), LocalTime.of(15, 0),
                "Salle D401", "#EF4444", 25, "PUBLISHED"),

            createPlanning("Workshop DevOps", "Pratiques DevOps et CI/CD", 
                "EVENT", LocalDate.of(2024, 1, 12), LocalTime.of(14, 0), LocalTime.of(17, 0),
                "Lab Informatique", "#10B981", 20, "PUBLISHED"),

            // Session avec capacité limitée pour tester la liste d'attente
            createPlanning("Formation Intensive Java", "Formation Java pour débutants", 
                "TRAINING", LocalDate.of(2024, 1, 15), LocalTime.of(9, 0), LocalTime.of(17, 0),
                "Salle E501", "#3B82F6", 5, "PUBLISHED"),

            // Session en brouillon (non publiée)
            createPlanning("Formation Vue.js", "Introduction à Vue.js", 
                "TRAINING", LocalDate.of(2024, 1, 16), LocalTime.of(10, 0), LocalTime.of(12, 0),
                "Salle F601", "#3B82F6", 20, "SCHEDULED")
        );

        planningRepository.saveAll(testPlannings);
        System.out.println("Données de test initialisées avec succès : " + testPlannings.size() + " plannings créés.");
    }

    private Planning createPlanning(String title, String description, String eventType, 
                                  LocalDate date, LocalTime startTime, LocalTime endTime,
                                  String location, String color, Integer maxParticipants, String status) {
        Planning planning = new Planning();
        planning.setTitle(title);
        planning.setDescription(description);
        planning.setEventType(eventType);
        planning.setDate(date);
        planning.setStartTime(startTime);
        planning.setEndTime(endTime);
        planning.setLocation(location);
        planning.setColor(color);
        planning.setMaxParticipants(maxParticipants);
        planning.setCurrentParticipants(0);
        planning.setStatus(status);
        planning.setTrainerId(1L);
        planning.setCreatedBy(1L);
        return planning;
    }
}