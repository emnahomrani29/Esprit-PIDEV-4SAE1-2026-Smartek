package com.smartek.sponsor.service;

import com.smartek.sponsor.dto.BudgetSummaryDTO;
import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.SponsorshipStatus;
import com.smartek.sponsor.exception.ResourceNotFoundException;
import com.smartek.sponsor.repository.ContractRepository;
import com.smartek.sponsor.repository.SponsorshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {
    
    private final ContractRepository contractRepository;
    private final SponsorshipRepository sponsorshipRepository;
    
    /**
     * Calculate available budget for a contract
     * Formula: Total - Spent - Reserved
     */
    public Double getAvailableBudget(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
        
        Double spent = calculateSpent(contractId);
        Double reserved = calculateReserved(contractId);
        
        return contract.getAmount() - spent - reserved;
    }
    
    /**
     * Calculate spent budget (APPROVED + COMPLETED sponsorships)
     */
    public Double calculateSpent(Long contractId) {
        return sponsorshipRepository
                .findByContractIdAndStatusIn(contractId, 
                    List.of(SponsorshipStatus.APPROVED, SponsorshipStatus.COMPLETED))
                .stream()
                .mapToDouble(s -> s.getAmountAllocated() != null ? s.getAmountAllocated() : 0.0)
                .sum();
    }
    
    /**
     * Calculate reserved budget (PENDING sponsorships)
     */
    public Double calculateReserved(Long contractId) {
        return sponsorshipRepository
                .findByContractIdAndStatus(contractId, SponsorshipStatus.PENDING)
                .stream()
                .mapToDouble(s -> s.getAmountAllocated() != null ? s.getAmountAllocated() : 0.0)
                .sum();
    }
    
    /**
     * Get complete budget summary for a contract
     */
    public BudgetSummaryDTO getBudgetSummary(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
        
        Double spent = calculateSpent(contractId);
        Double reserved = calculateReserved(contractId);
        Double available = contract.getAmount() - spent - reserved;
        Double usagePercentage = ((spent + reserved) / contract.getAmount()) * 100;
        
        return BudgetSummaryDTO.builder()
                .contractId(contract.getId())
                .contractNumber(contract.getContractNumber())
                .totalBudget(contract.getAmount())
                .spent(spent)
                .reserved(reserved)
                .available(available)
                .usagePercentage(usagePercentage)
                .warningLevel(getWarningLevel(usagePercentage))
                .build();
    }
    
    /**
     * Get aggregated budget summary for all sponsor's contracts
     */
    public BudgetSummaryDTO getSponsorBudgetSummary(Long sponsorId) {
        List<Contract> contracts = contractRepository.findBySponsorId(sponsorId);
        
        if (contracts.isEmpty()) {
            return BudgetSummaryDTO.builder()
                    .totalBudget(0.0)
                    .spent(0.0)
                    .reserved(0.0)
                    .available(0.0)
                    .usagePercentage(0.0)
                    .warningLevel("NORMAL")
                    .build();
        }
        
        Double totalBudget = 0.0;
        Double totalSpent = 0.0;
        Double totalReserved = 0.0;
        
        for (Contract contract : contracts) {
            totalBudget += contract.getAmount();
            totalSpent += calculateSpent(contract.getId());
            totalReserved += calculateReserved(contract.getId());
        }
        
        Double available = totalBudget - totalSpent - totalReserved;
        Double usagePercentage = totalBudget > 0 ? ((totalSpent + totalReserved) / totalBudget) * 100 : 0.0;
        
        return BudgetSummaryDTO.builder()
                .totalBudget(totalBudget)
                .spent(totalSpent)
                .reserved(totalReserved)
                .available(available)
                .usagePercentage(usagePercentage)
                .warningLevel(getWarningLevel(usagePercentage))
                .build();
    }
    
    /**
     * Determine warning level based on usage percentage
     */
    private String getWarningLevel(Double percentage) {
        if (percentage >= 90) {
            return "CRITICAL";
        } else if (percentage >= 80) {
            return "WARNING";
        }
        return "NORMAL";
    }
    
    /**
     * Check if budget threshold has been reached
     */
    public boolean isBudgetThresholdReached(Long contractId, int thresholdPercentage) {
        BudgetSummaryDTO summary = getBudgetSummary(contractId);
        return summary.getUsagePercentage() >= thresholdPercentage;
    }
}
