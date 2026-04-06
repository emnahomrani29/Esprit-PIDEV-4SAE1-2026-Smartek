package com.smartek.skillevidenceservice.service;

import com.smartek.skillevidenceservice.entity.Notification;
import com.smartek.skillevidenceservice.entity.NotificationType;
import com.smartek.skillevidenceservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(Long learnerId, Integer evidenceId, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .learnerId(learnerId)
                .evidenceId(evidenceId)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(Long learnerId) {
        return notificationRepository.findByLearnerIdAndIsReadFalseOrderByCreatedAtDesc(learnerId);
    }

    public List<Notification> getAllNotifications(Long learnerId) {
        return notificationRepository.findByLearnerIdOrderByCreatedAtDesc(learnerId);
    }

    @Transactional
    public void markAsRead(Integer notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(Long learnerId) {
        List<Notification> notifications = notificationRepository.findByLearnerIdAndIsReadFalseOrderByCreatedAtDesc(learnerId);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // Nettoyage automatique : supprime les notifications lues de plus de 7 jours
    @Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        notificationRepository.deleteReadNotificationsOlderThan(cutoffDate);
    }

    // Nettoyage agressif : supprime toutes les notifications de plus de 30 jours
    @Scheduled(cron = "0 0 3 * * ?") // Tous les jours à 3h du matin
    @Transactional
    public void cleanupVeryOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteAllNotificationsOlderThan(cutoffDate);
    }
}
