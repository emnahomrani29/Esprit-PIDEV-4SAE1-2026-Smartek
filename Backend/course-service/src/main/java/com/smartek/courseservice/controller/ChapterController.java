package com.smartek.courseservice.controller;

import com.smartek.courseservice.dto.ChapterRequest;
import com.smartek.courseservice.dto.ChapterResponse;
import com.smartek.courseservice.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/chapters")
@RequiredArgsConstructor
@Slf4j
public class ChapterController {
    
    private final ChapterService chapterService;
    
    @PostMapping
    public ResponseEntity<ChapterResponse> createChapter(
            @PathVariable Long courseId,
            @Valid @RequestBody ChapterRequest request) {
        log.info("Requête de création de chapitre pour le cours: {}", courseId);
        try {
            ChapterResponse response = chapterService.createChapter(courseId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création du chapitre: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ChapterResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ChapterResponse>> getChaptersByCourse(@PathVariable Long courseId) {
        log.info("Requête de récupération des chapitres du cours: {}", courseId);
        List<ChapterResponse> chapters = chapterService.getChaptersByCourse(courseId);
        return ResponseEntity.ok(chapters);
    }
    
    @GetMapping("/{chapterId}")
    public ResponseEntity<ChapterResponse> getChapterById(
            @PathVariable Long courseId,
            @PathVariable Long chapterId) {
        log.info("Requête de récupération du chapitre: {}", chapterId);
        try {
            ChapterResponse response = chapterService.getChapterById(chapterId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération du chapitre: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ChapterResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @PutMapping("/{chapterId}")
    public ResponseEntity<ChapterResponse> updateChapter(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterRequest request) {
        log.info("Requête de mise à jour du chapitre: {}", chapterId);
        try {
            ChapterResponse response = chapterService.updateChapter(chapterId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour du chapitre: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ChapterResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @PostMapping(value = "/{chapterId}/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChapterResponse> uploadPdf(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @RequestParam("file") MultipartFile file) {
        log.info("Requête d'upload de PDF pour le chapitre: {}", chapterId);
        try {
            ChapterResponse response = chapterService.uploadPdf(chapterId, file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Erreur lors de l'upload du PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChapterResponse.builder()
                            .message("Erreur lors de l'upload du fichier")
                            .build());
        } catch (RuntimeException e) {
            log.error("Erreur lors de l'upload du PDF: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ChapterResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/{chapterId}")
    public ResponseEntity<Void> deleteChapter(
            @PathVariable Long courseId,
            @PathVariable Long chapterId) {
        log.info("Requête de suppression du chapitre: {}", chapterId);
        try {
            chapterService.deleteChapter(chapterId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression du chapitre: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{chapterId}/pdf")
    public ResponseEntity<org.springframework.core.io.Resource> getPdf(
            @PathVariable Long courseId,
            @PathVariable Long chapterId) {
        log.info("Requête de téléchargement du PDF pour le chapitre: {}", chapterId);
        try {
            org.springframework.core.io.Resource resource = chapterService.getPdf(chapterId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (RuntimeException e) {
            log.error("Erreur lors du téléchargement du PDF: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
