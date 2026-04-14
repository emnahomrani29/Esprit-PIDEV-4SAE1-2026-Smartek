package com.smartek.courseservice.controller;

import com.smartek.courseservice.dto.CourseRequest;
import com.smartek.courseservice.dto.CourseResponse;
import com.smartek.courseservice.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {
    
    private final CourseService courseService;
    
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        log.info("Requête de création de cours reçue: {}", request.getTitle());
        try {
            CourseResponse response = courseService.createCourse(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création du cours: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(CourseResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        log.info("Requête de récupération de tous les cours");
        List<CourseResponse> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        log.info("Requête de récupération du cours avec ID: {}", id);
        try {
            CourseResponse response = courseService.getCourseById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération du cours: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CourseResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<CourseResponse>> getCoursesByTrainer(@PathVariable Long trainerId) {
        log.info("Requête de récupération des cours du trainer avec ID: {}", trainerId);
        List<CourseResponse> courses = courseService.getCoursesByTrainer(trainerId);
        return ResponseEntity.ok(courses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        log.info("Requête de mise à jour du cours avec ID: {}", id);
        try {
            CourseResponse response = courseService.updateCourse(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour du cours: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(CourseResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.info("Requête de suppression du cours avec ID: {}", id);
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression du cours: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Course Service is running");
    }
}
