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
import com.smartek.courseservice.repository.LiveSessionRepository;
import com.smartek.courseservice.client.TrainingClient;
import com.smartek.courseservice.client.dto.TrainingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final LiveSessionRepository liveSessionRepository;
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
    
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        log.info("Récupération de tous les cours");
        return courseRepository.findAllWithChapters().stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        log.info("Récupération du cours avec ID: {}", id);
        Course course = courseRepository.findByIdWithChapters(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours", "id", id));
        return courseMapper.toResponse(course);
    }
    
    @Transactional(readOnly = true)
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

        // Supprimer d'abord les live_sessions via SQL natif pour contourner
        // le problème de contrainte FK avec le batch Hibernate
        liveSessionRepository.deleteByCourseId(id);
        log.info("Live sessions du cours {} supprimées", id);

        // Supprimer le cours (chapters supprimés en cascade par orphanRemoval)
        courseRepository.deleteById(id);
        log.info("Cours supprimé avec succès: ID {}", id);
    }
    
    @Transactional(readOnly = true)
    public CourseStatsResponse getCourseStatsByUserId(Long userId) {
        log.info("Récupération des statistiques de cours pour l'utilisateur: {}", userId);

        List<CourseCompletion> completions = courseCompletionRepository.findByUserId(userId);
        int completed = completions.size();

        int totalEnrolled = 0;
        int totalChapters = 0;
        int completedChapters = 0;

        try {
            List<TrainingResponse> userTrainings = trainingClient.getUserTrainings(userId);

            Set<Long> uniqueCourseIds = userTrainings.stream()
                    .filter(t -> t.getCourseIds() != null)
                    .flatMap(t -> t.getCourseIds().stream())
                    .collect(Collectors.toSet());
            totalEnrolled = uniqueCourseIds.size();

            // Batch-fetch all courses in one query
            Set<Long> completedIds = completions.stream()
                    .map(CourseCompletion::getCourseId)
                    .collect(Collectors.toSet());

            List<Course> enrolledCourses = courseRepository.findAllById(uniqueCourseIds);
            for (Course course : enrolledCourses) {
                int chapterCount = course.getChapters() != null ? course.getChapters().size() : 0;
                totalChapters += chapterCount;
                if (completedIds.contains(course.getCourseId())) {
                    completedChapters += chapterCount;
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des formations: {}", e.getMessage());
            totalEnrolled = completed;
            List<Course> completedCourses = courseRepository.findAllById(
                    completions.stream().map(CourseCompletion::getCourseId).collect(Collectors.toList()));
            for (Course course : completedCourses) {
                int chapterCount = course.getChapters() != null ? course.getChapters().size() : 0;
                totalChapters += chapterCount;
                completedChapters += chapterCount;
            }
        }

        int inProgress = Math.max(0, totalEnrolled - completed);
        double completionRate = totalEnrolled > 0 ? Math.round((completed * 100.0 / totalEnrolled) * 100.0) / 100.0 : 0.0;

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
    
    @Transactional(readOnly = true)
    public CourseStatsResponse getUserCourseStats(Long userId) {
        log.info("Calcul des statistiques de cours pour l'utilisateur: {}", userId);

        List<CourseCompletion> completions = courseCompletionRepository.findByUserId(userId);
        if (completions.isEmpty()) {
            return CourseStatsResponse.builder()
                    .userId(userId).totalEnrolled(0).inProgress(0)
                    .completed(0).completionRate(0.0).totalChapters(0).completedChapters(0)
                    .build();
        }

        // Batch-fetch all completed courses in one query instead of N individual queries
        List<Long> completedCourseIds = completions.stream()
                .map(CourseCompletion::getCourseId)
                .collect(Collectors.toList());

        List<Course> courses = courseRepository.findAllById(completedCourseIds);
        int totalChapters = courses.stream()
                .mapToInt(c -> c.getChapters() != null ? c.getChapters().size() : 0)
                .sum();

        int completed = completions.size();
        return CourseStatsResponse.builder()
                .userId(userId)
                .totalEnrolled(completed)
                .inProgress(0)
                .completed(completed)
                .completionRate(100.0)
                .totalChapters(totalChapters)
                .completedChapters(totalChapters)
                .build();
    }
}
