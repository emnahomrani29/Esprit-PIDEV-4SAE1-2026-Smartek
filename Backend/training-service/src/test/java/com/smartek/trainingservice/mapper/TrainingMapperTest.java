package com.smartek.trainingservice.mapper;

import com.smartek.trainingservice.dto.TrainingRequest;
import com.smartek.trainingservice.dto.TrainingResponse;
import com.smartek.trainingservice.entity.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrainingMapper - Tests unitaires")
class TrainingMapperTest {

    private TrainingMapper trainingMapper;

    @BeforeEach
    void setUp() {
        trainingMapper = new TrainingMapper();
    }

    private TrainingRequest buildRequest() {
        return TrainingRequest.builder()
                .title("Formation Spring Boot")
                .description("Microservices avec Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(List.of(1L, 2L))
                .createdBy(10L)
                .build();
    }

    private Training buildTraining() {
        return Training.builder()
                .trainingId(1L)
                .title("Formation Spring Boot")
                .description("Microservices avec Spring Boot")
                .category("Backend")
                .level("Intermédiaire")
                .duration(LocalDate.now().plusMonths(3))
                .courseIds(List.of(1L, 2L))
                .createdBy(10L)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // toEntity
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("toEntity()")
    class ToEntity {

        @Test
        @DisplayName("Mappe correctement un TrainingRequest en Training")
        void shouldMapRequestToEntity() {
            Training result = trainingMapper.toEntity(buildRequest());

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Formation Spring Boot");
            assertThat(result.getCategory()).isEqualTo("Backend");
            assertThat(result.getLevel()).isEqualTo("Intermédiaire");
            assertThat(result.getCourseIds()).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("Retourne null si la requête est null")
        void nullRequest_returnsNull() {
            assertThat(trainingMapper.toEntity(null)).isNull();
        }

        @Test
        @DisplayName("courseIds vide si null dans la requête")
        void nullCourseIds_returnsEmptyList() {
            TrainingRequest req = buildRequest();
            req.setCourseIds(null);
            Training result = trainingMapper.toEntity(req);
            assertThat(result.getCourseIds()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // toResponse
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("toResponse()")
    class ToResponse {

        @Test
        @DisplayName("Mappe correctement un Training en TrainingResponse")
        void shouldMapTrainingToResponse() {
            TrainingResponse result = trainingMapper.toResponse(buildTraining());

            assertThat(result).isNotNull();
            assertThat(result.getTrainingId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Formation Spring Boot");
            assertThat(result.getCategory()).isEqualTo("Backend");
            assertThat(result.getCourseIds()).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("Retourne null si la formation est null")
        void nullTraining_returnsNull() {
            assertThat(trainingMapper.toResponse((Training) null)).isNull();
        }

        @Test
        @DisplayName("Inclut le message si fourni")
        void withMessage_includesMessage() {
            TrainingResponse result = trainingMapper.toResponse(buildTraining(), "Formation créée");
            assertThat(result.getMessage()).isEqualTo("Formation créée");
        }

        @Test
        @DisplayName("Message null si non fourni")
        void withoutMessage_messageIsNull() {
            TrainingResponse result = trainingMapper.toResponse(buildTraining());
            assertThat(result.getMessage()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateEntityFromRequest
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateEntityFromRequest()")
    class UpdateEntityFromRequest {

        @Test
        @DisplayName("Met à jour les champs de la formation")
        void shouldUpdateTrainingFields() {
            Training training = buildTraining();
            TrainingRequest updateReq = TrainingRequest.builder()
                    .title("Nouveau titre")
                    .description("Nouvelle description")
                    .category("DevOps")
                    .level("Avancé")
                    .duration(LocalDate.now().plusMonths(6))
                    .courseIds(List.of(3L, 4L))
                    .build();

            trainingMapper.updateEntityFromRequest(training, updateReq);

            assertThat(training.getTitle()).isEqualTo("Nouveau titre");
            assertThat(training.getCategory()).isEqualTo("DevOps");
            assertThat(training.getLevel()).isEqualTo("Avancé");
            assertThat(training.getCourseIds()).containsExactly(3L, 4L);
        }

        @Test
        @DisplayName("Ne plante pas si training ou requête est null")
        void nullInputs_noException() {
            assertThatCode(() -> trainingMapper.updateEntityFromRequest(null, buildRequest()))
                    .doesNotThrowAnyException();
            assertThatCode(() -> trainingMapper.updateEntityFromRequest(buildTraining(), null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Ne modifie pas courseIds si null dans la requête")
        void nullCourseIds_doesNotUpdateCourseIds() {
            Training training = buildTraining();
            TrainingRequest req = buildRequest();
            req.setCourseIds(null);

            trainingMapper.updateEntityFromRequest(training, req);

            assertThat(training.getCourseIds()).containsExactly(1L, 2L); // inchangé
        }
    }
}
