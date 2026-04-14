package com.smartek.courseservice.repository;

import com.smartek.courseservice.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByTitle(String title);
    
    @Query("SELECT c FROM Course c WHERE c.trainerId = :trainerId")
    List<Course> findByTrainerId(Long trainerId);
    
    @EntityGraph(attributePaths = {"chapters"})
    @Query("SELECT c FROM Course c WHERE c.courseId = :id")
    Optional<Course> findByIdWithChapters(Long id);
    
    @EntityGraph(attributePaths = {"chapters"})
    @Query("SELECT c FROM Course c")
    List<Course> findAllWithChapters();
    
    Page<Course> findAll(Pageable pageable);
    
    Page<Course> findByTrainerId(Long trainerId, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.trainerId = :trainerId")
    long countByTrainerId(Long trainerId);
}
