package com.smartek.skillevidenceservice.controller;

import com.smartek.skillevidenceservice.entity.Notification;
import com.smartek.skillevidenceservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/learner/{learnerId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long learnerId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(learnerId));
    }

    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<Notification>> getAllNotifications(@PathVariable Long learnerId) {
        return ResponseEntity.ok(notificationService.getAllNotifications(learnerId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/learner/{learnerId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long learnerId) {
        notificationService.markAllAsRead(learnerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
