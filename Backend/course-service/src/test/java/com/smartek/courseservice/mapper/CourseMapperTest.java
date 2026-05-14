package com.smartek.courseservice.mapper;

import com.smartek.courseservice.dto.ChapterResponse;
import com.smartek.courseservice.dto.CourseRequest;
import com.smartek.courseservice.dto.CourseResponse;
import com.smartek.courseservice.entity.Chapter;
import com.smartek.courseservice.entity.Course;
import com.smartek.courseservice.entity.DeliveryMode;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseMapper - Tests unitaires")
class CourseMapperTest {

    @Mock
    private LiveSessionMapper liveSessionMapper;

    @InjectMocks
    private CourseMapper courseMapper;

    private CourseRequest sampleRequest;
    private Course sampleCourse;

    @BeforeEach
    void setUp() {
        sampleRequest = CourseRequest.builder()
                .title("Spring Boot Avancé")
                .content("Contenu du cours")
                .duration(LocalDate.of(2026, 12, 31))
                .trainerId(10L)
                .deliveryMode(DeliveryMode.EN_LIGNE)
                .build();

        sampleCourse = Course.builder()
                .courseId(1L)
                .title("Spring Boot Avancé")
                .content("Contenu du cours")
                .duration(LocalDate.of(2026, 12, 31))
                .trainerId(10L)
                .deliveryMode(DeliveryMode.EN_LIGNE)
                .chapters(new ArrayList<>())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // toEntity
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("toEntity()")
    class ToEntity {

        @Test
        @DisplayName("Mappe correctement un CourseRequest en Course")
        void shouldMapRequestToEntity() {
            Course result = courseMapper.toEntity(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Spring Boot Avancé");
            assertThat(result.getContent()).isEqualTo("Contenu du cours");
            assertThat(result.getTrainerId()).isEqualTo(10L);
            assertThat(result.getDeliveryMode()).isEqualTo(DeliveryMode.EN_LIGNE);
        }

        @Test
        @DisplayName("Retourne null si la requête est null")
        void nullRequest_returnsNull() {
            assertThat(courseMapper.toEntity(null)).isNull();
        }

        @Test
        @DisplayName("Utilise PRESENTIEL par défaut si deliveryMode est null")
        void nullDeliveryMode_defaultsToPresentiel() {
            sampleRequest.setDeliveryMode(null);
            Course result = courseMapper.toEntity(sampleRequest);
            assertThat(result.getDeliveryMode()).isEqualTo(DeliveryMode.PRESENTIEL);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // toResponse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("toResponse()")
    class ToResponse {

        @Test
        @DisplayName("Mappe correctement un Course en CourseResponse")
        void shouldMapCourseToResponse() {
            CourseResponse result = courseMapper.toResponse(sampleCourse);

            assertThat(result).isNotNull();
            assertThat(result.getCourseId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Spring Boot Avancé");
            assertThat(result.getTrainerId()).isEqualTo(10L);
            assertThat(result.getDeliveryMode()).isEqualTo(DeliveryMode.EN_LIGNE);
        }

        @Test
        @DisplayName("Retourne null si le cours est null")
        void nullCourse_returnsNull() {
            assertThat(courseMapper.toResponse((Course) null)).isNull();
        }

        @Test
        @DisplayName("Inclut le message si fourni")
        void withMessage_includesMessage() {
            CourseResponse result = courseMapper.toResponse(sampleCourse, "Cours créé avec succès");
            assertThat(result.getMessage()).isEqualTo("Cours créé avec succès");
        }

        @Test
        @DisplayName("Mappe les chapitres correctement")
        void withChapters_mapsChapters() {
            Chapter chapter = Chapter.builder()
                    .chapterId(1L)
                    .title("Chapitre 1")
                    .description("Introduction")
                    .orderIndex(1)
                    .course(sampleCourse)
                    .build();
            sampleCourse.setChapters(List.of(chapter));

            CourseResponse result = courseMapper.toResponse(sampleCourse);

            assertThat(result.getChapters()).hasSize(1);
            assertThat(result.getChapters().get(0).getTitle()).isEqualTo("Chapitre 1");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // toChapterResponse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("toChapterResponse()")
    class ToChapterResponse {

        @Test
        @DisplayName("Mappe correctement un Chapter en ChapterResponse")
        void shouldMapChapterToResponse() {
            Chapter chapter = Chapter.builder()
                    .chapterId(5L)
                    .title("Chapitre 1")
                    .description("Introduction à Spring")
                    .orderIndex(1)
                    .pdfFileName("intro.pdf")
                    .course(sampleCourse)
                    .build();

            ChapterResponse result = courseMapper.toChapterResponse(chapter);

            assertThat(result.getChapterId()).isEqualTo(5L);
            assertThat(result.getTitle()).isEqualTo("Chapitre 1");
            assertThat(result.getCourseId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Retourne null si le chapitre est null")
        void nullChapter_returnsNull() {
            assertThat(courseMapper.toChapterResponse(null)).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateEntityFromRequest
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateEntityFromRequest()")
    class UpdateEntityFromRequest {

        @Test
        @DisplayName("Met à jour les champs du cours depuis la requête")
        void shouldUpdateCourseFields() {
            CourseRequest updateRequest = CourseRequest.builder()
                    .title("Nouveau titre")
                    .content("Nouveau contenu")
                    .duration(LocalDate.of(2027, 6, 30))
                    .deliveryMode(DeliveryMode.PRESENTIEL)
                    .build();

            courseMapper.updateEntityFromRequest(sampleCourse, updateRequest);

            assertThat(sampleCourse.getTitle()).isEqualTo("Nouveau titre");
            assertThat(sampleCourse.getContent()).isEqualTo("Nouveau contenu");
            assertThat(sampleCourse.getDeliveryMode()).isEqualTo(DeliveryMode.PRESENTIEL);
        }

        @Test
        @DisplayName("Ne plante pas si cours ou requête est null")
        void nullInputs_noException() {
            assertThatCode(() -> courseMapper.updateEntityFromRequest(null, sampleRequest))
                    .doesNotThrowAnyException();
            assertThatCode(() -> courseMapper.updateEntityFromRequest(sampleCourse, null))
                    .doesNotThrowAnyException();
        }
    }
}
