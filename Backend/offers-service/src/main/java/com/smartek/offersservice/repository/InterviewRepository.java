package com.smartek.offersservice.repository;

import com.smartek.offersservice.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByOfferId(Long offerId);

    List<Interview> findByLearnerId(Long learnerId);

    List<Interview> findByApplicationId(Long applicationId);

    Optional<Interview> findByApplicationIdAndStatus(Long applicationId, Interview.InterviewStatus status);

    List<Interview> findByCreatedBy(Long createdBy);

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.offerId IN (SELECT o.id FROM Offer o WHERE o.companyId = :companyId)")
    long countByOffer_CompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.status = :status AND i.offerId IN (SELECT o.id FROM Offer o WHERE o.companyId = :companyId)")
    long countByOffer_CompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") Interview.InterviewStatus status);

    @Query("SELECT i FROM Interview i WHERE i.offerId IN (SELECT o.id FROM Offer o WHERE o.companyId = :companyId) ORDER BY i.interviewDate DESC")
    List<Interview> findByOffer_CompanyId(@Param("companyId") Long companyId);
}
