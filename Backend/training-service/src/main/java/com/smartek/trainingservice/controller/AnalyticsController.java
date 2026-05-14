package com.smartek.trainingservice.controller;

import com.smartek.trainingservice.dto.TrainingAnalyticsResponse;
import com.smartek.trainingservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/trainer/{trainerId}/training-enrollments")
    public ResponseEntity<List<TrainingAnalyticsResponse>> getTrainerTrainingAnalytics(@PathVariable Long trainerId) {
        log.info("Fetching training enrollment analytics for trainer: {}", trainerId);
        List<TrainingAnalyticsResponse> analytics = analyticsService.getTrainerTrainingAnalytics(trainerId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/trainer/{trainerId}/trainings")
    public ResponseEntity<List<TrainingAnalyticsResponse>> getTrainingAnalytics(@PathVariable Long trainerId) {
        log.info("Fetching training analytics for trainer: {}", trainerId);
        List<TrainingAnalyticsResponse> analytics = analyticsService.getTrainerTrainingAnalytics(trainerId);
        return ResponseEntity.ok(analytics);
    }
}
