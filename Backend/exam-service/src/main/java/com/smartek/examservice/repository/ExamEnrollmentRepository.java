package com.smartek.examservice.repository;

import com.smartek.examservice.entity.ExamEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamEnrollmentRepository extends JpaRepository<ExamEnrollment, Long> {
    List<ExamEnrollment> findByUserId(Long userId);
    List<ExamEnrollment> findByUserIdAndIsUnlocked(Long userId, Boolean isUnlocked);
    List<ExamEnrollment> findByTrainingId(Long trainingId);
    List<ExamEnrollment> findByCourseId(Long courseId);
    Optional<ExamEnrollment> findByUserIdAndExamId(Long userId, Long examId);
    Optional<ExamEnrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    boolean existsByUserIdAndExamId(Long userId, Long examId);
    void deleteByTrainingId(Long trainingId);
    void deleteByCourseId(Long courseId);
}
