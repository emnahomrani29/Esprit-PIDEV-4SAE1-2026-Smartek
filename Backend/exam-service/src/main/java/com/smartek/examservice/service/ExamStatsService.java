package com.smartek.examservice.service;

import com.smartek.examservice.dto.ExamStatsResponse;
import com.smartek.examservice.entity.Exam;
import com.smartek.examservice.entity.ExamEnrollment;
import com.smartek.examservice.entity.ExamResult;
import com.smartek.examservice.repository.ExamRepository;
import com.smartek.examservice.repository.ExamEnrollmentRepository;
import com.smartek.examservice.repository.ExamResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamStatsService {
    
    private final ExamRepository examRepository;
    private final ExamEnrollmentRepository examEnrollmentRepository;
    private final ExamResultRepository examResultRepository;
    
    public ExamStatsResponse getUserExamStats(Long userId) {
        log.info("Calcul des statistiques d'examens pour l'utilisateur: {}", userId);
        
        // Get all enrollments for the user
        List<ExamEnrollment> enrollments = examEnrollmentRepository.findByUserId(userId);
        
        // Get all results for the user
        List<ExamResult> results = examResultRepository.findByUserId(userId);
        
        // Calculate stats
        int totalAvailable = (int) examRepository.count();
        int attempted = (int) enrollments.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsCompleted()))
                .count();
        int passed = (int) results.stream()
                .filter(r -> Boolean.TRUE.equals(r.getPassed()))
                .count();
        int failed = attempted - passed;
        
        double averageScore = results.stream()
                .mapToDouble(r -> r.getPercentage() != null ? r.getPercentage() : 0.0)
                .average()
                .orElse(0.0);
        
        double successRate = attempted > 0 ? (passed * 100.0 / attempted) : 0.0;
        
        int totalAttempts = results.size(); // Each result is an attempt
        
        return ExamStatsResponse.builder()
                .userId(userId)
                .totalAvailable(totalAvailable)
                .attempted(attempted)
                .passed(passed)
                .failed(failed)
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .totalAttempts(totalAttempts)
                .build();
    }
}
