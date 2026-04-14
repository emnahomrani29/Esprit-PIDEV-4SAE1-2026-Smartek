package com.smartek.examservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "course-service")
public interface CourseClient {
    
    @GetMapping("/api/courses/{id}")
    CourseResponse getCourse(@PathVariable Long id);
    
    @GetMapping("/api/courses/{courseId}/is-completed")
    Boolean isCourseCompleted(@PathVariable Long courseId, @RequestParam Long userId);
}
