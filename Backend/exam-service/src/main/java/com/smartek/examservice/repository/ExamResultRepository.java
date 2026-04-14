package com.smartek.examservice.repository;

import com.smartek.examservice.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByExamId(Long examId);
    List<ExamResult> findByUserId(Long userId);
    List<ExamResult> findByExamIdAndUserId(Long examId, Long userId);
    
    long countDistinctExamIdByUserId(Long userId);
    
    @Query("SELECT er FROM ExamResult er JOIN er.exam e WHERE e.createdBy = :trainerId ORDER BY er.submittedAt DESC")
    List<ExamResult> findAllByTrainerId(@Param("trainerId") Long trainerId);
}

