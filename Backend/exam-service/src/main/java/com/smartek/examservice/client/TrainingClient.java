package com.smartek.examservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "training-service")
public interface TrainingClient {
    
    @GetMapping("/api/trainings/enrollments/check-completion")
    Boolean hasCompletedAllCourses(@RequestParam Long userId, @RequestParam Long trainingId);
    
    @GetMapping("/api/trainings/{trainingId}")
    TrainingResponse getTraining(@PathVariable Long trainingId);
}
