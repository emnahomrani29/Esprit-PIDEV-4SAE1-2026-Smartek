package com.smartek.examservice.repository;

import com.smartek.examservice.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByExamIdOrderByExerciseNumberAsc(Long examId);
}
