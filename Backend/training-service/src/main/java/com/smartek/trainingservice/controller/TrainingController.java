package com.smartek.trainingservice.controller;

import com.smartek.trainingservice.dto.TrainingRequest;
import com.smartek.trainingservice.dto.TrainingResponse;
import com.smartek.trainingservice.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Slf4j
public class TrainingController {
    
    private final TrainingService trainingService;
    
    @PostMapping
    public ResponseEntity<TrainingResponse> createTraining(@Valid @RequestBody TrainingRequest request) {
        log.info("Requête de création de formation reçue: {}", request.getTitle());
        try {
            TrainingResponse response = trainingService.createTraining(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création de la formation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(TrainingResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<TrainingResponse>> getAllTrainings() {
        log.info("Requête de récupération de toutes les formations");
        List<TrainingResponse> trainings = trainingService.getAllTrainings();
        return ResponseEntity.ok(trainings);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TrainingResponse> getTrainingById(@PathVariable Long id) {
        log.info("Requête de récupération de la formation avec ID: {}", id);
        try {
            TrainingResponse response = trainingService.getTrainingById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération de la formation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(TrainingResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TrainingResponse>> getTrainingsByCategory(@PathVariable String category) {
        log.info("Requête de récupération des formations de la catégorie: {}", category);
        List<TrainingResponse> trainings = trainingService.getTrainingsByCategory(category);
        return ResponseEntity.ok(trainings);
    }
    
    @GetMapping("/level/{level}")
    public ResponseEntity<List<TrainingResponse>> getTrainingsByLevel(@PathVariable String level) {
        log.info("Requête de récupération des formations du niveau: {}", level);
        List<TrainingResponse> trainings = trainingService.getTrainingsByLevel(level);
        return ResponseEntity.ok(trainings);
    }
    
    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<List<TrainingResponse>> getTrainingsByCourse(@PathVariable Long courseId) {
        log.info("Requête de récupération des formations contenant le cours: {}", courseId);
        List<TrainingResponse> trainings = trainingService.getTrainingsByCourseId(courseId);
        return ResponseEntity.ok(trainings);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TrainingResponse> updateTraining(
            @PathVariable Long id,
            @Valid @RequestBody TrainingRequest request) {
        log.info("Requête de mise à jour de la formation avec ID: {}", id);
        try {
            TrainingResponse response = trainingService.updateTraining(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de la formation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(TrainingResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @PostMapping("/{trainingId}/courses/{courseId}")
    public ResponseEntity<TrainingResponse> addCourseToTraining(
            @PathVariable Long trainingId,
            @PathVariable Long courseId) {
        log.info("Requête d'ajout du cours {} à la formation {}", courseId, trainingId);
        try {
            TrainingResponse response = trainingService.addCourseToTraining(trainingId, courseId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de l'ajout du cours: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(TrainingResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/{trainingId}/courses/{courseId}")
    public ResponseEntity<TrainingResponse> removeCourseFromTraining(
            @PathVariable Long trainingId,
            @PathVariable Long courseId) {
        log.info("Requête de suppression du cours {} de la formation {}", courseId, trainingId);
        try {
            TrainingResponse response = trainingService.removeCourseFromTraining(trainingId, courseId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression du cours: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(TrainingResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id) {
        log.info("Requête de suppression de la formation avec ID: {}", id);
        try {
            trainingService.deleteTraining(id);
            log.info("Formation {} supprimée avec succès", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de la formation {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Training Service is running");
    }
}
