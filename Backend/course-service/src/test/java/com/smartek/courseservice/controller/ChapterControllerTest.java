package com.smartek.courseservice.controller;

import com.smartek.courseservice.dto.ChapterRequest;
import com.smartek.courseservice.dto.ChapterResponse;
import com.smartek.courseservice.service.ChapterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ChapterController (sans Spring context).
 * Verifie la delegation au service et les codes HTTP retournes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChapterController - Tests unitaires")
class ChapterControllerTest {

    @Mock
    private ChapterService chapterService;

    @InjectMocks
    private ChapterController chapterController;

    private ChapterRequest validRequest;
    private ChapterResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = ChapterRequest.builder()
                .title("Introduction a Spring Boot")
                .description("Chapitre d'introduction")
                .orderIndex(1)
                .build();

        sampleResponse = ChapterResponse.builder()
                .chapterId(1L)
                .title("Introduction a Spring Boot")
                .description("Chapitre d'introduction")
                .orderIndex(1)
                .courseId(5L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createChapter
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/courses/{courseId}/chapters - Creation de chapitre")
    class CreateChapter {

        @Test
        @DisplayName("Doit creer un chapitre et retourner 201")
        void shouldCreateChapterAndReturn201() {
            when(chapterService.createChapter(eq(5L), any(ChapterRequest.class))).thenReturn(sampleResponse);

            ResponseEntity<ChapterResponse> response = chapterController.createChapter(5L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getChapterId()).isEqualTo(1L);
            assertThat(response.getBody().getTitle()).isEqualTo("Introduction a Spring Boot");
            verify(chapterService, times(1)).createChapter(eq(5L), any(ChapterRequest.class));
        }

        @Test
        @DisplayName("Doit deleguer la creation au service avec courseId et request corrects")
        void shouldDelegateToServiceWithCorrectParams() {
            when(chapterService.createChapter(5L, validRequest)).thenReturn(sampleResponse);

            chapterController.createChapter(5L, validRequest);

            verify(chapterService).createChapter(5L, validRequest);
        }

        @Test
        @DisplayName("Doit retourner 400 si le cours n'existe pas")
        void shouldReturn400WhenCourseNotFound() {
            when(chapterService.createChapter(eq(999L), any()))
                    .thenThrow(new RuntimeException("Cours non trouve avec l'ID: 999"));

            ResponseEntity<ChapterResponse> response = chapterController.createChapter(999L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("999");
        }

        @Test
        @DisplayName("Doit retourner 400 si le cours est en mode EN_LIGNE")
        void shouldReturn400WhenCourseIsOnline() {
            when(chapterService.createChapter(eq(5L), any()))
                    .thenThrow(new RuntimeException("Les chapitres ne peuvent pas etre ajoutes aux cours en ligne"));

            ResponseEntity<ChapterResponse> response = chapterController.createChapter(5L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).contains("en ligne");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getChaptersByCourse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/{courseId}/chapters - Liste des chapitres")
    class GetChaptersByCourse {

        @Test
        @DisplayName("Doit retourner la liste des chapitres avec 200")
        void shouldReturnChapterListWith200() {
            when(chapterService.getChaptersByCourse(5L)).thenReturn(List.of(sampleResponse));

            ResponseEntity<List<ChapterResponse>> response = chapterController.getChaptersByCourse(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTitle()).isEqualTo("Introduction a Spring Boot");
            verify(chapterService).getChaptersByCourse(5L);
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun chapitre")
        void shouldReturnEmptyListWhenNoChapters() {
            when(chapterService.getChaptersByCourse(5L)).thenReturn(Collections.emptyList());

            ResponseEntity<List<ChapterResponse>> response = chapterController.getChaptersByCourse(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Doit retourner plusieurs chapitres pour un cours")
        void shouldReturnMultipleChapters() {
            ChapterResponse ch2 = ChapterResponse.builder()
                    .chapterId(2L)
                    .title("Chapitre 2 - Injection de dependances")
                    .orderIndex(2)
                    .courseId(5L)
                    .build();
            when(chapterService.getChaptersByCourse(5L)).thenReturn(List.of(sampleResponse, ch2));

            ResponseEntity<List<ChapterResponse>> response = chapterController.getChaptersByCourse(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getChapterById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/{courseId}/chapters/{chapterId}")
    class GetChapterById {

        @Test
        @DisplayName("Doit retourner le chapitre par ID avec 200")
        void shouldReturnChapterByIdWith200() {
            when(chapterService.getChapterById(1L)).thenReturn(sampleResponse);

            ResponseEntity<ChapterResponse> response = chapterController.getChapterById(5L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getChapterId()).isEqualTo(1L);
            verify(chapterService).getChapterById(1L);
        }

        @Test
        @DisplayName("Doit retourner 404 si le chapitre n'existe pas")
        void shouldReturn404WhenChapterNotFound() {
            when(chapterService.getChapterById(999L))
                    .thenThrow(new RuntimeException("Chapitre non trouve avec l'ID: 999"));

            ResponseEntity<ChapterResponse> response = chapterController.getChapterById(5L, 999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().getMessage()).contains("999");
        }

        @Test
        @DisplayName("Doit utiliser le chapterId (pas le courseId) pour la recherche")
        void shouldUseChapterIdForLookup() {
            when(chapterService.getChapterById(1L)).thenReturn(sampleResponse);

            chapterController.getChapterById(5L, 1L);

            verify(chapterService).getChapterById(1L);
            verify(chapterService, never()).getChapterById(5L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateChapter
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/courses/{courseId}/chapters/{chapterId} - Mise a jour")
    class UpdateChapter {

        @Test
        @DisplayName("Doit mettre a jour un chapitre et retourner 200")
        void shouldUpdateChapterAndReturn200() {
            when(chapterService.updateChapter(eq(1L), any(ChapterRequest.class))).thenReturn(sampleResponse);

            ResponseEntity<ChapterResponse> response = chapterController.updateChapter(5L, 1L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(chapterService).updateChapter(eq(1L), any(ChapterRequest.class));
        }

        @Test
        @DisplayName("Doit retourner 400 si le chapitre n'existe pas")
        void shouldReturn400WhenChapterNotFound() {
            when(chapterService.updateChapter(eq(999L), any()))
                    .thenThrow(new RuntimeException("Chapitre non trouve avec l'ID: 999"));

            ResponseEntity<ChapterResponse> response = chapterController.updateChapter(5L, 999L, validRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).contains("999");
        }

        @Test
        @DisplayName("Doit deleguer la mise a jour au service avec le chapterId correct")
        void shouldDelegateWithCorrectChapterId() {
            when(chapterService.updateChapter(1L, validRequest)).thenReturn(sampleResponse);

            chapterController.updateChapter(5L, 1L, validRequest);

            verify(chapterService).updateChapter(1L, validRequest);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // uploadPdf
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/courses/{courseId}/chapters/{chapterId}/upload-pdf")
    class UploadPdf {

        @Test
        @DisplayName("Doit uploader un PDF et retourner 200")
        void shouldUploadPdfAndReturn200() throws IOException {
            MockMultipartFile pdfFile = new MockMultipartFile(
                    "file", "cours.pdf", "application/pdf", "contenu pdf".getBytes());
            ChapterResponse responseWithPdf = ChapterResponse.builder()
                    .chapterId(1L)
                    .title("Introduction a Spring Boot")
                    .pdfFileName("cours.pdf")
                    .build();
            when(chapterService.uploadPdf(eq(1L), any())).thenReturn(responseWithPdf);

            ResponseEntity<ChapterResponse> response = chapterController.uploadPdf(5L, 1L, pdfFile);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getPdfFileName()).isEqualTo("cours.pdf");
            verify(chapterService).uploadPdf(eq(1L), any());
        }

        @Test
        @DisplayName("Doit retourner 500 si une IOException est levee")
        void shouldReturn500WhenIOExceptionThrown() throws IOException {
            MockMultipartFile pdfFile = new MockMultipartFile(
                    "file", "cours.pdf", "application/pdf", "contenu".getBytes());
            when(chapterService.uploadPdf(eq(1L), any()))
                    .thenThrow(new IOException("Erreur d'ecriture sur le disque"));

            ResponseEntity<ChapterResponse> response = chapterController.uploadPdf(5L, 1L, pdfFile);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getMessage()).contains("Erreur lors de l'upload");
        }

        @Test
        @DisplayName("Doit retourner 400 si le fichier n'est pas un PDF")
        void shouldReturn400WhenFileIsNotPdf() throws IOException {
            MockMultipartFile txtFile = new MockMultipartFile(
                    "file", "cours.txt", "text/plain", "contenu".getBytes());
            when(chapterService.uploadPdf(eq(1L), any()))
                    .thenThrow(new RuntimeException("Seuls les fichiers PDF sont acceptes"));

            ResponseEntity<ChapterResponse> response = chapterController.uploadPdf(5L, 1L, txtFile);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).contains("PDF");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteChapter
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/courses/{courseId}/chapters/{chapterId}")
    class DeleteChapter {

        @Test
        @DisplayName("Doit supprimer un chapitre et retourner 204")
        void shouldDeleteChapterAndReturn204() {
            doNothing().when(chapterService).deleteChapter(1L);

            ResponseEntity<Void> response = chapterController.deleteChapter(5L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(chapterService).deleteChapter(1L);
        }

        @Test
        @DisplayName("Doit retourner 404 si le chapitre n'existe pas")
        void shouldReturn404WhenChapterNotFound() {
            doThrow(new RuntimeException("Chapitre non trouve avec l'ID: 999"))
                    .when(chapterService).deleteChapter(999L);

            ResponseEntity<Void> response = chapterController.deleteChapter(5L, 999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Doit deleguer la suppression au service avec le chapterId correct")
        void shouldDelegateWithCorrectChapterId() {
            doNothing().when(chapterService).deleteChapter(1L);

            chapterController.deleteChapter(5L, 1L);

            verify(chapterService).deleteChapter(1L);
            verify(chapterService, never()).deleteChapter(5L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPdf
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/courses/{courseId}/chapters/{chapterId}/pdf")
    class GetPdf {

        @Test
        @DisplayName("Doit retourner le PDF avec 200 et content-type application/pdf")
        void shouldReturnPdfWith200() {
            Resource mockResource = mock(Resource.class);
            when(mockResource.getFilename()).thenReturn("cours.pdf");
            when(chapterService.getPdf(1L)).thenReturn(mockResource);

            ResponseEntity<Resource> response = chapterController.getPdf(5L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType())
                    .isEqualTo(org.springframework.http.MediaType.APPLICATION_PDF);
            verify(chapterService).getPdf(1L);
        }

        @Test
        @DisplayName("Doit retourner 404 si aucun PDF disponible pour ce chapitre")
        void shouldReturn404WhenNoPdfAvailable() {
            when(chapterService.getPdf(1L))
                    .thenThrow(new RuntimeException("Aucun PDF disponible pour ce chapitre"));

            ResponseEntity<Resource> response = chapterController.getPdf(5L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Doit retourner 404 si le chapitre n'existe pas")
        void shouldReturn404WhenChapterNotFound() {
            when(chapterService.getPdf(999L))
                    .thenThrow(new RuntimeException("Chapitre non trouve avec l'ID: 999"));

            ResponseEntity<Resource> response = chapterController.getPdf(5L, 999L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
