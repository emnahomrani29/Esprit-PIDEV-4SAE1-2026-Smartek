package com.smartek.examservice.controller;

import com.smartek.examservice.dto.TrainerExamAnalyticsResponse;
import com.smartek.examservice.service.AnalyticsService;
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

    @GetMapping("/trainer/{trainerId}/exams")
    public ResponseEntity<List<TrainerExamAnalyticsResponse>> getTrainerExamAnalytics(@PathVariable Long trainerId) {
        log.info("Fetching exam analytics for trainer: {}", trainerId);
        List<TrainerExamAnalyticsResponse> analytics = analyticsService.getTrainerExamAnalytics(trainerId);
        return ResponseEntity.ok(analytics);
    }
}
