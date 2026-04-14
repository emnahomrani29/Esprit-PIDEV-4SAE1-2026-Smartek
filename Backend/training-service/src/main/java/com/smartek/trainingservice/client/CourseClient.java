package com.smartek.trainingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "course-service")
public interface CourseClient {
    
    @GetMapping("/api/courses/{courseId}/is-completed")
    Boolean isCourseCompleted(@PathVariable Long courseId, @RequestParam Long userId);
    
    @GetMapping("/api/courses/completed-count")
    Integer getCompletedCoursesCount(@RequestParam Long userId);
    
    @GetMapping("/api/courses/{courseId}")
    CourseResponse getCourseById(@PathVariable Long courseId);
    
    @GetMapping("/api/courses/{courseId}/chapters")
    List<ChapterResponse> getChaptersByCourseId(@PathVariable Long courseId);
}
