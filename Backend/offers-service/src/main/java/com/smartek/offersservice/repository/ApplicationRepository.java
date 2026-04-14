package com.smartek.offersservice.repository;

import com.smartek.offersservice.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByOfferId(Long offerId);

    List<Application> findByLearnerId(Long learnerId);

    Optional<Application> findByOfferIdAndLearnerId(Long offerId, Long learnerId);

    boolean existsByOfferIdAndLearnerId(Long offerId, Long learnerId);

    long countByOfferId(Long offerId);

    long countByOfferIdAndStatus(Long offerId, String status);

    // Stats globales par offre
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.offerId = :offerId GROUP BY a.status")
    List<Object[]> countByOfferIdGroupByStatus(@Param("offerId") Long offerId);

    // Stats globales par entreprise (via sous-requête)
    @Query("SELECT COUNT(a) FROM Application a WHERE a.offerId IN (SELECT o.id FROM Offer o WHERE o.companyId = :companyId)")
    long countByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = :status AND a.offerId IN (SELECT o.id FROM Offer o WHERE o.companyId = :companyId)")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") String status);
}
