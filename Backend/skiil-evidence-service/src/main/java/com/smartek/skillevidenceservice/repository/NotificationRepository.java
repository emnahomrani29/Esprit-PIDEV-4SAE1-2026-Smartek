package com.smartek.skillevidenceservice.repository;

import com.smartek.skillevidenceservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByLearnerIdAndIsReadFalseOrderByCreatedAtDesc(Long learnerId);

    List<Notification> findByLearnerIdOrderByCreatedAtDesc(Long learnerId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :cutoffDate")
    void deleteReadNotificationsOlderThan(LocalDateTime cutoffDate);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteAllNotificationsOlderThan(LocalDateTime cutoffDate);
}
