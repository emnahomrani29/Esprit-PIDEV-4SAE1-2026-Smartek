package com.smartek.courseservice.service;

import com.smartek.courseservice.dto.ChapterRequest;
import com.smartek.courseservice.dto.ChapterResponse;
import com.smartek.courseservice.entity.Chapter;
import com.smartek.courseservice.entity.Course;
import com.smartek.courseservice.entity.DeliveryMode;
import com.smartek.courseservice.exception.BadRequestException;
import com.smartek.courseservice.repository.ChapterRepository;
import com.smartek.courseservice.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChapterService - Tests unitaires")
class ChapterServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ChapterService chapterService;

    private Course presentielCourse;
    private Course onlineCourse;
    private Chapter chapter;
    private ChapterRequest request;

    @BeforeEach
    void setUp() {
        presentielCourse = Course.builder()
                .courseId(1L).title("Spring Boot").content("Contenu")
                .duration(LocalDate.now().plusMonths(3)).trainerId(1L)
                .deliveryMode(DeliveryMode.PRESENTIEL).chapters(new ArrayList<>())
                .build();

        onlineCourse = Course.builder()
                .courseId(2L).title("Spring Boot Online").content("Contenu")
                .duration(LocalDate.now().plusMonths(3)).trainerId(1L)
                .deliveryMode(DeliveryMode.EN_LIGNE).chapters(new ArrayList<>())
                .build();

        chapter = Chapter.builder()
                .chapterId(1L).title("Chapitre 1").description("Intro")
                .orderIndex(1).course(presentielCourse).build();

        request = new ChapterRequest();
        request.setTitle("Chapitre 1");
        request.setDescription("Intro");
        request.setOrderIndex(1);
    }

    @Nested
    @DisplayName("createChapter()")
    class CreateChapter {

        @Test
        @DisplayName("Doit créer un chapitre pour un cours PRESENTIEL")
        void shouldCreateChapterForPresentielCourse() {
            when(courseRepository.findById(1L)).thenReturn(Optional.of(presentielCourse));
            when(chapterRepository.save(any(Chapter.class))).thenReturn(chapter);

            ChapterResponse response = chapterService.createChapter(1L, request);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Chapitre 1");
            verify(chapterRepository, times(1)).save(any(Chapter.class));
        }

        @Test
        @DisplayName("Doit lever BadRequestException pour un cours EN_LIGNE")
        void shouldThrowForOnlineCourse() {
            when(courseRepository.findById(2L)).thenReturn(Optional.of(onlineCourse));

            assertThatThrownBy(() -> chapterService.createChapter(2L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cours en ligne");

            verify(chapterRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le cours n'existe pas")
        void shouldThrowWhenCourseNotFound() {
            when(courseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chapterService.createChapter(99L, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("getChaptersByCourse()")
    class GetChaptersByCourse {

        @Test
        @DisplayName("Doit retourner les chapitres d'un cours")
        void shouldReturnChaptersByCourse() {
            when(chapterRepository.findByCourse_CourseIdOrderByOrderIndexAsc(1L))
                    .thenReturn(List.of(chapter));

            List<ChapterResponse> result = chapterService.getChaptersByCourse(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Chapitre 1");
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun chapitre")
        void shouldReturnEmptyList() {
            when(chapterRepository.findByCourse_CourseIdOrderByOrderIndexAsc(99L))
                    .thenReturn(List.of());

            List<ChapterResponse> result = chapterService.getChaptersByCourse(99L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getChapterById()")
    class GetChapterById {

        @Test
        @DisplayName("Doit retourner le chapitre par ID")
        void shouldReturnChapterById() {
            when(chapterRepository.findById(1L)).thenReturn(Optional.of(chapter));

            ChapterResponse response = chapterService.getChapterById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getChapterId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le chapitre n'existe pas")
        void shouldThrowWhenNotFound() {
            when(chapterRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chapterService.getChapterById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("updateChapter()")
    class UpdateChapter {

        @Test
        @DisplayName("Doit mettre à jour un chapitre existant")
        void shouldUpdateChapter() {
            when(chapterRepository.findById(1L)).thenReturn(Optional.of(chapter));
            when(chapterRepository.save(any(Chapter.class))).thenReturn(chapter);

            ChapterResponse response = chapterService.updateChapter(1L, request);

            assertThat(response).isNotNull();
            verify(chapterRepository, times(1)).save(any(Chapter.class));
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le chapitre n'existe pas")
        void shouldThrowWhenNotFound() {
            when(chapterRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chapterService.updateChapter(99L, request))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("deleteChapter()")
    class DeleteChapter {

        @Test
        @DisplayName("Doit supprimer un chapitre sans PDF")
        void shouldDeleteChapterWithoutPdf() {
            when(chapterRepository.findById(1L)).thenReturn(Optional.of(chapter));

            chapterService.deleteChapter(1L);

            verify(chapterRepository, times(1)).delete(chapter);
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le chapitre n'existe pas")
        void shouldThrowWhenNotFound() {
            when(chapterRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chapterService.deleteChapter(99L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("getPdf()")
    class GetPdf {

        @Test
        @DisplayName("Doit lever RuntimeException si aucun PDF disponible")
        void shouldThrowWhenNoPdf() {
            when(chapterRepository.findById(1L)).thenReturn(Optional.of(chapter));

            assertThatThrownBy(() -> chapterService.getPdf(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Aucun PDF");
        }

        @Test
        @DisplayName("Doit lever RuntimeException si le chapitre n'existe pas")
        void shouldThrowWhenChapterNotFound() {
            when(chapterRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chapterService.getPdf(99L))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
