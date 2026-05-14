package com.smartek.planning.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi de SMS via Twilio.
 * Utilisé pour notifier les learners lors de la publication d'un planning.
 */
@Service
@Slf4j
public class TwilioSmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @Value("${twilio.enabled:true}")
    private boolean enabled;

    @PostConstruct
    public void init() {
        if (enabled) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialisé avec le numéro: {}", fromPhoneNumber);
        } else {
            log.warn("Twilio est désactivé (twilio.enabled=false)");
        }
    }

    /**
     * Envoie un SMS à un numéro de téléphone donné.
     *
     * @param toPhoneNumber numéro destinataire (format E.164, ex: +33612345678)
     * @param messageBody   contenu du message
     */
    public void sendSms(String toPhoneNumber, String messageBody) {
        if (!enabled) {
            log.info("[Twilio désactivé] SMS simulé vers {} : {}", toPhoneNumber, messageBody);
            return;
        }

        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            log.warn("Numéro de téléphone manquant, SMS non envoyé.");
            return;
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            log.info("SMS envoyé avec succès à {} - SID: {}", toPhoneNumber, message.getSid());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du SMS à {}: {}", toPhoneNumber, e.getMessage());
        }
    }
}
