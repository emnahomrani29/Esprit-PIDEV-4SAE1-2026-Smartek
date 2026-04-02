package com.smartek.certificationbadgeservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.signatures.*;
import com.smartek.certificationbadgeservice.entity.EarnedCertification;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

/**
 * Generates digitally signed PDF certificates using iText 7 + BouncyCastle.
 * Embeds a ZXing QR code pointing to the public verification URL.
 */
@Slf4j
@Service
public class PdfGenerationService {

    @Value("${certificate.keystore.path:smartek-keystore.p12}")
    private String keystorePath;

    @Value("${certificate.keystore.password:smartek123}")
    private String keystorePassword;

    @Value("${certificate.keystore.alias:smartek}")
    private String keystoreAlias;

    @Value("${certificate.verification.base-url:http://localhost:8083}")
    private String verificationBaseUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DeviceRgb NAVY = new DeviceRgb(0, 32, 96);
    private static final DeviceRgb GOLD = new DeviceRgb(212, 175, 55);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Generate a signed PDF certificate and return it as a byte array.
     */
    public byte[] generateCertificatePdf(EarnedCertification cert, String learnerName) {
        try {
            String verificationUrl = verificationBaseUrl
                    + "/api/certifications-badges/verify/" + cert.getVerificationId();

            // 1. Build unsigned PDF in memory
            ByteArrayOutputStream unsignedOut = new ByteArrayOutputStream();
            buildPdf(unsignedOut, cert, learnerName, verificationUrl);

            // 2. Sign the PDF
            return signPdf(unsignedOut.toByteArray());

        } catch (Exception e) {
            log.error("Failed to generate certificate PDF for certId={}", cert.getId(), e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void buildPdf(ByteArrayOutputStream out,
                          EarnedCertification cert,
                          String learnerName,
                          String verificationUrl) throws Exception {

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
        Document document = new Document(pdfDoc);
        document.setMargins(40, 50, 40, 50);

        PdfFont bold    = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        PdfFont italic  = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_OBLIQUE);

        // Header
        document.add(new Paragraph("SMARTEK")
                .setFont(bold).setFontSize(36).setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));

        document.add(new Paragraph("Learning Excellence Platform")
                .setFont(regular).setFontSize(12).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

        document.add(new Paragraph("Certificate of Achievement")
                .setFont(italic).setFontSize(20).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(8));

        document.add(new Paragraph("This is to certify that")
                .setFont(regular).setFontSize(13).setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));

        // Learner name
        document.add(new Paragraph(learnerName)
                .setFont(bold).setFontSize(30).setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));

        document.add(new Paragraph("has successfully completed")
                .setFont(regular).setFontSize(13).setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));

        // Certification title
        document.add(new Paragraph(cert.getCertificationTemplate().getTitle())
                .setFont(bold).setFontSize(22).setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));

        if (cert.getCertificationTemplate().getDescription() != null) {
            document.add(new Paragraph(cert.getCertificationTemplate().getDescription())
                    .setFont(regular).setFontSize(11).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(16));
        }

        // Dates row
        Table dateTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(80))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginBottom(16);

        addDateCell(dateTable,
                "Issue Date",
                cert.getIssueDate() != null ? cert.getIssueDate().format(DATE_FMT) : "-",
                bold, regular);
        addDateCell(dateTable,
                "Certificate ID",
                "SMARTEK-" + cert.getIssueDate().getYear() + "-" + String.format("%06d", cert.getId()),
                bold, regular);
        addDateCell(dateTable,
                "Expiry Date",
                cert.getExpiryDate() != null ? cert.getExpiryDate().format(DATE_FMT) : "No Expiration",
                bold, regular);

        document.add(dateTable);

        // Digital signature notice
        document.add(new Paragraph("Digitally Signed by Smartek Platform")
                .setFont(italic).setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

        // QR Code
        byte[] qrBytes = generateQrCode(verificationUrl, 120);
        Image qrImage = new Image(ImageDataFactory.create(qrBytes))
                .setWidth(80).setHeight(80)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        document.add(qrImage);

        document.add(new Paragraph("Scan to verify this certificate")
                .setFont(regular).setFontSize(9).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
    }

    private void addDateCell(Table table, String label, String value,
                             PdfFont bold, PdfFont regular) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph(label)
                .setFont(bold).setFontSize(9).setFontColor(ColorConstants.GRAY));
        cell.add(new Paragraph(value)
                .setFont(regular).setFontSize(11).setFontColor(new DeviceRgb(30, 30, 30)));
        table.addCell(cell);
    }

    private byte[] generateQrCode(String content, int size) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    private byte[] signPdf(byte[] unsignedPdfBytes) {
        try (InputStream ksStream = new ClassPathResource(keystorePath).getInputStream()) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(ksStream, keystorePassword.toCharArray());

            PrivateKey privateKey = (PrivateKey) ks.getKey(keystoreAlias, keystorePassword.toCharArray());
            Certificate[] chain = ks.getCertificateChain(keystoreAlias);

            ByteArrayOutputStream signedOut = new ByteArrayOutputStream();

            PdfReader reader = new PdfReader(new ByteArrayInputStream(unsignedPdfBytes));
            PdfSigner signer = new PdfSigner(reader, signedOut, new StampingProperties().useAppendMode());

            // Configure appearance
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Issued by Smartek Platform");
            appearance.setLocation("Smartek Platform");

            signer.setFieldName("SmartekSignature");

            IExternalSignature pks = new PrivateKeySignature(
                    privateKey, DigestAlgorithms.SHA256, BouncyCastleProvider.PROVIDER_NAME);
            IExternalDigest digest = new BouncyCastleDigest();

            signer.signDetached(digest, pks, chain, null, null, null, 0,
                    PdfSigner.CryptoStandard.CMS);

            return signedOut.toByteArray();

        } catch (Exception e) {
            log.warn("PDF signing failed, returning unsigned PDF. Reason: {}", e.getMessage());
            return unsignedPdfBytes;
        }
    }
}
