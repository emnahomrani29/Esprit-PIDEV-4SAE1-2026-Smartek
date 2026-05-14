package com.smartek.sponsor.repository;

import com.smartek.sponsor.entity.Sponsorship;
import com.smartek.sponsor.entity.SponsorshipStatus;
import com.smartek.sponsor.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SponsorshipRepository extends JpaRepository<Sponsorship, Long> {
    
    // Find by sponsor
    List<Sponsorship> findByContractSponsorId(Long sponsorId);
    
    // Find by contract
    List<Sponsorship> findByContractId(Long contractId);
    
    // Find by status
    List<Sponsorship> findByStatus(SponsorshipStatus status);
    
    // Find by status with eager loading of contract and sponsor
    @Query("SELECT s FROM Sponsorship s JOIN FETCH s.contract c JOIN FETCH c.sponsor WHERE s.status = :status")
    List<Sponsorship> findByStatusWithContractAndSponsor(@Param("status") SponsorshipStatus status);
    
    // Find by contract and status
    List<Sponsorship> findByContractIdAndStatus(Long contractId, SponsorshipStatus status);
    
    // Find by contract and multiple statuses
    List<Sponsorship> findByContractIdAndStatusIn(Long contractId, List<SponsorshipStatus> statuses);
    
    // Count by status
    Long countByStatus(SponsorshipStatus status);
    
    // Count by sponsor and status
    Long countByContractSponsorIdAndStatus(Long sponsorId, SponsorshipStatus status);
    
    // Find recent pending (last 24 hours)
    @Query("SELECT s FROM Sponsorship s WHERE s.status = :status AND s.createdAt > :since ORDER BY s.createdAt DESC")
    List<Sponsorship> findRecentPending(@Param("status") SponsorshipStatus status, @Param("since") LocalDateTime since);
    
    // Check for overlapping sponsorships for same target
    @Query("SELECT s FROM Sponsorship s WHERE s.targetType = :targetType AND s.targetId = :targetId " +
           "AND s.status IN ('PENDING', 'APPROVED') " +
           "AND ((s.startDate <= :endDate AND s.endDate >= :startDate))")
    List<Sponsorship> findOverlappingSponsorships(
        @Param("targetType") TargetType targetType,
        @Param("targetId") Long targetId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Find sponsorships ending before a date (for completion job)
    @Query("SELECT s FROM Sponsorship s WHERE s.status = 'APPROVED' AND s.endDate < :date")
    List<Sponsorship> findSponsorshipsToComplete(@Param("date") LocalDate date);
    
    // Get approval times for calculating average response time
    @Query("SELECT s.createdAt, s.updatedAt FROM Sponsorship s WHERE s.status IN ('APPROVED', 'REJECTED') AND s.createdAt IS NOT NULL AND s.updatedAt IS NOT NULL")
    List<Object[]> findApprovalTimes();

    // Native queries to handle enum parsing issues
    @Query(value = "SELECT COUNT(*) FROM sponsorships WHERE status = 'PENDING'", nativeQuery = true)
    Long countPendingNative();

    @Query(value = "SELECT id, status, amount_allocated, contract_id, visibility_level FROM sponsorships ORDER BY id", nativeQuery = true)
    List<Object[]> findRawSponsorshipData();
}

