package com.smartek.courseservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "exam-service")
public interface ExamClient {
    
    @PostMapping("/api/exam-enrollments/unlock-quiz")
    String unlockQuizForCourse(@RequestParam Long userId, @RequestParam Long courseId);
    
    @PostMapping("/api/exam-enrollments/lock-quiz")
    String lockQuizForCourse(@RequestParam Long userId, @RequestParam Long courseId);
    
    @PostMapping("/api/exam-enrollments/unlock")
    String unlockExamForCourse(@RequestParam Long userId, @RequestParam Long courseId);
}
