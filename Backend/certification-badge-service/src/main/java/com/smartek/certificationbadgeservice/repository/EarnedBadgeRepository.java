package com.smartek.certificationbadgeservice.repository;

import com.smartek.certificationbadgeservice.entity.EarnedBadge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EarnedBadgeRepository extends JpaRepository<EarnedBadge, Long> {
    
    /**
     * Check if a badge has already been awarded to a learner
     * @param badgeTemplateId the badge template ID
     * @param learnerId the learner ID
     * @return true if the badge has been awarded, false otherwise
     */
    boolean existsByBadgeTemplateIdAndLearnerId(Long badgeTemplateId, Long learnerId);
    
    /**
     * Check if a learner already has a specific badge.
     * Used to prevent duplicate awards.
     */
    boolean existsByLearnerIdAndBadgeTemplateId(Long learnerId, Long badgeTemplateId);
    
    /**
     * Find all badges earned by a specific learner
     * @param learnerId the learner ID
     * @return list of earned badges
     */
    List<EarnedBadge> findByLearnerId(Long learnerId);
    
    /**
     * Find all badges earned by a specific learner with pagination
     * @param learnerId the learner ID
     * @param pageable pagination information
     * @return page of earned badges
     */
    Page<EarnedBadge> findByLearnerId(Long learnerId, Pageable pageable);
    
    /**
     * Count the number of times a badge template has been awarded
     * @param badgeTemplateId the badge template ID
     * @return count of awards
     */
    long countByBadgeTemplateId(Long badgeTemplateId);
}
