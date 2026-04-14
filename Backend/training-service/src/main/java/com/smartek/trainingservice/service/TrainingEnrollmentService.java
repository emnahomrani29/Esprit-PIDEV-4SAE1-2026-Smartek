package com.smartek.trainingservice.service;

import com.smartek.trainingservice.client.ExamClient;
import com.smartek.trainingservice.client.CourseClient;
import com.smartek.trainingservice.client.CourseResponse;
import com.smartek.trainingservice.client.ChapterResponse;
import com.smartek.trainingservice.dto.TrainingEnrollmentRequest;
import com.smartek.trainingservice.dto.TrainingEnrollmentResponse;
import com.smartek.trainingservice.dto.TrainingStatsResponse;
import com.smartek.trainingservice.dto.TrainingResponse;
import com.smartek.trainingservice.entity.Training;
import com.smartek.trainingservice.entity.TrainingEnrollment;
import com.smartek.trainingservice.repository.TrainingRepository;
import com.smartek.trainingservice.repository.TrainingEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingEnrollmentService {
    
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingRepository trainingRepository;
    private final ExamClient examClient;
    private final CourseClient courseClient;
    
    @Transactional
    public TrainingEnrollmentResponse enrollUser(TrainingEnrollmentRequest request) {
        // Vérifier si l'utilisateur est déjà inscrit
        if (enrollmentRepository.existsByUserIdAndTrainingTrainingId(request.getUserId(), request.getTrainingId())) {
            throw new RuntimeException("L'utilisateur est déjà inscrit à cette formation");
        }
        
        // Récupérer la formation
        Training training = trainingRepository.findById(request.getTrainingId())
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));
        
        // Créer l'inscription
        TrainingEnrollment enrollment = TrainingEnrollment.builder()
                .training(training)
                .userId(request.getUserId())
                .build();
        
        TrainingEnrollment savedEnrollment = enrollmentRepository.save(enrollment);
        
        // Créer l'enrollment pour l'examen de la formation (verrouillé)
        try {
            log.info("Création de l'enrollment pour l'examen de la formation {} pour l'utilisateur {}", 
                    request.getTrainingId(), request.getUserId());
            examClient.enrollExamForTraining(request.getUserId(), request.getTrainingId());
            log.info("Enrollment d'examen créé avec succès");
        } catch (Exception e) {
            log.warn("Aucun examen trouvé pour cette formation ou erreur: {}", e.getMessage());
            // Ne pas bloquer l'inscription si aucun examen n'existe
        }
        
        // Créer les enrollments pour les quiz de tous les cours de la formation
        if (training.getCourseIds() != null && !training.getCourseIds().isEmpty()) {
            for (Long courseId : training.getCourseIds()) {
                try {
                    log.info("Création de l'enrollment pour le quiz du cours {} pour l'utilisateur {}", 
                            courseId, request.getUserId());
                    examClient.enrollQuizForCourse(request.getUserId(), courseId);
                } catch (Exception e) {
                    log.warn("Aucun quiz trouvé pour le cours {} ou erreur: {}", courseId, e.getMessage());
                    // Continuer même si un quiz n'existe pas
                }
            }
        }
        
        return mapToResponse(savedEnrollment);
    }
    
    public List<TrainingEnrollmentResponse> getUserEnrollments(Long userId) {
        return enrollmentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TrainingEnrollmentResponse> getTrainingEnrollments(Long trainingId) {
        return enrollmentRepository.findByTrainingTrainingId(trainingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void unenrollUser(Long userId, Long trainingId) {
        TrainingEnrollment enrollment = enrollmentRepository.findByUserIdAndTrainingTrainingId(userId, trainingId)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
        enrollmentRepository.delete(enrollment);
    }
    
    @Transactional
    public TrainingEnrollmentResponse updateProgress(Long userId, Long trainingId, int progress) {
        TrainingEnrollment enrollment = enrollmentRepository.findByUserIdAndTrainingTrainingId(userId, trainingId)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
        
        int oldProgress = enrollment.getProgress();
        enrollment.setProgress(progress);
        
        // Si la progression atteint 100%, marquer comme terminé et débloquer l'examen
        if (progress >= 100 && enrollment.getCompletedAt() == null) {
            enrollment.setCompletedAt(java.time.LocalDateTime.now());
            enrollment.setStatus("COMPLETED");
            
            // Débloquer l'examen final de la formation
            try {
                log.info("Formation terminée - Déblocage de l'examen pour userId={}, trainingId={}", userId, trainingId);
                examClient.unlockExamForTraining(userId, trainingId);
                log.info("Examen débloqué avec succès pour la formation {}", trainingId);
            } catch (Exception e) {
                log.error("Erreur lors du déblocage de l'examen: {}", e.getMessage());
                // Ne pas bloquer la progression même si le déblocage échoue
            }
        } 
        // Si la progression était à 100% et descend en dessous, reverrouiller l'examen
        else if (oldProgress >= 100 && progress < 100) {
            enrollment.setCompletedAt(null);
            enrollment.setStatus("IN_PROGRESS");
            
            // Reverrouiller l'examen
            try {
                log.info("Formation non terminée - Reverrouillage de l'examen pour userId={}, trainingId={}", userId, trainingId);
                examClient.lockExamForTraining(userId, trainingId);
                log.info("Examen reverrouillé avec succès pour la formation {}", trainingId);
            } catch (Exception e) {
                log.error("Erreur lors du reverrouillage de l'examen: {}", e.getMessage());
            }
        }
        else if (progress > 0 && progress < 100) {
            enrollment.setStatus("IN_PROGRESS");
        }
        
        TrainingEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return mapToResponse(updatedEnrollment);
    }
    
    private TrainingEnrollmentResponse mapToResponse(TrainingEnrollment enrollment) {
        TrainingEnrollmentResponse response = new TrainingEnrollmentResponse();
        response.setId(enrollment.getId());
        response.setTrainingId(enrollment.getTraining().getTrainingId());
        response.setTrainingTitle(enrollment.getTraining().getTitle());
        response.setUserId(enrollment.getUserId());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setIsActive(enrollment.getIsActive());
        response.setProgress(enrollment.getProgress());
        response.setCompletedAt(enrollment.getCompletedAt());
        response.setStatus(enrollment.getStatus());
        return response;
    }

    public Boolean hasCompletedAllCourses(Long userId, Long trainingId) {
        TrainingEnrollment enrollment = enrollmentRepository.findByUserIdAndTrainingTrainingId(userId, trainingId)
                .orElse(null);
        
        if (enrollment == null) {
            return false; // Pas inscrit = pas complété
        }
        
        // Vérifier si la progression est à 100% ou si completedAt est défini
        return enrollment.getProgress() >= 100 || enrollment.getCompletedAt() != null;
    }
    
    public TrainingStatsResponse getTrainingStatsByUserId(Long userId) {
        log.info("Récupération des statistiques de formation pour l'utilisateur: {}", userId);
        
        List<TrainingEnrollment> enrollments = enrollmentRepository.findByUserId(userId);
        
        long totalEnrolled = enrollments.size();
        long completed = enrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .count();
        long inProgress = enrollments.stream()
                .filter(e -> "IN_PROGRESS".equals(e.getStatus()) || "COURSES_COMPLETED".equals(e.getStatus()))
                .count();
        
        double averageProgress = enrollments.stream()
                .mapToInt(TrainingEnrollment::getProgress)
                .average()
                .orElse(0.0);
        
        // Status breakdown
        java.util.Map<String, Integer> statusBreakdown = new java.util.HashMap<>();
        for (TrainingEnrollment enrollment : enrollments) {
            String status = enrollment.getStatus();
            statusBreakdown.put(status, statusBreakdown.getOrDefault(status, 0) + 1);
        }
        
        return TrainingStatsResponse.builder()
                .userId(userId)
                .totalEnrolled((int) totalEnrolled)
                .inProgress((int) inProgress)
                .completed((int) completed)
                .averageProgress(Math.round(averageProgress * 100.0) / 100.0)
                .statusBreakdown(statusBreakdown)
                .build();
    }
    
    public List<TrainingResponse> getUserTrainingsWithDetails(Long userId) {
        log.info("Récupération des formations complètes pour l'utilisateur: {}", userId);
        
        List<TrainingEnrollment> enrollments = enrollmentRepository.findByUserId(userId);
        List<TrainingResponse> trainings = new ArrayList<>();
        
        for (TrainingEnrollment enrollment : enrollments) {
            Training training = enrollment.getTraining();
            
            // Récupérer les informations détaillées des cours
            List<TrainingResponse.CourseInfo> courses = new ArrayList<>();
            if (!training.getCourseIds().isEmpty()) {
                for (Long courseId : training.getCourseIds()) {
                    try {
                        CourseResponse courseResponse = courseClient.getCourseById(courseId);
                        List<ChapterResponse> chapterResponses = courseClient.getChaptersByCourseId(courseId);
                        
                        List<TrainingResponse.ChapterInfo> chapters = chapterResponses.stream()
                                .map(ch -> TrainingResponse.ChapterInfo.builder()
                                        .chapterId(ch.getChapterId())
                                        .title(ch.getTitle())
                                        .description(ch.getDescription())
                                        .orderIndex(ch.getOrderIndex())
                                        .pdfFileName(ch.getPdfFileName())
                                        .pdfFilePath(ch.getPdfFilePath())
                                        .build())
                                .collect(Collectors.toList());
                        
                        TrainingResponse.CourseInfo courseInfo = TrainingResponse.CourseInfo.builder()
                                .courseId(courseResponse.getCourseId())
                                .title(courseResponse.getTitle())
                                .content(courseResponse.getContent())
                                .duration(courseResponse.getDuration() != null ? 
                                    LocalDate.parse(courseResponse.getDuration()) : null)
                                .chapters(chapters)
                                .build();
                        courses.add(courseInfo);
                    } catch (Exception e) {
                        log.error("Erreur lors de la récupération du cours {}: {}", courseId, e.getMessage());
                    }
                }
            }
            
            TrainingResponse trainingResponse = TrainingResponse.builder()
                    .trainingId(training.getTrainingId())
                    .title(training.getTitle())
                    .description(training.getDescription())
                    .category(training.getCategory())
                    .level(training.getLevel())
                    .duration(training.getDuration())
                    .courseIds(training.getCourseIds())
                    .courses(courses)
                    .createdAt(training.getCreatedAt())
                    .updatedAt(training.getUpdatedAt())
                    .build();
            
            trainings.add(trainingResponse);
        }
        
        return trainings;
    }
    
    public List<com.smartek.trainingservice.dto.TrainerTrainingAnalyticsResponse> getTrainerTrainingAnalytics(Long trainerId) {
        List<Training> trainerTrainings = trainingRepository.findByCreatedBy(trainerId);
        List<com.smartek.trainingservice.dto.TrainerTrainingAnalyticsResponse> analytics = new ArrayList<>();
        
        for (Training training : trainerTrainings) {
            long totalEnrollments = enrollmentRepository.countByTrainingTrainingId(training.getTrainingId());
            long activeEnrollments = enrollmentRepository.countByTrainingTrainingIdAndStatus(training.getTrainingId(), "IN_PROGRESS");
            long completedEnrollments = enrollmentRepository.countByTrainingTrainingIdAndStatus(training.getTrainingId(), "COMPLETED");
            
            com.smartek.trainingservice.dto.TrainerTrainingAnalyticsResponse response = new com.smartek.trainingservice.dto.TrainerTrainingAnalyticsResponse();
            response.setTrainingId(training.getTrainingId());
            response.setTrainingTitle(training.getTitle());
            response.setTotalEnrollments((int) totalEnrollments);
            response.setActiveEnrollments((int) activeEnrollments);
            response.setCompletedEnrollments((int) completedEnrollments);
            
            analytics.add(response);
        }
        
        return analytics;
    }
}
