package com.smartek.trainingservice.controller;

import com.smartek.trainingservice.dto.PerformanceStatsResponse;
import com.smartek.trainingservice.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Slf4j
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<PerformanceStatsResponse> getUserPerformanceStats(@PathVariable Long userId) {
        log.info("Récupération des statistiques de performance pour l'utilisateur {}", userId);
        PerformanceStatsResponse stats = performanceService.getUserPerformanceStats(userId);
        return ResponseEntity.ok(stats);
    }
}
