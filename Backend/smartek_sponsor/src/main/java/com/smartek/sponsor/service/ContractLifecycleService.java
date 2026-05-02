package com.smartek.sponsor.service;

import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.ContractStatus;
import com.smartek.sponsor.entity.Sponsorship;
import com.smartek.sponsor.entity.SponsorshipStatus;
import com.smartek.sponsor.repository.ContractRepository;
import com.smartek.sponsor.repository.SponsorshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractLifecycleService {
    
    private final ContractRepository contractRepository;
    private final SponsorshipRepository sponsorshipRepository;
    private final EmailService emailService;
    private final BudgetService budgetService;
    
    /**
     * Run daily at 2:00 AM to update contract statuses
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void updateContractStatuses() {
        log.info("Starting contract status update job");
        
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(30);
        
        // Find contracts expiring soon (30 days or less)
        List<Contract> expiringSoon = contractRepository.findContractsExpiringSoon(today, warningDate);
        for (Contract contract : expiringSoon) {
            contract.setStatus(ContractStatus.EXPIRING_SOON);
            contractRepository.save(contract);
            
            long daysRemaining = ChronoUnit.DAYS.between(today, contract.getEndDate());
            emailService.notifyContractExpiring(contract, (int) daysRemaining);
            
            log.info("Contract {} status changed to EXPIRING_SOON ({} days remaining)", 
                contract.getContractNumber(), daysRemaining);
        }
        
        // Find expired contracts
        List<Contract> expired = contractRepository.findExpiredContracts(today);
        for (Contract contract : expired) {
            contract.setStatus(ContractStatus.EXPIRED);
            contractRepository.save(contract);
            
            log.info("Contract {} status changed to EXPIRED", contract.getContractNumber());
        }
        
        log.info("Contract status update job completed. Expiring soon: {}, Expired: {}", 
            expiringSoon.size(), expired.size());
    }
    
    /**
     * Run daily at 3:00 AM to complete finished sponsorships
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void completeFinishedSponsorships() {
        log.info("Starting sponsorship completion job");
        
        LocalDate today = LocalDate.now();
        
        List<Sponsorship> toComplete = sponsorshipRepository.findSponsorshipsToComplete(today);
        
        for (Sponsorship sponsorship : toComplete) {
            sponsorship.setStatus(SponsorshipStatus.COMPLETED);
            sponsorshipRepository.save(sponsorship);
            
            log.info("Sponsorship {} status changed to COMPLETED", sponsorship.getId());
        }
        
        log.info("Sponsorship completion job completed. Completed: {}", toComplete.size());
    }
    
    /**
     * Run daily at 4:00 AM to check budget thresholds
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void checkBudgetThresholds() {
        log.info("Starting budget threshold check job");
        
        List<Contract> activeContracts = contractRepository.findByStatus(ContractStatus.ACTIVE);
        
        int warningCount = 0;
        int criticalCount = 0;
        
        for (Contract contract : activeContracts) {
            Double usagePercentage = budgetService.getBudgetSummary(contract.getId()).getUsagePercentage();
            
            // Check 90% threshold (CRITICAL)
            if (usagePercentage >= 90 && usagePercentage < 95) {
                emailService.notifyBudgetThreshold(contract, 90);
                criticalCount++;
                log.warn("Contract {} reached 90% budget usage", contract.getContractNumber());
            }
            // Check 80% threshold (WARNING)
            else if (usagePercentage >= 80 && usagePercentage < 85) {
                emailService.notifyBudgetThreshold(contract, 80);
                warningCount++;
                log.warn("Contract {} reached 80% budget usage", contract.getContractNumber());
            }
        }
        
        log.info("Budget threshold check completed. Warnings: {}, Critical: {}", warningCount, criticalCount);
    }
    
    /**
     * Manual method to activate a contract
     */
    @Transactional
    public Contract activateContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT contracts can be activated");
        }
        
        contract.setStatus(ContractStatus.ACTIVE);
        Contract saved = contractRepository.save(contract);
        
        log.info("Contract {} activated", contract.getContractNumber());
        
        return saved;
    }
}
