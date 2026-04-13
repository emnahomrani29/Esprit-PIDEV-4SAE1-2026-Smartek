package com.smartek.learningmicroservice.service;

import com.smartek.learningmicroservice.dto.LearningPathRequest;
import com.smartek.learningmicroservice.dto.LearningPathResponse;
import com.smartek.learningmicroservice.entity.LearningPath;
import com.smartek.learningmicroservice.entity.LearningPathStatus;
import com.smartek.learningmicroservice.repository.LearningPathRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningPathServiceTest {

    @Mock
    private LearningPathRepository pathRepository;

    @InjectMocks
    private LearningPathService service;

    private LearningPath path;
    private LearningPathRequest request;

    @BeforeEach
    void setUp() {
        path = LearningPath.builder()
                .pathId(1L)
                .title("Spring Boot Mastery")
                .description("Learn Spring Boot from scratch")
                .learnerId(5L)
                .learnerName("Bob Martin")
                .status(LearningPathStatus.EN_COURS)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .progress(40)
                .build();

        request = new LearningPathRequest(
                "Spring Boot Mastery",
                "Learn Spring Boot from scratch",
                5L,
                "Bob Martin",
                LearningPathStatus.EN_COURS,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30),
                40
        );
    }

    // ===== createPath =====

    @Test
    void createPath_success() {
        when(pathRepository.existsByLearnerIdAndTitle(5L, "Spring Boot Mastery")).thenReturn(false);
        when(pathRepository.save(any())).thenReturn(path);

        LearningPathResponse response = service.createPath(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Spring Boot Mastery");
        assertThat(response.getLearnerId()).isEqualTo(5L);
        verify(pathRepository).save(any(LearningPath.class));
    }

    @Test
    void createPath_duplicateTitle_throwsException() {
        when(pathRepository.existsByLearnerIdAndTitle(5L, "Spring Boot Mastery")).thenReturn(true);

        assertThatThrownBy(() -> service.createPath(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("existe déjà");

        verify(pathRepository, never()).save(any());
    }

    // ===== getPathById =====

    @Test
    void getPathById_found() {
        when(pathRepository.findById(1L)).thenReturn(Optional.of(path));

        LearningPathResponse response = service.getPathById(1L);

        assertThat(response.getPathId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(LearningPathStatus.EN_COURS);
    }

    @Test
    void getPathById_notFound_throwsException() {
        when(pathRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPathById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("non trouvé");
    }

    // ===== getAllPathsByLearner =====

    @Test
    void getAllPathsByLearner_returnsList() {
        when(pathRepository.findByLearnerIdOrderByStartDateDesc(5L)).thenReturn(List.of(path));

        List<LearningPathResponse> result = service.getAllPathsByLearner(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLearnerName()).isEqualTo("Bob Martin");
    }

    @Test
    void getAllPathsByLearner_empty_returnsEmptyList() {
        when(pathRepository.findByLearnerIdOrderByStartDateDesc(99L)).thenReturn(List.of());

        List<LearningPathResponse> result = service.getAllPathsByLearner(99L);

        assertThat(result).isEmpty();
    }

    // ===== getPathsByStatus =====

    @Test
    void getPathsByStatus_returnsFiltered() {
        when(pathRepository.findByStatus(LearningPathStatus.EN_COURS)).thenReturn(List.of(path));

        List<LearningPathResponse> result = service.getPathsByStatus(LearningPathStatus.EN_COURS);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(LearningPathStatus.EN_COURS);
    }

    // ===== updatePath =====

    @Test
    void updatePath_success() {
        when(pathRepository.findById(1L)).thenReturn(Optional.of(path));
        when(pathRepository.existsByLearnerIdAndTitleAndPathIdNot(5L, "Spring Boot Mastery", 1L)).thenReturn(false);
        when(pathRepository.save(any())).thenReturn(path);

        LearningPathResponse response = service.updatePath(1L, request);

        assertThat(response).isNotNull();
        verify(pathRepository).save(path);
    }

    @Test
    void updatePath_notFound_throwsException() {
        when(pathRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePath(99L, request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void updatePath_duplicateTitle_throwsException() {
        when(pathRepository.findById(1L)).thenReturn(Optional.of(path));
        when(pathRepository.existsByLearnerIdAndTitleAndPathIdNot(5L, "Spring Boot Mastery", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.updatePath(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("existe déjà");
    }

    // ===== deletePath =====

    @Test
    void deletePath_success() {
        when(pathRepository.existsById(1L)).thenReturn(true);

        service.deletePath(1L);

        verify(pathRepository).deleteById(1L);
    }

    @Test
    void deletePath_notFound_throwsException() {
        when(pathRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletePath(99L))
                .isInstanceOf(RuntimeException.class);
    }

    // ===== getPathsByLearnerAndStatus =====

    @Test
    void getPathsByLearnerAndStatus_returnsFiltered() {
        when(pathRepository.findByLearnerIdAndStatus(5L, LearningPathStatus.EN_COURS))
                .thenReturn(List.of(path));

        List<LearningPathResponse> result = service.getPathsByLearnerAndStatus(5L, LearningPathStatus.EN_COURS);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProgress()).isEqualTo(40);
    }
}
