package com.smartek.trainingservice.controller;

import com.smartek.trainingservice.dto.TrainingStatsResponse;
import com.smartek.trainingservice.service.TrainingEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training/stats")
@RequiredArgsConstructor
@Slf4j
public class TrainingStatsController {
    
    private final TrainingEnrollmentService trainingEnrollmentService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<TrainingStatsResponse> getTrainingStatsByUserId(@PathVariable Long userId) {
        log.info("Récupération des statistiques de formation pour l'utilisateur: {}", userId);
        TrainingStatsResponse stats = trainingEnrollmentService.getTrainingStatsByUserId(userId);
        return ResponseEntity.ok(stats);
    }
}
