package com.smartek.trainingservice.repository;

import com.smartek.trainingservice.entity.Training;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    Optional<Training> findByTitle(String title);
    
    List<Training> findByCategory(String category);
    
    List<Training> findByLevel(String level);
    
    Page<Training> findAll(Pageable pageable);
    
    Page<Training> findByCategory(String category, Pageable pageable);
    
    Page<Training> findByLevel(String level, Pageable pageable);
    
    @Query("SELECT t FROM Training t WHERE :courseId MEMBER OF t.courseIds")
    List<Training> findByCourseId(Long courseId);
    
    @Query("SELECT COUNT(t) FROM Training t WHERE t.category = :category")
    long countByCategory(String category);
    
    List<Training> findByCreatedBy(Long createdBy);
}
