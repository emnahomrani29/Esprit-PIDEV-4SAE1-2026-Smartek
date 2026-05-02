package com.smartek.examservice.service;

import com.smartek.examservice.dto.ExamStatsResponse;
import com.smartek.examservice.entity.ExamEnrollment;
import com.smartek.examservice.entity.ExamResult;
import com.smartek.examservice.repository.ExamEnrollmentRepository;
import com.smartek.examservice.repository.ExamRepository;
import com.smartek.examservice.repository.ExamResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExamStatsService - Tests unitaires")
class ExamStatsServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamEnrollmentRepository examEnrollmentRepository;

    @Mock
    private ExamResultRepository examResultRepository;

    @InjectMocks
    private ExamStatsService examStatsService;

    @BeforeEach
    void setUp() {
        when(examRepository.count()).thenReturn(5L);
    }

    @Test
    @DisplayName("Doit retourner des stats vides si aucune inscription ni résultat")
    void shouldReturnEmptyStatsWhenNoData() {
        when(examEnrollmentRepository.findByUserId(5L)).thenReturn(Collections.emptyList());
        when(examResultRepository.findByUserId(5L)).thenReturn(Collections.emptyList());

        ExamStatsResponse result = examStatsService.getUserExamStats(5L);

        assertThat(result.getUserId()).isEqualTo(5L);
        assertThat(result.getTotalAvailable()).isEqualTo(5);
        assertThat(result.getAttempted()).isZero();
        assertThat(result.getPassed()).isZero();
        assertThat(result.getFailed()).isZero();
        assertThat(result.getAverageScore()).isZero();
        assertThat(result.getSuccessRate()).isZero();
    }

    @Test
    @DisplayName("Doit calculer correctement les stats avec des résultats")
    void shouldCalculateStatsWithResults() {
        ExamEnrollment completed = ExamEnrollment.builder()
                .id(1L).userId(5L).isCompleted(true).isUnlocked(true).build();
        ExamEnrollment notCompleted = ExamEnrollment.builder()
                .id(2L).userId(5L).isCompleted(false).isUnlocked(true).build();

        ExamResult passed = new ExamResult();
        passed.setUserId(5L);
        passed.setObtainedMarks(80);
        passed.setTotalMarks(100);
        passed.setPercentage(80.0);
        passed.setPassed(true);

        ExamResult failed = new ExamResult();
        failed.setUserId(5L);
        failed.setObtainedMarks(40);
        failed.setTotalMarks(100);
        failed.setPercentage(40.0);
        failed.setPassed(false);

        when(examEnrollmentRepository.findByUserId(5L)).thenReturn(List.of(completed, notCompleted));
        when(examResultRepository.findByUserId(5L)).thenReturn(List.of(passed, failed));

        ExamStatsResponse result = examStatsService.getUserExamStats(5L);

        assertThat(result.getAttempted()).isEqualTo(1); // 1 enrollment completed
        assertThat(result.getPassed()).isEqualTo(1);
        assertThat(result.getFailed()).isZero();
        assertThat(result.getAverageScore()).isEqualTo(60.0); // (80+40)/2
        assertThat(result.getTotalAttempts()).isEqualTo(2);
    }

    @Test
    @DisplayName("Doit calculer le taux de succès correctement")
    void shouldCalculateSuccessRate() {
        ExamEnrollment e1 = ExamEnrollment.builder().id(1L).userId(5L).isCompleted(true).isUnlocked(true).build();
        ExamEnrollment e2 = ExamEnrollment.builder().id(2L).userId(5L).isCompleted(true).isUnlocked(true).build();

        ExamResult r1 = new ExamResult(); r1.setPercentage(90.0); r1.setPassed(true);
        ExamResult r2 = new ExamResult(); r2.setPercentage(30.0); r2.setPassed(false);

        when(examEnrollmentRepository.findByUserId(5L)).thenReturn(List.of(e1, e2));
        when(examResultRepository.findByUserId(5L)).thenReturn(List.of(r1, r2));

        ExamStatsResponse result = examStatsService.getUserExamStats(5L);

        assertThat(result.getSuccessRate()).isEqualTo(50.0); // 1 passed / 2 attempted
    }
}
