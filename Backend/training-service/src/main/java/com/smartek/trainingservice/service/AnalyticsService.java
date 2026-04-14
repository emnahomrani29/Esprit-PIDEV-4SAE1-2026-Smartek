package com.smartek.trainingservice.service;

import com.smartek.trainingservice.dto.TrainingAnalyticsResponse;
import com.smartek.trainingservice.entity.Training;
import com.smartek.trainingservice.entity.TrainingEnrollment;
import com.smartek.trainingservice.repository.TrainingRepository;
import com.smartek.trainingservice.repository.TrainingEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TrainingRepository trainingRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;

    public List<TrainingAnalyticsResponse> getTrainerTrainingAnalytics(Long trainerId) {
        log.info("Getting training analytics for trainer: {}", trainerId);
        
        // Récupérer toutes les formations créées par ce trainer
        List<Training> trainerTrainings = trainingRepository.findByCreatedBy(trainerId);
        log.info("Found {} trainings for trainer {}", trainerTrainings.size(), trainerId);
        
        List<TrainingAnalyticsResponse> analytics = new ArrayList<>();
        
        for (Training training : trainerTrainings) {
            // Récupérer toutes les inscriptions pour cette formation
            List<TrainingEnrollment> enrollments = enrollmentRepository.findByTrainingTrainingId(training.getTrainingId());
            
            // Calculer les statistiques
            int totalEnrollments = enrollments.size();
            int activeEnrollments = (int) enrollments.stream()
                .filter(e -> "ENROLLED".equals(e.getStatus()) || "IN_PROGRESS".equals(e.getStatus()) || "COURSES_COMPLETED".equals(e.getStatus()))
                .count();
            int completedEnrollments = (int) enrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .count();
            
            // Calculer la progression moyenne
            double averageProgress = enrollments.stream()
                .mapToInt(TrainingEnrollment::getProgress)
                .average()
                .orElse(0.0);
            
            // Calculer le score moyen des examens
            double averageScore = enrollments.stream()
                .filter(e -> e.getExamScore() != null)
                .mapToInt(TrainingEnrollment::getExamScore)
                .average()
                .orElse(0.0);
            
            TrainingAnalyticsResponse response = TrainingAnalyticsResponse.builder()
                .trainingId(training.getTrainingId())
                .trainingName(training.getTitle())
                .trainingTitle(training.getTitle())
                .totalLearners(totalEnrollments)
                .completedLearners(completedEnrollments)
                .activeLearners(activeEnrollments)
                .averageProgress(averageProgress)
                .averageScore(averageScore)
                .totalEnrollments(totalEnrollments)
                .activeEnrollments(activeEnrollments)
                .completedEnrollments(completedEnrollments)
                .build();
            
            analytics.add(response);
        }
        
        log.info("Returning {} training analytics records", analytics.size());
        return analytics;
    }
}
