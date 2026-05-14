package com.smartek.trainingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "exam-service")
public interface ExamClient {
    
    @GetMapping("/api/exams/stats/user/{userId}")
    Map<String, Object> getUserExamStats(@PathVariable Long userId);
    
    @PostMapping("/api/exam-enrollments/training/{trainingId}/user/{userId}")
    void enrollExamForTraining(@PathVariable Long trainingId, @PathVariable Long userId);
    
    @PostMapping("/api/exam-enrollments/course/{courseId}/user/{userId}")
    void enrollQuizForCourse(@PathVariable Long courseId, @PathVariable Long userId);
    
    @PutMapping("/api/exam-enrollments/training/{trainingId}/user/{userId}/unlock")
    void unlockExamForTraining(@PathVariable Long trainingId, @PathVariable Long userId);
    
    @PutMapping("/api/exam-enrollments/training/{trainingId}/user/{userId}/lock")
    void lockExamForTraining(@PathVariable Long trainingId, @PathVariable Long userId);
    
    @DeleteMapping("/api/exams/training/{trainingId}")
    void deleteExamsByTrainingId(@PathVariable Long trainingId);
    
    @DeleteMapping("/api/exams/course/{courseId}")
    void deleteQuizzesByCourseId(@PathVariable Long courseId);
}
