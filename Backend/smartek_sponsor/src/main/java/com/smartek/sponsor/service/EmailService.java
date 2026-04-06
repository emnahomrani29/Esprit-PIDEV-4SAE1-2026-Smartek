package com.smartek.sponsor.service;

import com.smartek.sponsor.entity.Contract;
import com.smartek.sponsor.entity.Sponsorship;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${email.from:noreply@smartek.com}")
    private String fromEmail;
    
    @Value("${email.admin:admin@smartek.com}")
    private String adminEmail;
    
    /**
     * Notify admin about new sponsorship pending approval (NOTIFICATION ONLY - NO EMAIL)
     */
    public void notifyAdminNewSponsorship(Sponsorship sponsorship) {
        // Admin gets only system notification, not email
        log.info("🔔 ADMIN NOTIFICATION: New sponsorship pending approval from {} (ID: {})", 
                sponsorship.getContract().getSponsor().getName(), 
                sponsorship.getId());

        // Log details for admin dashboard notification system
        logNewSponsorshipFallback(sponsorship);
    }

    
    /**
     * Notify sponsor that sponsorship was approved
     */
    /**
     * Notify sponsor that sponsorship was approved
     */
    public void notifySponsorApproved(Sponsorship sponsorship) {
        CompletableFuture.runAsync(() -> {
            try {
                String sponsorEmail = sponsorship.getContract().getSponsor().getEmail();

                // Validate required data
                if (sponsorEmail == null || sponsorEmail.trim().isEmpty()) {
                    log.error("Cannot send approval email: sponsor email is null or empty");
                    logSponsorApprovedFallback(sponsorship);
                    return;
                }

                Context context = new Context();
                context.setVariable("sponsorName", sponsorship.getContract().getSponsor().getName());
                context.setVariable("sponsorshipType", sponsorship.getSponsorshipType());
                context.setVariable("targetType", sponsorship.getTargetType());
                context.setVariable("targetId", sponsorship.getTargetId());
                context.setVariable("amount", sponsorship.getAmountAllocated());
                context.setVariable("visibilityLevel", sponsorship.getVisibilityLevel());
                context.setVariable("startDate", sponsorship.getStartDate());
                context.setVariable("endDate", sponsorship.getEndDate());
                context.setVariable("sponsorshipId", sponsorship.getId());

                log.info("Processing email template for sponsorship approval...");
                // Skip template processing for now - send plain text
                String plainTextContent = String.format("""
                    Great news! Your sponsorship request has been approved.
                    
                    Details:
                    - Type: %s
                    - Target: %s #%d
                    - Amount: €%.2f
                    - Visibility: %s
                    - Period: %s to %s
                    
                    Your sponsorship is now active!
                    
                    Thank you for choosing Smartek!
                    """, 
                    sponsorship.getSponsorshipType(),
                    sponsorship.getTargetType(),
                    sponsorship.getTargetId(),
                    sponsorship.getAmountAllocated(),
                    sponsorship.getVisibilityLevel(),
                    sponsorship.getStartDate(),
                    sponsorship.getEndDate()
                );
                log.info("Email content prepared successfully");

                log.info("Sending email to: {}", sponsorEmail);
                // Send plain text email directly
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(sponsorEmail);
                helper.setSubject("Your Sponsorship Has Been Approved! 🎉");
                helper.setText(plainTextContent, false); // false = plain text
                
                mailSender.send(message);

                log.info("📧 Email sent successfully to sponsor: {} for approved sponsorship {}", sponsorEmail, sponsorship.getId());

            } catch (Exception e) {
                log.error("Failed to send approval email to sponsor: {}", e.getMessage(), e);
                // Fallback to console logging
                logSponsorApprovedFallback(sponsorship);
            }
        });
    }

    
    /**
     * Notify sponsor that sponsorship was rejected
     */
    public void notifySponsorRejected(Sponsorship sponsorship, String reason) {
        CompletableFuture.runAsync(() -> {
            try {
                String sponsorEmail = sponsorship.getContract().getSponsor().getEmail();
                
                Context context = new Context();
                context.setVariable("sponsorName", sponsorship.getContract().getSponsor().getName());
                context.setVariable("sponsorshipType", sponsorship.getSponsorshipType());
                context.setVariable("targetType", sponsorship.getTargetType());
                context.setVariable("targetId", sponsorship.getTargetId());
                context.setVariable("amount", sponsorship.getAmountAllocated());
                context.setVariable("reason", reason);
                context.setVariable("sponsorshipId", sponsorship.getId());
                
                String htmlContent = templateEngine.process("emails/sponsorship-rejected", context);
                
                sendHtmlEmail(
                    sponsorEmail,
                    "Sponsorship Request Update - Action Required",
                    htmlContent
                );
                
                log.info("📧 Email sent to sponsor: {} for rejected sponsorship {}", sponsorEmail, sponsorship.getId());
                
            } catch (Exception e) {
                log.error("Failed to send rejection email to sponsor", e);
                // Fallback to console logging
                logSponsorRejectedFallback(sponsorship, reason);
            }
        });
    }
    
    /**
     * Notify sponsor about contract expiring soon
     */
    public void notifyContractExpiring(Contract contract, int daysRemaining) {
        CompletableFuture.runAsync(() -> {
            try {
                String sponsorEmail = contract.getSponsor().getEmail();
                
                Context context = new Context();
                context.setVariable("sponsorName", contract.getSponsor().getName());
                context.setVariable("contractNumber", contract.getContractNumber());
                context.setVariable("endDate", contract.getEndDate());
                context.setVariable("daysRemaining", daysRemaining);
                context.setVariable("contractId", contract.getId());
                
                String htmlContent = templateEngine.process("emails/contract-expiring", context);
                
                sendHtmlEmail(
                    sponsorEmail,
                    "Contract Expiring Soon - " + daysRemaining + " Days Remaining",
                    htmlContent
                );
                
                log.info("📧 Email sent to sponsor: {} for expiring contract {}", sponsorEmail, contract.getContractNumber());
                
            } catch (Exception e) {
                log.error("Failed to send contract expiring email", e);
                // Fallback to console logging
                logContractExpiringFallback(contract, daysRemaining);
            }
        });
    }
    
    /**
     * Notify sponsor about budget threshold reached
     */
    public void notifyBudgetThreshold(Contract contract, int percentage) {
        CompletableFuture.runAsync(() -> {
            try {
                String sponsorEmail = contract.getSponsor().getEmail();
                
                Context context = new Context();
                context.setVariable("sponsorName", contract.getSponsor().getName());
                context.setVariable("contractNumber", contract.getContractNumber());
                context.setVariable("percentage", percentage);
                context.setVariable("contractId", contract.getId());
                
                String htmlContent = templateEngine.process("emails/budget-threshold", context);
                
                sendHtmlEmail(
                    sponsorEmail,
                    "Budget Alert - " + percentage + "% Usage Reached",
                    htmlContent
                );
                
                log.info("📧 Email sent to sponsor: {} for budget threshold {}", sponsorEmail, percentage);
                
            } catch (Exception e) {
                log.error("Failed to send budget threshold email", e);
                // Fallback to console logging
                logBudgetThresholdFallback(contract, percentage);
            }
        });
    }
    
    /**
     * Notify sponsor that sponsorship was cancelled
     */
    public void notifySponsorCancelled(Sponsorship sponsorship) {
        CompletableFuture.runAsync(() -> {
            try {
                String sponsorEmail = sponsorship.getContract().getSponsor().getEmail();
                
                Context context = new Context();
                context.setVariable("sponsorName", sponsorship.getContract().getSponsor().getName());
                context.setVariable("sponsorshipType", sponsorship.getSponsorshipType());
                context.setVariable("amount", sponsorship.getAmountAllocated());
                context.setVariable("sponsorshipId", sponsorship.getId());
                
                String htmlContent = templateEngine.process("emails/sponsorship-cancelled", context);
                
                sendHtmlEmail(
                    sponsorEmail,
                    "Sponsorship Cancelled - Budget Returned",
                    htmlContent
                );
                
                log.info("📧 Email sent to sponsor: {} for cancelled sponsorship {}", sponsorEmail, sponsorship.getId());
                
            } catch (Exception e) {
                log.error("Failed to send cancellation email", e);
                // Fallback to console logging
                logSponsorCancelledFallback(sponsorship);
            }
        });
    }
    
    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
    
    // Fallback methods for console logging when email fails
    private void logNewSponsorshipFallback(Sponsorship sponsorship) {
        String sponsorEmail = sponsorship.getContract().getSponsor().getEmail();
        String sponsorName = sponsorship.getContract().getSponsor().getName();
        
        String message = String.format("""
            ═══════════════════════════════════════════════════
            NEW SPONSORSHIP PENDING APPROVAL
            ═══════════════════════════════════════════════════
            Sponsor: %s
            Email: %s
            Type: %s
            Target: %s #%d
            Amount: %.2f€
            Visibility: %s
            Period: %s to %s
            
            Review at: http://localhost:4200/admin/sponsorships/%d/review
            ═══════════════════════════════════════════════════
            """,
            sponsorName,
            sponsorEmail,
            sponsorship.getSponsorshipType(),
            sponsorship.getTargetType(),
            sponsorship.getTargetId(),
            sponsorship.getAmountAllocated(),
            sponsorship.getVisibilityLevel(),
            sponsorship.getStartDate(),
            sponsorship.getEndDate(),
            sponsorship.getId()
        );
        
        log.info("📧 EMAIL FALLBACK TO ADMIN: {}", adminEmail);
        log.info(message);
    }
    
    private void logSponsorApprovedFallback(Sponsorship sponsorship) {
        String sponsorEmail = sponsorship.getContract().getSponsor().getEmail();
        
        String message = String.format("""
            ═══════════════════════════════════════════════════
            YOUR SPONSORSHIP HAS BEEN APPROVED! ✅
            ═══════════════════════════════════════════════════
            Type: %s
            Target: %s #%d
            Amount: %.2f€
            Visibility: %s
            Period: %s to %s
            
            Your sponsorship is now active!
            View at: http://localhost:4200/dashboard/sponsorships/%d
            ═══════════════════════════════════════════════════
            """,
            sponsorship.getSponsorshipType(),
            sponsorship.getTargetType(),
            sponsorship.getTargetId(),
            sponsorship.getAmountAllocated(),
            sponsorship.getVisibilityLevel(),
            sponsorship.getStartDate(),
            sponsorship.getEndDate(),
            sponsorship.getId()
        );
        
        log.info("📧 EMAIL FALLBACK TO SPONSOR: {}", sponsorEmail);
        log.info(message);
    }
    
    private void logSponsorRejectedFallback(Sponsorship sponsorship, String reason) {
        String sponsorEmail = sponsorship.getContract().getSponsor().getEmail();
        
        String message = String.format("""
            ═══════════════════════════════════════════════════
            YOUR SPONSORSHIP HAS BEEN REJECTED ❌
            ═══════════════════════════════════════════════════
            Reason: %s
            
            Type: %s
            Target: %s #%d
            Amount: %.2f€
            
            You can modify your sponsorship and resubmit.
            ═══════════════════════════════════════════════════
            """,
            reason,
            sponsorship.getSponsorshipType(),
            sponsorship.getTargetType(),
            sponsorship.getTargetId(),
            sponsorship.getAmountAllocated()
        );
        
        log.info("📧 EMAIL FALLBACK TO SPONSOR: {}", sponsorEmail);
        log.info(message);
    }
    
    private void logContractExpiringFallback(Contract contract, int daysRemaining) {
        String sponsorEmail = contract.getSponsor().getEmail();
        
        String message = String.format("""
            ═══════════════════════════════════════════════════
            CONTRACT EXPIRING SOON ⚠️
            ═══════════════════════════════════════════════════
            Contract: %s
            Expires: %s (%d days remaining)
            Remaining Budget: Check your dashboard
            
            Contact us to renew your contract!
            Special offer: 10%% discount for renewal
            ═══════════════════════════════════════════════════
            """,
            contract.getContractNumber(),
            contract.getEndDate(),
            daysRemaining
        );
        
        log.info("📧 EMAIL FALLBACK TO SPONSOR: {}", sponsorEmail);
        log.info(message);
    }
    
    private void logBudgetThresholdFallback(Contract contract, int percentage) {
        String sponsorEmail = contract.getSponsor().getEmail();
        
        String message = String.format("""
            ═══════════════════════════════════════════════════
            BUDGET THRESHOLD ALERT ⚠️
            ═══════════════════════════════════════════════════
            Contract: %s
            Budget Usage: %d%%
            
            You have used %d%% of your contract budget.
            View details: http://localhost:4200/dashboard
            ═══════════════════════════════════════════════════
            """,
            contract.getContractNumber(),
            percentage,
            percentage
        );
        
        log.info("📧 EMAIL FALLBACK TO SPONSOR: {}", sponsorEmail);
        log.info(message);
    }
    
    private void logSponsorCancelledFallback(Sponsorship sponsorship) {
        String sponsorEmail = sponsorship.getContract().getSponsor().getEmail();
        
        String message = String.format("""
            ═══════════════════════════════════════════════════
            SPONSORSHIP CANCELLED
            ═══════════════════════════════════════════════════
            Type: %s
            Amount: %.2f€
            
            Your sponsorship has been cancelled.
            Budget has been returned to your contract.
            ═══════════════════════════════════════════════════
            """,
            sponsorship.getSponsorshipType(),
            sponsorship.getAmountAllocated()
        );
        
        log.info("📧 EMAIL FALLBACK TO SPONSOR: {}", sponsorEmail);
        log.info(message);
    }
}