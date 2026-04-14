package com.smartek.courseservice.controller;

import com.smartek.courseservice.dto.CourseStatsResponse;
import com.smartek.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/stats")
@RequiredArgsConstructor
@Slf4j
public class CourseStatsController {
    
    private final CourseService courseService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<CourseStatsResponse> getCourseStatsByUserId(@PathVariable Long userId) {
        log.info("Récupération des statistiques de cours pour l'utilisateur: {}", userId);
        CourseStatsResponse stats = courseService.getCourseStatsByUserId(userId);
        return ResponseEntity.ok(stats);
    }
}
