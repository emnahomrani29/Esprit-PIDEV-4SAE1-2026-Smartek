package com.smartek.trainingservice.service;

import com.smartek.trainingservice.client.CourseClient;
import com.smartek.trainingservice.client.CourseResponse;
import com.smartek.trainingservice.client.ChapterResponse;
import com.smartek.trainingservice.client.ExamClient;
import com.smartek.trainingservice.dto.TrainingRequest;
import com.smartek.trainingservice.dto.TrainingResponse;
import com.smartek.trainingservice.entity.Training;
import com.smartek.trainingservice.repository.TrainingRepository;
import com.smartek.trainingservice.repository.TrainingEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingService {
    
    private final TrainingRepository trainingRepository;
    private final CourseClient courseClient;
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final ExamClient examClient;
    
    @Transactional
    @CacheEvict(value = {"trainings", "trainingsByCategory", "trainingsByLevel"}, allEntries = true)
    public TrainingResponse createTraining(TrainingRequest request) {
        log.info("Création d'une nouvelle formation: {}", request.getTitle());
        
        trainingRepository.findByTitle(request.getTitle()).ifPresent(t -> {
            throw new RuntimeException("Une formation avec ce titre existe déjà");
        });
        
        Training training = Training.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .level(request.getLevel())
                .duration(request.getDuration())
                .courseIds(request.getCourseIds() != null ? request.getCourseIds() : List.of())
                .createdBy(request.getCreatedBy())
                .build();
        
        Training savedTraining = trainingRepository.save(training);
        log.info("Formation créée avec succès: ID {}", savedTraining.getTrainingId());
        
        return mapToResponse(savedTraining, "Formation créée avec succès");
    }
    
    @Cacheable(value = "trainings", unless = "#result.isEmpty()")
    public List<TrainingResponse> getAllTrainings() {
        log.info("Récupération de toutes les formations");
        return trainingRepository.findAll().stream()
                .map(training -> mapToResponse(training, null))
                .collect(Collectors.toList());
    }
    
    public Page<TrainingResponse> getAllTrainingsPaginated(Pageable pageable) {
        log.info("Récupération paginée des formations: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        return trainingRepository.findAll(pageable)
                .map(training -> mapToResponse(training, null));
    }
    
    @Cacheable(value = "training", key = "#id")
    public TrainingResponse getTrainingById(Long id) {
        log.info("Récupération de la formation avec ID: {}", id);
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));
        return mapToResponse(training, null);
    }
    
    @Cacheable(value = "trainingsByCategory", key = "#category")
    public List<TrainingResponse> getTrainingsByCategory(String category) {
        log.info("Récupération des formations de la catégorie: {}", category);
        return trainingRepository.findByCategory(category).stream()
                .map(training -> mapToResponse(training, null))
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "trainingsByLevel", key = "#level")
    public List<TrainingResponse> getTrainingsByLevel(String level) {
        log.info("Récupération des formations du niveau: {}", level);
        return trainingRepository.findByLevel(level).stream()
                .map(training -> mapToResponse(training, null))
                .collect(Collectors.toList());
    }
    
    public List<TrainingResponse> getTrainingsByCourseId(Long courseId) {
        log.info("Récupération des formations contenant le cours: {}", courseId);
        return trainingRepository.findByCourseId(courseId).stream()
                .map(training -> mapToResponse(training, null))
                .collect(Collectors.toList());
    }
    
    @Transactional
    @CachePut(value = "training", key = "#id")
    @CacheEvict(value = {"trainings", "trainingsByCategory", "trainingsByLevel"}, allEntries = true)
    public TrainingResponse updateTraining(Long id, TrainingRequest request) {
        log.info("Mise à jour de la formation avec ID: {}", id);
        
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));
        
        training.setTitle(request.getTitle());
        training.setDescription(request.getDescription());
        training.setCategory(request.getCategory());
        training.setLevel(request.getLevel());
        training.setDuration(request.getDuration());
        
        if (request.getCourseIds() != null) {
            training.setCourseIds(request.getCourseIds());
        }
        
        Training updatedTraining = trainingRepository.save(training);
        log.info("Formation mise à jour avec succès: ID {}", updatedTraining.getTrainingId());
        
        return mapToResponse(updatedTraining, "Formation mise à jour avec succès");
    }
    
    @Transactional
    @CachePut(value = "training", key = "#trainingId")
    @CacheEvict(value = {"trainings", "trainingsByCategory", "trainingsByLevel"}, allEntries = true)
    public TrainingResponse addCourseToTraining(Long trainingId, Long courseId) {
        log.info("Ajout du cours {} à la formation {}", courseId, trainingId);
        
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));
        
        if (!training.getCourseIds().contains(courseId)) {
            training.getCourseIds().add(courseId);
            trainingRepository.save(training);
        }
        
        return mapToResponse(training, "Cours ajouté avec succès");
    }
    
    @Transactional
    @CachePut(value = "training", key = "#trainingId")
    @CacheEvict(value = {"trainings", "trainingsByCategory", "trainingsByLevel"}, allEntries = true)
    public TrainingResponse removeCourseFromTraining(Long trainingId, Long courseId) {
        log.info("Suppression du cours {} de la formation {}", courseId, trainingId);
        
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));
        
        training.getCourseIds().remove(courseId);
        trainingRepository.save(training);
        
        return mapToResponse(training, "Cours supprimé avec succès");
    }
    
    @Transactional
    @Async
    @CacheEvict(value = {"training", "trainings", "trainingsByCategory", "trainingsByLevel"}, allEntries = true)
    public CompletableFuture<Void> deleteTraining(Long id) {
        log.info("Suppression de la formation avec ID: {}", id);
        
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'ID: " + id));
        
        // Supprimer tous les examens associés à cette formation
        try {
            log.info("Suppression des examens associés à la formation {}", id);
            examClient.deleteExamsByTrainingId(id);
            log.info("Examens supprimés avec succès pour la formation {}", id);
        } catch (Exception e) {
            log.warn("Erreur lors de la suppression des examens: {}", e.getMessage());
        }
        
        // Supprimer tous les quiz associés aux cours de cette formation
        if (training.getCourseIds() != null && !training.getCourseIds().isEmpty()) {
            for (Long courseId : training.getCourseIds()) {
                try {
                    log.info("Suppression des quiz du cours {} de la formation {}", courseId, id);
                    examClient.deleteQuizzesByCourseId(courseId);
                } catch (Exception e) {
                    log.warn("Erreur lors de la suppression des quiz du cours {}: {}", courseId, e.getMessage());
                }
            }
        }
        
        // Supprimer tous les enrollments associés
        trainingEnrollmentRepository.deleteByTrainingTrainingId(id);
        log.info("Enrollments associés supprimés pour la formation {}", id);
        
        // Maintenant supprimer la formation
        trainingRepository.delete(training);
        log.info("Formation supprimée avec succès: ID {}", id);
        
        return CompletableFuture.completedFuture(null);
    }
    
    private TrainingResponse mapToResponse(Training training, String message) {
        // Récupérer les informations détaillées des cours
        List<TrainingResponse.CourseInfo> courses = new ArrayList<>();
        if (!training.getCourseIds().isEmpty()) {
            for (Long courseId : training.getCourseIds()) {
                try {
                    CourseResponse courseResponse = courseClient.getCourseById(courseId);
                    List<ChapterResponse> chapterResponses = courseClient.getChaptersByCourseId(courseId);
                    
                    // Convertir les ChapterResponse en ChapterInfo
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
        
        return TrainingResponse.builder()
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
                .message(message)
                .build();
    }
}
