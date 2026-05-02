package com.smartek.sponsor.service;

import com.smartek.sponsor.dto.StatusSummaryDTO;
import com.smartek.sponsor.dto.ExpiringContractDTO;
import com.smartek.sponsor.entity.Sponsorship;
import com.smartek.sponsor.entity.SponsorshipStatus;
import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.ContractStatus;
import com.smartek.sponsor.exception.BusinessException;
import com.smartek.sponsor.exception.ResourceNotFoundException;
import com.smartek.sponsor.repository.SponsorshipRepository;
import com.smartek.sponsor.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {
    
    private final SponsorshipRepository sponsorshipRepository;
    private final ContractRepository contractRepository;
    private final EmailService emailService;
    
    /**
     * Admin approves a pending sponsorship
     */
    @Transactional
    public Sponsorship approveSponsorship(Long sponsorshipId, Long adminId) {
        Sponsorship sponsorship = sponsorshipRepository.findById(sponsorshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsorship", "id", sponsorshipId));
        
        // Validate status
        if (sponsorship.getStatus() != SponsorshipStatus.PENDING) {
            throw new BusinessException("Only PENDING sponsorships can be approved. Current status: " + sponsorship.getStatus());
        }
        
        // Update status
        sponsorship.setStatus(SponsorshipStatus.APPROVED);
        sponsorship.setApprovedBy(adminId);
        sponsorship.setApprovedAt(LocalDateTime.now());
        
        Sponsorship saved = sponsorshipRepository.save(sponsorship);
        
        // Send notification
        emailService.notifySponsorApproved(saved);
        
        log.info("Sponsorship {} approved by admin {}", sponsorshipId, adminId);
        
        return saved;
    }
    
    /**
     * Admin rejects a pending sponsorship with reason
     */
    @Transactional
    public Sponsorship rejectSponsorship(Long sponsorshipId, String reason, Long adminId) {
        Sponsorship sponsorship = sponsorshipRepository.findById(sponsorshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsorship", "id", sponsorshipId));
        
        // Validate status
        if (sponsorship.getStatus() != SponsorshipStatus.PENDING) {
            throw new BusinessException("Only PENDING sponsorships can be rejected. Current status: " + sponsorship.getStatus());
        }
        
        // Update status
        sponsorship.setStatus(SponsorshipStatus.REJECTED);
        sponsorship.setRejectionReason(reason);
        sponsorship.setApprovedBy(adminId);
        sponsorship.setApprovedAt(LocalDateTime.now());
        
        Sponsorship saved = sponsorshipRepository.save(sponsorship);
        
        // Send notification
        emailService.notifySponsorRejected(saved, reason);
        
        log.info("Sponsorship {} rejected by admin {}: {}", sponsorshipId, adminId, reason);
        
        return saved;
    }
    
    /**
     * Sponsor cancels a pending sponsorship
     */
    @Transactional
    public Sponsorship cancelSponsorship(Long sponsorshipId) {
        Sponsorship sponsorship = sponsorshipRepository.findById(sponsorshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsorship", "id", sponsorshipId));
        
        // Can only cancel PENDING sponsorships
        if (sponsorship.getStatus() != SponsorshipStatus.PENDING) {
            throw new BusinessException("Only PENDING sponsorships can be cancelled. Current status: " + sponsorship.getStatus());
        }
        
        sponsorship.setStatus(SponsorshipStatus.CANCELLED);
        
        Sponsorship saved = sponsorshipRepository.save(sponsorship);
        
        // Send notification
        emailService.notifySponsorCancelled(saved);
        
        log.info("Sponsorship {} cancelled by sponsor", sponsorshipId);
        
        return saved;
    }
    
    /**
     * Get all pending sponsorships (for admin)
     */
    public List<Sponsorship> getPendingSponsorships() {
        try {
            log.info("Fetching pending sponsorships...");
            List<Sponsorship> pendingSponsorships = sponsorshipRepository.findByStatus(SponsorshipStatus.PENDING);
            log.info("Found {} pending sponsorships", pendingSponsorships.size());
            
            for (Sponsorship sponsorship : pendingSponsorships) {
                log.info("Sponsorship ID: {}, Status: {}, Amount: {}, Contract ID: {}", 
                    sponsorship.getId(), 
                    sponsorship.getStatus(),
                    sponsorship.getAmountAllocated(),
                    sponsorship.getContract() != null ? sponsorship.getContract().getId() : "null");
                
                if (sponsorship.getContract() != null && sponsorship.getContract().getSponsor() != null) {
                    log.info("  - Sponsor: {}", sponsorship.getContract().getSponsor().getName());
                }
            }
            
            return pendingSponsorships;
        } catch (Exception e) {
            log.error("Error fetching pending sponsorships: ", e);
            // Return empty list instead of throwing exception to avoid 500 errors
            return List.of();
        }
    }
    
    /**
     * Get count of pending sponsorships (for badge)
     */
    public Long getPendingCount() {
        return sponsorshipRepository.countByStatus(SponsorshipStatus.PENDING);
    }
    
    /**
     * Get count of urgent pending sponsorships (pending for more than 24 hours)
     */
    public Long getUrgentPendingCount() {
        LocalDateTime urgentThreshold = LocalDateTime.now().minusHours(24);
        return sponsorshipRepository.findByStatus(SponsorshipStatus.PENDING)
                .stream()
                .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isBefore(urgentThreshold))
                .count();
    }
    
    /**
     * Get status summary for a sponsor
     */
    public StatusSummaryDTO getStatusSummary(Long sponsorId) {
        Long pending = sponsorshipRepository.countByContractSponsorIdAndStatus(sponsorId, SponsorshipStatus.PENDING);
        Long approved = sponsorshipRepository.countByContractSponsorIdAndStatus(sponsorId, SponsorshipStatus.APPROVED);
        Long rejected = sponsorshipRepository.countByContractSponsorIdAndStatus(sponsorId, SponsorshipStatus.REJECTED);
        Long completed = sponsorshipRepository.countByContractSponsorIdAndStatus(sponsorId, SponsorshipStatus.COMPLETED);
        Long cancelled = sponsorshipRepository.countByContractSponsorIdAndStatus(sponsorId, SponsorshipStatus.CANCELLED);
        
        return StatusSummaryDTO.builder()
                .sponsorId(sponsorId)
                .pendingCount(pending)
                .approvedCount(approved)
                .rejectedCount(rejected)
                .completedCount(completed)
                .cancelledCount(cancelled)
                .build();
    }
    
    /**
     * Get recent pending sponsorships (last 24 hours)
     */
    public List<Sponsorship> getRecentPending() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return sponsorshipRepository.findRecentPending(SponsorshipStatus.PENDING, since);
    }
    
    /**
     * Get contracts expiring within 30 days
     */
    public List<ExpiringContractDTO> getExpiringContracts() {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30);
        
        return contractRepository.findContractsExpiringSoon(today, futureDate)
                .stream()
                .map(contract -> {
                    int daysUntilExpiry = (int) ChronoUnit.DAYS.between(today, contract.getEndDate());
                    return new ExpiringContractDTO(
                            contract.getId(),
                            contract.getContractNumber(),
                            contract.getEndDate(),
                            contract.getSponsor().getName(),
                            daysUntilExpiry
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get count of expiring contracts for a specific sponsor
     */
    public Long getSponsorExpiringContractsCount(Long sponsorId) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30);
        
        return contractRepository.findContractsExpiringSoon(today, futureDate)
                .stream()
                .filter(contract -> contract.getSponsor().getId().equals(sponsorId))
                .count();
    }
}
