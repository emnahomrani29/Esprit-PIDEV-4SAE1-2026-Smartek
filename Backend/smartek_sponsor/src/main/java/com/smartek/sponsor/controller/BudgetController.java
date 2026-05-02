package com.smartek.sponsor.controller;

import com.smartek.sponsor.dto.BudgetSummaryDTO;
import com.smartek.sponsor.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/budget")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BudgetController {
    
    private final BudgetService budgetService;
    
    /**
     * Get available budget for a contract
     */
    @GetMapping("/available/{contractId}")
    public ResponseEntity<Double> getAvailableBudget(@PathVariable Long contractId) {
        Double available = budgetService.getAvailableBudget(contractId);
        return ResponseEntity.ok(available);
    }
    
    /**
     * Get complete budget summary for a contract
     */
    @GetMapping("/summary/contract/{contractId}")
    public ResponseEntity<BudgetSummaryDTO> getContractBudgetSummary(@PathVariable Long contractId) {
        BudgetSummaryDTO summary = budgetService.getBudgetSummary(contractId);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get aggregated budget summary for all sponsor's contracts
     */
    @GetMapping("/summary/sponsor/{sponsorId}")
    public ResponseEntity<BudgetSummaryDTO> getSponsorBudgetSummary(@PathVariable Long sponsorId) {
        BudgetSummaryDTO summary = budgetService.getSponsorBudgetSummary(sponsorId);
        return ResponseEntity.ok(summary);
    }
}
