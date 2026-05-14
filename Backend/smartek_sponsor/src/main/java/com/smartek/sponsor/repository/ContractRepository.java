package com.smartek.sponsor.repository;

import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    
    // Find by sponsor
    List<Contract> findBySponsorId(Long sponsorId);
    
    // Find by status
    List<Contract> findByStatus(ContractStatus status);
    
    // Count by status
    Long countByStatus(ContractStatus status);
    
    // Find by sponsor and status
    List<Contract> findBySponsorIdAndStatus(Long sponsorId, ContractStatus status);
    
    // Find contracts expiring soon (end date between now and specified date)
    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.endDate BETWEEN :today AND :futureDate")
    List<Contract> findContractsExpiringSoon(@Param("today") LocalDate today, @Param("futureDate") LocalDate futureDate);
    
    // Find expired contracts (end date before today)
    @Query("SELECT c FROM Contract c WHERE c.status IN ('ACTIVE', 'EXPIRING_SOON') AND c.endDate < :today")
    List<Contract> findExpiredContracts(@Param("today") LocalDate today);
    
    // Check if contract number exists
    boolean existsByContractNumber(String contractNumber);
}
