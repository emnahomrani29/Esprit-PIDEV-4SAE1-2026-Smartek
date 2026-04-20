package com.smartek.certificationbadgeservice.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Generates a PDF certificate using iText 7, then signs it via PdfSigningService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CertificatePdfService {

    private final PdfSigningService pdfSigningService;

    @Value("${app.base-url:http://localhost:4200}")
    private String baseUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    /**
     * Build and sign a PDF for the given earned certification.
     *
     * @param cert        the earned certification entity
     * @param learnerName the learner's display name (passed in from the caller)
     * @return signed PDF bytes
     */
    public byte[] generateSignedPdf(EarnedCertification cert, String learnerName) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(40, 60, 40, 60);

            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            String title = cert.getCertificationTemplate().getTitle();
            String issueDateStr = cert.getIssueDate().format(DATE_FMT);
            String verifyUrl = baseUrl + "/verify/" + cert.getVerificationId();

            doc.add(new Paragraph("SMARTEK")
                    .setFont(bold).setFontSize(36)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("Learning Excellence Platform")
                    .setFont(regular).setFontSize(14)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("\nCertificate of Achievement\n")
                    .setFont(bold).setFontSize(22)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("This is to certify that")
                    .setFont(regular).setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph(learnerName)
                    .setFont(bold).setFontSize(30)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("has successfully completed")
                    .setFont(regular).setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph(title)
                    .setFont(bold).setFontSize(24)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            if (cert.getCertificationTemplate().getDescription() != null) {
                doc.add(new Paragraph(cert.getCertificationTemplate().getDescription())
                        .setFont(regular).setFontSize(12)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            doc.add(new Paragraph("\nIssued: " + issueDateStr
                    + (cert.getExpiryDate() != null ? "   |   Expires: " + cert.getExpiryDate().format(DATE_FMT) : ""))
                    .setFont(regular).setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("\nVerify at: " + verifyUrl)
                    .setFont(regular).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();

            byte[] unsigned = baos.toByteArray();
            return pdfSigningService.sign(unsigned, learnerName, title, issueDateStr);

        } catch (Exception e) {
            log.error("Failed to generate PDF for certification {}: {}", cert.getId(), e.getMessage());
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
