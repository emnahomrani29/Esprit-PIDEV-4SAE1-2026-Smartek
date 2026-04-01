package com.smartek.certificationbadgeservice.repository;

import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EarnedCertificationRepository extends JpaRepository<EarnedCertification, Long> {
    
    /**
     * Find all certifications earned by a specific learner
     * @param learnerId the learner ID
     * @return list of earned certifications
     */
    List<EarnedCertification> findByLearnerId(Long learnerId);
    
    /**
     * Find all certifications earned by a specific learner with pagination
     * @param learnerId the learner ID
     * @param pageable pagination information
     * @return page of earned certifications
     */
    Page<EarnedCertification> findByLearnerId(Long learnerId, Pageable pageable);
    
    /**
     * Count the number of times a certification template has been awarded
     * @param certificationTemplateId the certification template ID
     * @return count of awards
     */
    long countByCertificationTemplateId(Long certificationTemplateId);
    
    /**
     * Count active certifications for a learner (not expired)
     * @param learnerId the learner ID
     * @param currentDate the current date to compare against expiry dates
     * @return count of active certifications
     */
    long countByLearnerIdAndExpiryDateAfter(Long learnerId, LocalDate currentDate);
    
    boolean existsByCertificationTemplate_IdAndLearnerId(Long certificationTemplateId, Long learnerId);
    
    /**
     * Check if a learner already has a specific certification.
     * Used to prevent duplicate awards.
     */
    boolean existsByLearnerIdAndCertificationTemplateId(Long learnerId, Long certificationTemplateId);
}
