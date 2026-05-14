package com.smartek.examservice.repository;

import com.smartek.examservice.entity.ExamEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamEnrollmentRepository extends JpaRepository<ExamEnrollment, Long> {

    @Query("SELECT ee FROM ExamEnrollment ee JOIN FETCH ee.exam WHERE ee.userId = :userId")
    List<ExamEnrollment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT ee FROM ExamEnrollment ee JOIN FETCH ee.exam WHERE ee.userId = :userId AND ee.isUnlocked = :isUnlocked")
    List<ExamEnrollment> findByUserIdAndIsUnlocked(@Param("userId") Long userId, @Param("isUnlocked") Boolean isUnlocked);

    @Query("SELECT ee FROM ExamEnrollment ee JOIN FETCH ee.exam WHERE ee.trainingId = :trainingId")
    List<ExamEnrollment> findByTrainingId(@Param("trainingId") Long trainingId);

    @Query("SELECT ee FROM ExamEnrollment ee JOIN FETCH ee.exam WHERE ee.courseId = :courseId")
    List<ExamEnrollment> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT ee FROM ExamEnrollment ee JOIN FETCH ee.exam WHERE ee.userId = :userId AND ee.exam.id = :examId")
    Optional<ExamEnrollment> findByUserIdAndExamId(@Param("userId") Long userId, @Param("examId") Long examId);

    @Query("SELECT ee FROM ExamEnrollment ee JOIN FETCH ee.exam WHERE ee.userId = :userId AND ee.courseId = :courseId")
    Optional<ExamEnrollment> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    boolean existsByUserIdAndExamId(Long userId, Long examId);

    void deleteByTrainingId(Long trainingId);
    void deleteByCourseId(Long courseId);
    void deleteByExamId(Long examId);
}
