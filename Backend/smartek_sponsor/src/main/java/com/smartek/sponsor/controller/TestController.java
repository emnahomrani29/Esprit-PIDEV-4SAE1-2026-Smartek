package com.smartek.sponsor.controller;

import com.smartek.sponsor.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.internet.MimeMessage;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final JavaMailSender mailSender;
    
    @PostMapping("/email/{email}")
    public String testEmail(@PathVariable String email) {
        try {
            log.info("Testing email to: {}", email);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("workfirst557@gmail.com");
            helper.setTo(email);
            helper.setSubject("Test Email from Smartek - Email System Working! 🎉");
            helper.setText("""
                Hello!
                
                This is a test email from your Smartek sponsorship system.
                
                If you receive this email, it means:
                ✅ Gmail SMTP is working correctly
                ✅ Your email configuration is correct
                ✅ The email system is ready for sponsorship notifications
                
                Next time you approve a sponsorship, the sponsor will receive a professional email notification.
                
                Best regards,
                Smartek Team
                """, false);
            
            mailSender.send(message);
            
            log.info("✅ Test email sent successfully to: {}", email);
            return "✅ Test email sent successfully to: " + email;
            
        } catch (Exception e) {
            log.error("❌ Failed to send test email: {}", e.getMessage(), e);
            return "❌ Failed to send test email: " + e.getMessage();
        }
    }
}