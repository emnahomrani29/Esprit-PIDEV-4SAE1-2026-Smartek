package com.smartek.certificationbadgeservice.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

/**
 * Signs PDF bytes using a PKCS#12 keystore.
 * The keystore is loaded once at startup from the configured path.
 * For development, a self-signed keystore is bundled in src/main/resources.
 */
@Service
@Slf4j
public class PdfSigningService {

    @Value("${pdf.signing.keystore-path}")
    private Resource keystorePath;

    @Value("${pdf.signing.keystore-password}")
    private String keystorePassword;

    @Value("${pdf.signing.key-alias}")
    private String keyAlias;

    private PrivateKey privateKey;
    private Certificate[] certificateChain;

    @PostConstruct
    public void init() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = keystorePath.getInputStream()) {
                ks.load(is, keystorePassword.toCharArray());
            }
            privateKey = (PrivateKey) ks.getKey(keyAlias, keystorePassword.toCharArray());
            certificateChain = ks.getCertificateChain(keyAlias);
            log.info("PDF signing keystore loaded successfully for alias '{}'", keyAlias);
        } catch (Exception e) {
            log.warn("PDF signing keystore could not be loaded — PDF signing will be skipped: {}", e.getMessage());
            privateKey = null;
            certificateChain = null;
        }
    }

    /**
     * Sign the given PDF bytes and return the signed PDF bytes.
     * If the keystore is not available, returns the original bytes unchanged.
     *
     * @param pdfBytes      raw PDF bytes to sign
     * @param learnerName   embedded in signature metadata
     * @param certTitle     embedded in signature metadata
     * @param issueDate     embedded in signature metadata
     * @return signed PDF bytes (or original if signing unavailable)
     */
    public byte[] sign(byte[] pdfBytes, String learnerName, String certTitle, String issueDate) {
        if (privateKey == null || certificateChain == null) {
            log.warn("Signing skipped — keystore not loaded");
            return pdfBytes;
        }
        try {
            ByteArrayOutputStream signedOut = new ByteArrayOutputStream();
            PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
            PdfSigner signer = new PdfSigner(reader, signedOut, new StampingProperties().useAppendMode());

            // Signature appearance metadata
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Issued to: " + learnerName + " | " + certTitle + " | " + issueDate);
            appearance.setLocation("SMARTEK Learning Platform");
            signer.setFieldName("SMARTEK_SIGNATURE");

            IExternalDigest digest = new BouncyCastleDigest();
            IExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, BouncyCastleProvider.PROVIDER_NAME);

            signer.signDetached(digest, signature, certificateChain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

            log.info("PDF signed successfully for: {} — {}", learnerName, certTitle);
            return signedOut.toByteArray();
        } catch (Exception e) {
            log.error("PDF signing failed — returning unsigned PDF: {}", e.getMessage());
            return pdfBytes;
        }
    }
}
