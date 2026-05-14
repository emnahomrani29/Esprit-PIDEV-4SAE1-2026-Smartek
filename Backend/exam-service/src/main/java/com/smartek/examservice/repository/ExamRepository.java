package com.smartek.examservice.repository;

import com.smartek.examservice.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCourseId(Long courseId);
    
    List<Exam> findByTrainingId(Long trainingId);
    
    Optional<Exam> findFirstByCourseId(Long courseId);
    
    Optional<Exam> findFirstByCourseIdAndExamType(Long courseId, String examType);
    
    Optional<Exam> findFirstByTrainingIdAndExamType(Long trainingId, String examType);
    
    List<Exam> findByIsActive(Boolean isActive);
    
    List<Exam> findByCreatedBy(Long createdBy);
    
    @EntityGraph(attributePaths = {"questions"})
    @Query("SELECT e FROM Exam e WHERE e.id = :id")
    Optional<Exam> findByIdWithQuestions(Long id);
    
    @EntityGraph(attributePaths = {"questions"})
    @Query("SELECT e FROM Exam e WHERE e.isActive = true")
    List<Exam> findAllActiveWithQuestions();
    
    Page<Exam> findAll(Pageable pageable);
    
    Page<Exam> findByCourseId(Long courseId, Pageable pageable);
    
    @Query("SELECT COUNT(e) FROM Exam e WHERE e.courseId = :courseId")
    long countByCourseId(Long courseId);
}
