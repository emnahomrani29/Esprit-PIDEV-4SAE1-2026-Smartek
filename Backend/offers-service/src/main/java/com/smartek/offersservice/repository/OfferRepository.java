package com.smartek.offersservice.repository;

import com.smartek.offersservice.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByCompanyId(Long companyId);

    List<Offer> findByStatus(String status);

    Page<Offer> findByStatus(String status, Pageable pageable);

    List<Offer> findByCompanyIdAndStatus(Long companyId, String status);

    Page<Offer> findAll(Pageable pageable);

    // Recherche full-text sur titre, description, companyName, location
    @Query("SELECT o FROM Offer o WHERE o.status = 'ACTIVE' AND (" +
           "LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Offer> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Recherche avancée avec filtres multiples
    @Query("SELECT o FROM Offer o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:contractType IS NULL OR o.contractType = :contractType) AND " +
           "(:location IS NULL OR LOWER(o.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:domain IS NULL OR o.domain = :domain) AND " +
           "(:experienceLevel IS NULL OR o.experienceLevel = :experienceLevel) AND " +
           "(:remote IS NULL OR o.remote = :remote) AND " +
           "(:salaryMin IS NULL OR o.salaryMin >= :salaryMin) AND " +
           "(:salaryMax IS NULL OR o.salaryMax <= :salaryMax) AND " +
           "(:keyword IS NULL OR LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Offer> searchWithFilters(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("contractType") String contractType,
            @Param("location") String location,
            @Param("domain") String domain,
            @Param("experienceLevel") String experienceLevel,
            @Param("remote") Boolean remote,
            @Param("salaryMin") Integer salaryMin,
            @Param("salaryMax") Integer salaryMax,
            Pageable pageable);

    // Statistiques par entreprise
    @Query("SELECT COUNT(o) FROM Offer o WHERE o.companyId = :companyId AND o.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") String status);

    // Offres expirées à fermer automatiquement
    @Query("SELECT o FROM Offer o WHERE o.status = 'ACTIVE' AND o.expiresAt IS NOT NULL AND o.expiresAt < :now")
    List<Offer> findExpiredOffers(@Param("now") LocalDateTime now);

    // Incrémenter le compteur de vues
    @Modifying
    @Query("UPDATE Offer o SET o.viewCount = o.viewCount + 1 WHERE o.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Offres les plus vues
    @Query("SELECT o FROM Offer o WHERE o.status = 'ACTIVE' ORDER BY o.viewCount DESC")
    List<Offer> findTopViewedOffers(Pageable pageable);
}
