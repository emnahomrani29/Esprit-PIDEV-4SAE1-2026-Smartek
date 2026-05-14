package com.smartek.certificationbadgeservice.repository;

import com.smartek.certificationbadgeservice.entity.BadgeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeTemplateRepository extends JpaRepository<BadgeTemplate, Long> {
    
    /**
     * Find badge templates for an exam where the minimum score is less than or equal to the achieved score.
     * Used for automatic badge awarding after exam completion.
     */
    List<BadgeTemplate> findByExamIdAndMinimumScoreLessThanEqual(Long examId, Double achievedScore);
}
