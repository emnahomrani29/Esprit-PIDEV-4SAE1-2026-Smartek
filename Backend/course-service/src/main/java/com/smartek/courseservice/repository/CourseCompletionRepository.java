package com.smartek.courseservice.repository;

import com.smartek.courseservice.entity.CourseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseCompletionRepository extends JpaRepository<CourseCompletion, Long> {
    
    Optional<CourseCompletion> findByUserIdAndCourseId(Long userId, Long courseId);
    
    List<CourseCompletion> findByUserId(Long userId);
    
    List<CourseCompletion> findByCourseId(Long courseId);
    
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    
    long countByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);
    
    int countByUserId(Long userId);
}
