package com.smartek.trainingservice.service;

import com.smartek.trainingservice.client.CourseClient;
import com.smartek.trainingservice.client.ExamClient;
import com.smartek.trainingservice.dto.PerformanceStatsResponse;
import com.smartek.trainingservice.entity.Training;
import com.smartek.trainingservice.entity.TrainingEnrollment;
import com.smartek.trainingservice.repository.TrainingEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceService {
    
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final CourseClient courseClient;
    private final ExamClient examClient;
    
    public PerformanceStatsResponse getUserPerformanceStats(Long userId) {
        log.info("Calcul des statistiques de performance pour l'utilisateur {}", userId);
        
        return PerformanceStatsResponse.builder()
                .courses(getCourseStats(userId))
                .trainings(getTrainingStats(userId))
                .exams(getExamStats(userId))
                .build();
    }
    
    private PerformanceStatsResponse.CourseStats getCourseStats(Long userId) {
        try {
            // Récupérer toutes les formations de l'utilisateur
            List<TrainingEnrollment> enrollments = trainingEnrollmentRepository.findByUserId(userId);
            
            int totalEnrolled = 0;
            int completed = 0;
            
            for (TrainingEnrollment enrollment : enrollments) {
                Training training = enrollment.getTraining();
                if (training != null && training.getCourseIds() != null) {
                    totalEnrolled += training.getCourseIds().size();
                    
                    // Compter les cours complétés
                    for (Long courseId : training.getCourseIds()) {
                        try {
                            Boolean isCompleted = courseClient.isCourseCompleted(courseId, userId);
                            if (Boolean.TRUE.equals(isCompleted)) {
                                completed++;
                            }
                        } catch (Exception e) {
                            log.warn("Erreur lors de la vérification du cours {}: {}", courseId, e.getMessage());
                        }
                    }
                }
            }
            
            int inProgress = totalEnrolled - completed;
            double completionRate = totalEnrolled > 0 ? (completed * 100.0 / totalEnrolled) : 0.0;
            
            return PerformanceStatsResponse.CourseStats.builder()
                    .totalEnrolled(totalEnrolled)
                    .inProgress(inProgress)
                    .completed(completed)
                    .completionRate(Math.round(completionRate * 100.0) / 100.0)
                    .totalChapters(0) // À implémenter si nécessaire
                    .completedChapters(0) // À implémenter si nécessaire
                    .build();
                    
        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques de cours: {}", e.getMessage());
            return PerformanceStatsResponse.CourseStats.builder()
                    .totalEnrolled(0)
                    .inProgress(0)
                    .completed(0)
                    .completionRate(0.0)
                    .totalChapters(0)
                    .completedChapters(0)
                    .build();
        }
    }
    
    private PerformanceStatsResponse.TrainingStats getTrainingStats(Long userId) {
        try {
            List<TrainingEnrollment> enrollments = trainingEnrollmentRepository.findByUserId(userId);
            
            int totalEnrolled = enrollments.size();
            int completed = 0;
            int inProgress = 0;
            double totalProgress = 0.0;
            Map<String, Integer> statusBreakdown = new HashMap<>();
            
            for (TrainingEnrollment enrollment : enrollments) {
                String status = enrollment.getStatus() != null ? enrollment.getStatus() : "NOT_STARTED";
                statusBreakdown.put(status, statusBreakdown.getOrDefault(status, 0) + 1);
                
                if ("COMPLETED".equals(status)) {
                    completed++;
                } else if ("IN_PROGRESS".equals(status)) {
                    inProgress++;
                }
                
                totalProgress += (enrollment.getProgress() != null ? enrollment.getProgress() : 0);
            }
            
            double averageProgress = totalEnrolled > 0 ? totalProgress / totalEnrolled : 0.0;
            
            return PerformanceStatsResponse.TrainingStats.builder()
                    .totalEnrolled(totalEnrolled)
                    .inProgress(inProgress)
                    .completed(completed)
                    .averageProgress(Math.round(averageProgress * 100.0) / 100.0)
                    .statusBreakdown(statusBreakdown)
                    .build();
                    
        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques de formation: {}", e.getMessage());
            return PerformanceStatsResponse.TrainingStats.builder()
                    .totalEnrolled(0)
                    .inProgress(0)
                    .completed(0)
                    .averageProgress(0.0)
                    .statusBreakdown(new HashMap<>())
                    .build();
        }
    }
    
    private PerformanceStatsResponse.ExamStats getExamStats(Long userId) {
        try {
            Map<String, Object> examStats = examClient.getUserExamStats(userId);
            
            return PerformanceStatsResponse.ExamStats.builder()
                    .totalAvailable(getIntValue(examStats, "totalAvailable"))
                    .attempted(getIntValue(examStats, "attempted"))
                    .passed(getIntValue(examStats, "passed"))
                    .failed(getIntValue(examStats, "failed"))
                    .averageScore(getDoubleValue(examStats, "averageScore"))
                    .successRate(getDoubleValue(examStats, "successRate"))
                    .totalAttempts(getIntValue(examStats, "totalAttempts"))
                    .build();
                    
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques d'examen: {}", e.getMessage());
            return PerformanceStatsResponse.ExamStats.builder()
                    .totalAvailable(0)
                    .attempted(0)
                    .passed(0)
                    .failed(0)
                    .averageScore(0.0)
                    .successRate(0.0)
                    .totalAttempts(0)
                    .build();
        }
    }
    
    private int getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
    
    private double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
}
