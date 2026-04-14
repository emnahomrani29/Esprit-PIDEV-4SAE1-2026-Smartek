package com.smartek.learningmicroservice.repository;

import com.smartek.learningmicroservice.entity.LearningPath;
import com.smartek.learningmicroservice.entity.LearningPathStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    List<LearningPath> findByLearnerIdOrderByStartDateDesc(Long learnerId);

    List<LearningPath> findByStatus(LearningPathStatus status);

    List<LearningPath> findByLearnerIdAndStatus(Long learnerId, LearningPathStatus status);

    boolean existsByLearnerIdAndTitle(Long learnerId, String title);

    boolean existsByLearnerIdAndTitleAndPathIdNot(Long learnerId, String title, Long pathId);
}
