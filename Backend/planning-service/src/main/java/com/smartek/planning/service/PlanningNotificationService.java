package com.smartek.planning.service;

import com.smartek.planning.model.Planning;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service de notification SMS pour les learners lors de la publication d'un planning.
 * Notifie TOUS les learners de la plateforme qu'un nouveau planning est disponible.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanningNotificationService {

    private final TwilioSmsService twilioSmsService;
    private final RestTemplate restTemplate;

    @Value("${auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Notifie tous les learners de la plateforme qu'un planning vient d'être publié.
     */
    public void notifyLearnersForPublishedPlanning(Planning planning) {
        List<Map<String, Object>> learners = fetchAllLearners();

        if (learners.isEmpty()) {
            log.warn("Aucun learner trouvé sur la plateforme, aucun SMS envoyé.");
            return;
        }

        log.info("Envoi de notifications SMS à {} learner(s) pour le planning '{}'",
                learners.size(), planning.getTitle());

        String smsMessage = buildSmsMessage(planning);

        for (Map<String, Object> learner : learners) {
            Object phoneObj = learner.get("phone");
            Object nameObj  = learner.get("firstName");
            if (phoneObj != null && !phoneObj.toString().isBlank()) {
                twilioSmsService.sendSms(phoneObj.toString().trim(), smsMessage);
            } else {
                log.warn("Learner '{}' n'a pas de numéro de téléphone, SMS ignoré.", nameObj);
            }
        }
    }

    /**
     * Notifie tous les learners pour une publication hebdomadaire.
     * Un seul SMS par learner même si plusieurs sessions sont publiées.
     */
    public void notifyLearnersForPublishedWeek(List<Planning> plannings) {
        if (plannings.isEmpty()) return;

        List<Map<String, Object>> learners = fetchAllLearners();
        if (learners.isEmpty()) {
            log.warn("Aucun learner trouvé, aucun SMS envoyé.");
            return;
        }

        log.info("Publication hebdomadaire : envoi SMS à {} learner(s) pour {} session(s)",
                learners.size(), plannings.size());

        String smsMessage = buildWeeklySmsMessage(plannings);

        for (Map<String, Object> learner : learners) {
            Object phoneObj = learner.get("phone");
            Object nameObj  = learner.get("firstName");
            if (phoneObj != null && !phoneObj.toString().isBlank()) {
                twilioSmsService.sendSms(phoneObj.toString().trim(), smsMessage);
            } else {
                log.warn("Learner '{}' n'a pas de numéro de téléphone, SMS ignoré.", nameObj);
            }
        }
    }

    /**
     * Récupère tous les learners depuis l'auth-service.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchAllLearners() {
        try {
            String url = authServiceUrl + "/api/auth/users/learners";
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des learners depuis l'auth-service: {}", e.getMessage());
        }
        return List.of();
    }

    /**
     * Construit le SMS pour une session individuelle publiée.
     */
    private String buildSmsMessage(Planning planning) {
        String date      = planning.getDate().format(DATE_FORMATTER);
        String startTime = planning.getStartTime().format(TIME_FORMATTER);
        String endTime   = planning.getEndTime().format(TIME_FORMATTER);
        String location  = (planning.getLocation() != null && !planning.getLocation().isBlank())
                ? planning.getLocation() : "À définir";

        return String.format(
                "📅 Smartek - Nouveau planning publié !\n" +
                "Session : %s\n" +
                "Date : %s de %s à %s\n" +
                "Lieu : %s\n" +
                "Connectez-vous à la plateforme pour vous inscrire.",
                planning.getTitle(), date, startTime, endTime, location
        );
    }

    /**
     * Construit un SMS récapitulatif pour une publication hebdomadaire.
     */
    private String buildWeeklySmsMessage(List<Planning> plannings) {
        Planning first = plannings.get(0);
        String weekStart = first.getDate().format(DATE_FORMATTER);

        return String.format(
                "📅 Smartek - Planning de la semaine publié !\n" +
                "%d session(s) disponible(s) à partir du %s.\n" +
                "Connectez-vous à la plateforme pour consulter le programme et vous inscrire.",
                plannings.size(), weekStart
        );
    }
}
