package com.smartek.courseservice.client;

import com.smartek.courseservice.client.dto.ProgressUpdateRequest;
import com.smartek.courseservice.client.dto.TrainingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "training-service")
public interface TrainingClient {
    
    @PutMapping("/api/trainings/enrollments/user/{userId}/training/{trainingId}/progress")
    void updateTrainingProgress(
            @PathVariable("userId") Long userId,
            @PathVariable("trainingId") Long trainingId,
            @RequestBody ProgressUpdateRequest request);
    
    @GetMapping("/api/trainings/{trainingId}")
    TrainingResponse getTraining(@PathVariable("trainingId") Long trainingId);
    
    @GetMapping("/api/trainings/by-course/{courseId}")
    List<TrainingResponse> getTrainingsByCourse(@PathVariable("courseId") Long courseId);
    
    @GetMapping("/api/trainings/enrollments/user/{userId}/details")
    List<TrainingResponse> getUserTrainings(@PathVariable("userId") Long userId);
}
