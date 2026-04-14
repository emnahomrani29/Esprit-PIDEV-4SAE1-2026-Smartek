package com.smartek.courseservice.service;

import com.smartek.courseservice.dto.ChapterRequest;
import com.smartek.courseservice.dto.ChapterResponse;
import com.smartek.courseservice.entity.Chapter;
import com.smartek.courseservice.entity.Course;
import com.smartek.courseservice.repository.ChapterRepository;
import com.smartek.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterService {
    
    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/pdfs/";
    
    @Transactional
    public ChapterResponse createChapter(Long courseId, ChapterRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé avec l'ID: " + courseId));
        
        Chapter chapter = Chapter.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .course(course)
                .build();
        
        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Chapitre créé avec succès: {}", savedChapter.getChapterId());
        
        return mapToResponse(savedChapter);
    }
    
    public List<ChapterResponse> getChaptersByCourse(Long courseId) {
        return chapterRepository.findByCourse_CourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public ChapterResponse getChapterById(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapitre non trouvé avec l'ID: " + chapterId));
        return mapToResponse(chapter);
    }
    
    @Transactional
    public ChapterResponse updateChapter(Long chapterId, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapitre non trouvé avec l'ID: " + chapterId));
        
        chapter.setTitle(request.getTitle());
        chapter.setDescription(request.getDescription());
        chapter.setOrderIndex(request.getOrderIndex());
        
        Chapter updatedChapter = chapterRepository.save(chapter);
        log.info("Chapitre mis à jour avec succès: {}", chapterId);
        
        return mapToResponse(updatedChapter);
    }
    
    @Transactional
    public ChapterResponse uploadPdf(Long chapterId, MultipartFile file) throws IOException {
        log.info("Début upload PDF - ChapterId: {}, FileName: {}, Size: {}", 
                 chapterId, file.getOriginalFilename(), file.getSize());
        
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapitre non trouvé avec l'ID: " + chapterId));
        
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }
        
        String contentType = file.getContentType();
        log.info("Content-Type du fichier: {}", contentType);
        
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("Seuls les fichiers PDF sont acceptés. Type reçu: " + contentType);
        }
        
        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(UPLOAD_DIR);
        log.info("Chemin d'upload: {}", uploadPath.toAbsolutePath());
        
        if (!Files.exists(uploadPath)) {
            log.info("Création du répertoire: {}", uploadPath);
            Files.createDirectories(uploadPath);
        }
        
        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(uniqueFilename);
        log.info("Sauvegarde du fichier vers: {}", filePath.toAbsolutePath());
        
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Mettre à jour le chapitre
        chapter.setPdfFileName(originalFilename);
        chapter.setPdfFilePath(filePath.toString());
        
        Chapter updatedChapter = chapterRepository.save(chapter);
        log.info("PDF uploadé avec succès pour le chapitre: {}", chapterId);
        
        return mapToResponse(updatedChapter);
    }
    
    @Transactional
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapitre non trouvé avec l'ID: " + chapterId));
        
        // Supprimer le fichier PDF s'il existe
        if (chapter.getPdfFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(chapter.getPdfFilePath()));
            } catch (IOException e) {
                log.error("Erreur lors de la suppression du fichier PDF: {}", e.getMessage());
            }
        }
        
        chapterRepository.delete(chapter);
        log.info("Chapitre supprimé avec succès: {}", chapterId);
    }
    
    public org.springframework.core.io.Resource getPdf(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapitre non trouvé avec l'ID: " + chapterId));
        
        if (chapter.getPdfFilePath() == null) {
            throw new RuntimeException("Aucun PDF disponible pour ce chapitre");
        }
        
        try {
            Path filePath = Paths.get(chapter.getPdfFilePath());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Le fichier PDF n'existe pas ou n'est pas lisible");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier PDF: " + e.getMessage());
        }
    }
    
    private ChapterResponse mapToResponse(Chapter chapter) {
        return ChapterResponse.builder()
                .chapterId(chapter.getChapterId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .orderIndex(chapter.getOrderIndex())
                .pdfFileName(chapter.getPdfFileName())
                .pdfFilePath(chapter.getPdfFilePath())
                .courseId(chapter.getCourse().getCourseId())
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt())
                .build();
    }
}
