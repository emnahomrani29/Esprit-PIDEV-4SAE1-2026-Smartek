package com.smartek.sponsor.service.impl;

import com.smartek.sponsor.entity.*;
import com.smartek.sponsor.exception.*;
import com.smartek.sponsor.repository.ContractRepository;
import com.smartek.sponsor.repository.SponsorshipRepository;
import com.smartek.sponsor.service.BudgetService;
import com.smartek.sponsor.service.EmailService;
import com.smartek.sponsor.service.SponsorshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SponsorshipServiceImpl implements SponsorshipService {
    private final SponsorshipRepository sponsorshipRepository;
    private final ContractRepository contractRepository;
    private final BudgetService budgetService;
    private final EmailService emailService;

    @Override
    @Transactional
    public Sponsorship createSponsorship(Long contractId, Sponsorship sponsorship) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
        
        // VALIDATION 1: Contract must be ACTIVE
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new InvalidContractStateException(
                String.format("Contract is not active. Current status: %s", contract.getStatus())
            );
        }
        
        // VALIDATION 2: Check available budget
        Double available = budgetService.getAvailableBudget(contractId);
        if (sponsorship.getAmountAllocated() > available) {
            throw new InsufficientBudgetException(
                String.format("Insufficient budget. Available: %.2f€, Requested: %.2f€", 
                    available, sponsorship.getAmountAllocated())
            );
        }
        
        // VALIDATION 3: Check minimum amount for visibility level
        Double minimum = sponsorship.getVisibilityLevel().getMinimumAmount();
        if (sponsorship.getAmountAllocated() < minimum) {
            throw new BusinessException(
                String.format("%s visibility requires minimum %.2f€. Provided: %.2f€", 
                    sponsorship.getVisibilityLevel(), minimum, sponsorship.getAmountAllocated())
            );
        }
        
        // VALIDATION 4: Dates must be within contract period
        if (sponsorship.getStartDate().isBefore(contract.getStartDate()) ||
            sponsorship.getEndDate().isAfter(contract.getEndDate())) {
            throw new InvalidDateRangeException(
                String.format("Sponsorship dates must be within contract period (%s to %s)", 
                    contract.getStartDate(), contract.getEndDate())
            );
        }
        
        // VALIDATION 5: Start date must be before end date
        if (!sponsorship.getStartDate().isBefore(sponsorship.getEndDate())) {
            throw new InvalidDateRangeException("Start date must be before end date");
        }
        
        // VALIDATION 6: Check for overlapping sponsorships
        List<Sponsorship> overlapping = sponsorshipRepository.findOverlappingSponsorships(
            sponsorship.getTargetType(),
            sponsorship.getTargetId(),
            sponsorship.getStartDate(),
            sponsorship.getEndDate()
        );
        
        if (!overlapping.isEmpty()) {
            throw new SponsorshipOverlapException(
                String.format("Target %s #%d already has a sponsorship during this period", 
                    sponsorship.getTargetType(), sponsorship.getTargetId())
            );
        }
        
        // Create with PENDING status
        sponsorship.setId(null);
        sponsorship.setStatus(SponsorshipStatus.PENDING);
        sponsorship.setContract(contract);
        
        Sponsorship saved = sponsorshipRepository.save(sponsorship);
        
        // Send notification to admin
        emailService.notifyAdminNewSponsorship(saved);
        
        log.info("Sponsorship created: {} for contract {}, status: PENDING", saved.getId(), contractId);
        
        return saved;
    }

    @Override
    public List<Sponsorship> getAllSponsorships() {
        return sponsorshipRepository.findAll();
    }

    @Override
    public Sponsorship getSponsorshipById(Long sponsorshipId) {
        return sponsorshipRepository.findById(sponsorshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsorship", "id", sponsorshipId));
    }

    @Override
    @Transactional
    public Sponsorship updateSponsorship(Long sponsorshipId, Long contractId, Sponsorship sponsorship) {
        Sponsorship existing = getSponsorshipById(sponsorshipId);
        
        // Can only update PENDING or REJECTED sponsorships
        if (existing.getStatus() != SponsorshipStatus.PENDING && 
            existing.getStatus() != SponsorshipStatus.REJECTED) {
            throw new BusinessException(
                String.format("Cannot update sponsorship with status: %s", existing.getStatus())
            );
        }
        
        existing.setSponsorshipType(sponsorship.getSponsorshipType());
        existing.setAmountAllocated(sponsorship.getAmountAllocated());
        existing.setStartDate(sponsorship.getStartDate());
        existing.setEndDate(sponsorship.getEndDate());
        existing.setVisibilityLevel(sponsorship.getVisibilityLevel());
        existing.setTargetType(sponsorship.getTargetType());
        existing.setTargetId(sponsorship.getTargetId());
        
        // Reset to PENDING if was REJECTED
        if (existing.getStatus() == SponsorshipStatus.REJECTED) {
            existing.setStatus(SponsorshipStatus.PENDING);
            existing.setRejectionReason(null);
            existing.setApprovedBy(null);
            existing.setApprovedAt(null);
        }

        if (contractId != null) {
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
            existing.setContract(contract);
        }

        return sponsorshipRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteSponsorship(Long sponsorshipId) {
        Sponsorship existing = getSponsorshipById(sponsorshipId);
        
        // Can only delete PENDING, REJECTED, or CANCELLED sponsorships
        if (existing.getStatus() == SponsorshipStatus.APPROVED || 
            existing.getStatus() == SponsorshipStatus.COMPLETED) {
            throw new BusinessException(
                String.format("Cannot delete sponsorship with status: %s", existing.getStatus())
            );
        }
        
        sponsorshipRepository.delete(existing);
        log.info("Sponsorship {} deleted", sponsorshipId);
    }
}

