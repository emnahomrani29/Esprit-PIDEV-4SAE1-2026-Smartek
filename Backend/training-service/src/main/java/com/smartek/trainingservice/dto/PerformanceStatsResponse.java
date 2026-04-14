package com.smartek.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceStatsResponse {
    private CourseStats courses;
    private TrainingStats trainings;
    private ExamStats exams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStats {
        private int totalEnrolled;
        private int inProgress;
        private int completed;
        private double completionRate;
        private int totalChapters;
        private int completedChapters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainingStats {
        private int totalEnrolled;
        private int inProgress;
        private int completed;
        private double averageProgress;
        private Map<String, Integer> statusBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamStats {
        private int totalAvailable;
        private int attempted;
        private int passed;
        private int failed;
        private double averageScore;
        private double successRate;
        private int totalAttempts;
    }
}
