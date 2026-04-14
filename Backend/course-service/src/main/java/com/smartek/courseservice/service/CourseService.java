package com.smartek.courseservice.service;

import com.smartek.courseservice.dto.CourseRequest;
import com.smartek.courseservice.dto.CourseResponse;
import com.smartek.courseservice.dto.CourseStatsResponse;
import com.smartek.courseservice.entity.Course;
import com.smartek.courseservice.entity.CourseCompletion;
import com.smartek.courseservice.exception.DuplicateResourceException;
import com.smartek.courseservice.exception.ResourceNotFoundException;
import com.smartek.courseservice.mapper.CourseMapper;
import com.smartek.courseservice.repository.CourseRepository;
import com.smartek.courseservice.repository.CourseCompletionRepository;
import com.smartek.courseservice.client.TrainingClient;
import com.smartek.courseservice.client.dto.TrainingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseCompletionRepository courseCompletionRepository;
    private final TrainingClient trainingClient;

    
    @Transactional
    @CacheEvict(value = {"courses", "coursesByTrainer"}, allEntries = true)
    public CourseResponse createCourse(CourseRequest request) {
        log.info("Création d'un nouveau cours: {}", request.getTitle());
        
        courseRepository.findByTitle(request.getTitle()).ifPresent(c -> {
            throw new DuplicateResourceException("Cours", "titre", request.getTitle());
        });
        
        Course course = courseMapper.toEntity(request);
        Course savedCourse = courseRepository.save(course);
        log.info("Cours créé avec succès: ID {}", savedCourse.getCourseId());
        
        return courseMapper.toResponse(savedCourse, "Cours créé avec succès");
    }
    
    @Cacheable(value = "courses", unless = "#result.isEmpty()")
    public List<CourseResponse> getAllCourses() {
        log.info("Récupération de tous les cours");
        return courseRepository.findAllWithChapters().stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "course", key = "#id")
    public CourseResponse getCourseById(Long id) {
        log.info("Récupération du cours avec ID: {}", id);
        Course course = courseRepository.findByIdWithChapters(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours", "id", id));
        return courseMapper.toResponse(course);
    }
    
    @Cacheable(value = "coursesByTrainer", key = "#trainerId")
    public List<CourseResponse> getCoursesByTrainer(Long trainerId) {
        log.info("Récupération des cours du trainer avec ID: {}", trainerId);
        return courseRepository.findByTrainerId(trainerId).stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public Page<CourseResponse> getAllCoursesPaginated(Pageable pageable) {
        log.info("Récupération paginée des cours: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        return courseRepository.findAll(pageable)
                .map(courseMapper::toResponse);
    }
    
    @Transactional
    @CachePut(value = "course", key = "#id")
    @CacheEvict(value = {"courses", "coursesByTrainer"}, allEntries = true)
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        log.info("Mise à jour du cours avec ID: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours", "id", id));
        
        courseMapper.updateEntityFromRequest(course, request);
        Course updatedCourse = courseRepository.save(course);
        log.info("Cours mis à jour avec succès: ID {}", updatedCourse.getCourseId());
        
        return courseMapper.toResponse(updatedCourse, "Cours mis à jour avec succès");
    }
    
    @Transactional
    @CacheEvict(value = {"course", "courses", "coursesByTrainer"}, allEntries = true)
    public void deleteCourse(Long id) {
        log.info("Suppression du cours avec ID: {}", id);
        
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cours", "id", id);
        }
        
        courseRepository.deleteById(id);
        log.info("Cours supprimé avec succès: ID {}", id);
    }
    
    public CourseStatsResponse getCourseStatsByUserId(Long userId) {
        log.info("Récupération des statistiques de cours pour l'utilisateur: {}", userId);
        
        // Récupérer tous les cours complétés par l'utilisateur
        List<CourseCompletion> completions = courseCompletionRepository.findByUserId(userId);
        
        // Pour obtenir le nombre total de cours "enrolled", on doit récupérer les formations
        // auxquelles l'utilisateur est inscrit et compter leurs cours
        int totalEnrolled = 0;
        int totalChapters = 0;
        int completedChapters = 0;
        
        try {
            // Récupérer les formations de l'utilisateur via le TrainingClient
            List<TrainingResponse> userTrainings = trainingClient.getUserTrainings(userId);
            
            // Compter tous les cours uniques dans ces formations
            Set<Long> uniqueCourseIds = new HashSet<>();
            for (TrainingResponse training : userTrainings) {
                if (training.getCourseIds() != null) {
                    uniqueCourseIds.addAll(training.getCourseIds());
                }
            }
            totalEnrolled = uniqueCourseIds.size();
            
            // Calculer les chapitres pour tous les cours enrolled
            for (Long courseId : uniqueCourseIds) {
                Course course = courseRepository.findById(courseId).orElse(null);
                if (course != null && course.getChapters() != null) {
                    int chapterCount = course.getChapters().size();
                    totalChapters += chapterCount;
                    
                    // Si le cours est complété, tous ses chapitres le sont
                    boolean isCompleted = completions.stream()
                            .anyMatch(c -> c.getCourseId().equals(courseId));
                    if (isCompleted) {
                        completedChapters += chapterCount;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des formations: {}", e.getMessage());
            // Fallback: utiliser uniquement les cours complétés
            totalEnrolled = completions.size();
            for (CourseCompletion completion : completions) {
                Course course = courseRepository.findById(completion.getCourseId()).orElse(null);
                if (course != null && course.getChapters() != null) {
                    int chapterCount = course.getChapters().size();
                    totalChapters += chapterCount;
                    completedChapters += chapterCount;
                }
            }
        }
        
        int completed = completions.size();
        int inProgress = Math.max(0, totalEnrolled - completed); // Ensure non-negative
        
        // Calculate completion rate as percentage (0-100)
        double completionRate = totalEnrolled > 0 ? (completed * 100.0 / totalEnrolled) : 0.0;
        // Round to 2 decimal places
        completionRate = Math.round(completionRate * 100.0) / 100.0;
        
        return CourseStatsResponse.builder()
                .userId(userId)
                .totalEnrolled(totalEnrolled)
                .inProgress(inProgress)
                .completed(completed)
                .completionRate(completionRate)
                .totalChapters(totalChapters)
                .completedChapters(completedChapters)
                .build();
    }
    
    public CourseStatsResponse getUserCourseStats(Long userId) {
        log.info("Calcul des statistiques de cours pour l'utilisateur: {}", userId);
        
        List<CourseCompletion> completions = courseCompletionRepository.findByUserId(userId);
        
        int totalEnrolled = completions.size();
        int completed = completions.size(); // All entries in CourseCompletion are completed courses
        int inProgress = 0; // Would need a separate enrollment table to track in-progress courses
        
        double completionRate = totalEnrolled > 0 ? 100.0 : 0.0; // All enrolled courses are completed
        
        // For chapters, we need to count from the actual courses
        int totalChapters = 0;
        int completedChapters = 0;
        
        for (CourseCompletion completion : completions) {
            Course course = courseRepository.findById(completion.getCourseId()).orElse(null);
            if (course != null && course.getChapters() != null) {
                int courseChapters = course.getChapters().size();
                totalChapters += courseChapters;
                completedChapters += courseChapters; // All chapters completed if course is completed
            }
        }
        
        return CourseStatsResponse.builder()
                .userId(userId)
                .totalEnrolled(totalEnrolled)
                .inProgress(inProgress)
                .completed(completed)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .totalChapters(totalChapters)
                .completedChapters(completedChapters)
                .build();
    }
}
