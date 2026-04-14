package com.smartek.certificationbadgeservice.service;

import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Asynchronous email service for sending certificate notifications.
 * Attaches the signed PDF and includes a QR verification link.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${certificate.verification.base-url:http://localhost:8083}")
    private String verificationBaseUrl;

    @Value("${spring.mail.from:noreply@smartek.com}")
    private String fromAddress;

    /**
     * Send a certificate award email asynchronously with the signed PDF attached.
     */
    @Async
    public void sendCertificateEmail(String toEmail,
                                     String learnerName,
                                     EarnedCertification cert,
                                     byte[] pdfBytes) {
        try {
            String verificationUrl = verificationBaseUrl
                    + "/api/certifications-badges/verify/" + cert.getVerificationId();
            String certTitle = cert.getCertificationTemplate().getTitle();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Congratulations! Your Smartek Certificate: " + certTitle);
            helper.setText(buildHtmlBody(learnerName, certTitle, verificationUrl), true);

            String fileName = "Smartek_Certificate_"
                    + certTitle.replaceAll("\\s+", "_") + ".pdf";
            helper.addAttachment(fileName, new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(message);
            log.info("Certificate email sent to {} for cert id={}", toEmail, cert.getId());

        } catch (Exception e) {
            log.error("Failed to send certificate email to {} for cert id={}", toEmail, cert.getId(), e);
        }
    }

    // -------------------------------------------------------------------------

    private String buildHtmlBody(String learnerName, String certTitle, String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#f4f6f9;margin:0;padding:0;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f9;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1);">
                        <!-- Header -->
                        <tr>
                          <td style="background:#002060;padding:32px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:28px;letter-spacing:2px;">SMARTEK</h1>
                            <p style="color:#d4af37;margin:6px 0 0;font-size:13px;">Learning Excellence Platform</p>
                          </td>
                        </tr>
                        <!-- Body -->
                        <tr>
                          <td style="padding:40px 48px;">
                            <h2 style="color:#002060;margin-top:0;">Congratulations, %s!</h2>
                            <p style="color:#444;line-height:1.6;">
                              We are delighted to inform you that you have successfully earned the
                              <strong>%s</strong> certification on the Smartek Platform.
                            </p>
                            <p style="color:#444;line-height:1.6;">
                              Your signed certificate is attached to this email as a PDF.
                              You can also verify its authenticity at any time using the link below.
                            </p>
                            <!-- CTA Button -->
                            <div style="text-align:center;margin:32px 0;">
                              <a href="%s"
                                 style="background:#002060;color:#ffffff;padding:14px 32px;border-radius:6px;
                                        text-decoration:none;font-weight:bold;font-size:15px;">
                                Verify Certificate
                              </a>
                            </div>
                            <p style="color:#888;font-size:12px;">
                              Or copy this link: <a href="%s" style="color:#002060;">%s</a>
                            </p>
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="background:#f0f0f0;padding:20px 48px;text-align:center;">
                            <p style="color:#aaa;font-size:11px;margin:0;">
                              © 2026 Smartek Platform. All rights reserved.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(learnerName, certTitle, verificationUrl, verificationUrl, verificationUrl);
    }
}
