package com.smartek.courseservice.controller;

import com.smartek.courseservice.client.ExamClient;
import com.smartek.courseservice.client.TrainingClient;
import com.smartek.courseservice.client.dto.ProgressUpdateRequest;
import com.smartek.courseservice.client.dto.TrainingResponse;
import com.smartek.courseservice.entity.CourseCompletion;
import com.smartek.courseservice.repository.CourseCompletionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseCompletionController {
    
    private final ExamClient examClient;
    private final TrainingClient trainingClient;
    private final CourseCompletionRepository courseCompletionRepository;
    
    @PostMapping("/{courseId}/complete")
    public ResponseEntity<String> completeCourse(
            @PathVariable Long courseId,
            @RequestParam Long userId) {
        log.info("Cours {} complété par l'utilisateur {}", courseId, userId);
        
        // Enregistrer la complétion du cours
        if (!courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            CourseCompletion completion = CourseCompletion.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .build();
            courseCompletionRepository.save(completion);
            log.info("Complétion du cours {} enregistrée pour l'utilisateur {}", courseId, userId);
        }
        
        try {
            // Déverrouiller le QUIZ associé au cours
            examClient.unlockQuizForCourse(userId, courseId);
            log.info("Quiz déverrouillé pour le cours {} et l'utilisateur {}", courseId, userId);
        } catch (Exception e) {
            log.error("Erreur lors du déverrouillage du quiz: {}", e.getMessage());
        }
        
        // Mettre à jour la progression de toutes les formations contenant ce cours
        try {
            List<TrainingResponse> trainings = trainingClient.getTrainingsByCourse(courseId);
            log.info("Trouvé {} formations contenant le cours {}", trainings.size(), courseId);
            
            for (TrainingResponse training : trainings) {
                updateTrainingProgress(userId, training);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la progression des formations: {}", e.getMessage());
        }
        
        return ResponseEntity.ok("Cours complété avec succès");
    }
    
    @GetMapping("/{courseId}/is-completed")
    public ResponseEntity<Boolean> isCourseCompleted(
            @PathVariable Long courseId,
            @RequestParam Long userId) {
        boolean isCompleted = courseCompletionRepository.existsByUserIdAndCourseId(userId, courseId);
        return ResponseEntity.ok(isCompleted);
    }
    
    @GetMapping("/completed-count")
    public ResponseEntity<Integer> getCompletedCoursesCount(@RequestParam Long userId) {
        int count = courseCompletionRepository.countByUserId(userId);
        log.info("Utilisateur {} a complété {} cours", userId, count);
        return ResponseEntity.ok(count);
    }
    
    @DeleteMapping("/{courseId}/uncomplete")
    public ResponseEntity<String> uncompleteCourse(
            @PathVariable Long courseId,
            @RequestParam Long userId) {
        log.info("Dé-complétion du cours {} pour l'utilisateur {}", courseId, userId);
        
        // Supprimer la complétion du cours
        courseCompletionRepository.findByUserIdAndCourseId(userId, courseId)
                .ifPresent(completion -> {
                    courseCompletionRepository.delete(completion);
                    log.info("Complétion du cours {} supprimée pour l'utilisateur {}", courseId, userId);
                });
        
        try {
            // Reverrouiller le QUIZ associé au cours
            examClient.lockQuizForCourse(userId, courseId);
            log.info("Quiz reverrouillé pour le cours {} et l'utilisateur {}", courseId, userId);
        } catch (Exception e) {
            log.error("Erreur lors du reverrouillage du quiz: {}", e.getMessage());
        }
        
        // Mettre à jour la progression de toutes les formations contenant ce cours
        try {
            List<TrainingResponse> trainings = trainingClient.getTrainingsByCourse(courseId);
            log.info("Trouvé {} formations contenant le cours {}", trainings.size(), courseId);
            
            for (TrainingResponse training : trainings) {
                updateTrainingProgress(userId, training);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la progression des formations: {}", e.getMessage());
        }
        
        return ResponseEntity.ok("Cours marqué comme non terminé");
    }
    
    private void updateTrainingProgress(Long userId, TrainingResponse training) {
        try {
            List<Long> courseIds = training.getCourseIds();
            if (courseIds == null || courseIds.isEmpty()) {
                log.warn("Formation {} n'a pas de cours", training.getTrainingId());
                return;
            }
            
            // Compter combien de cours l'utilisateur a complété dans cette formation
            long completedCount = courseCompletionRepository.countByUserIdAndCourseIdIn(userId, courseIds);
            int totalCourses = courseIds.size();
            int progress = (int) ((completedCount * 100) / totalCourses);
            
            log.info("Formation {}: {}/{} cours complétés = {}%", 
                    training.getTrainingId(), completedCount, totalCourses, progress);
            
            // Mettre à jour la progression
            ProgressUpdateRequest request = new ProgressUpdateRequest(progress);
            trainingClient.updateTrainingProgress(userId, training.getTrainingId(), request);
            log.info("Progression mise à jour pour la formation {} : {}%", training.getTrainingId(), progress);
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la progression de la formation {}: {}", 
                    training.getTrainingId(), e.getMessage());
        }
    }
}
