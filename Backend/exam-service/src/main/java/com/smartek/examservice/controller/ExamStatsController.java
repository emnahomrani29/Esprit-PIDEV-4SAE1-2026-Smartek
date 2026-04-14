package com.smartek.examservice.controller;

import com.smartek.examservice.dto.ExamStatsResponse;
import com.smartek.examservice.service.ExamStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams/stats")
@RequiredArgsConstructor
@Slf4j
public class ExamStatsController {
    
    private final ExamStatsService examStatsService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ExamStatsResponse> getUserExamStats(@PathVariable Long userId) {
        log.info("Requête de récupération des statistiques d'examens pour l'utilisateur: {}", userId);
        try {
            ExamStatsResponse stats = examStatsService.getUserExamStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération des statistiques: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
