package com.smartek.examservice.repository;

import com.smartek.examservice.entity.ExerciseAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExerciseAnswerRepository extends JpaRepository<ExerciseAnswer, Long> {
    List<ExerciseAnswer> findByExamResultId(Long examResultId);
    List<ExerciseAnswer> findByExerciseId(Long exerciseId);
    List<ExerciseAnswer> findByIsCorrected(Boolean isCorrected);
}
