package com.smartek.skillevidenceservice.service;

import com.smartek.skillevidenceservice.entity.Notification;
import com.smartek.skillevidenceservice.entity.NotificationType;
import com.smartek.skillevidenceservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService service;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .learnerId(10L)
                .evidenceId(1)
                .message("Your evidence has been approved")
                .type(NotificationType.APPROVAL)
                .isRead(false)
                .build();
    }

    @Test
    void createNotification_savesNotification() {
        service.createNotification(10L, 1, "Approved", NotificationType.APPROVAL);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUnreadNotifications_returnsUnread() {
        when(notificationRepository.findByLearnerIdAndIsReadFalseOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(notification));

        List<Notification> result = service.getUnreadNotifications(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsRead()).isFalse();
    }

    @Test
    void getAllNotifications_returnsAll() {
        Notification read = Notification.builder().learnerId(10L).isRead(true).build();
        when(notificationRepository.findByLearnerIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(notification, read));

        List<Notification> result = service.getAllNotifications(10L);

        assertThat(result).hasSize(2);
    }

    @Test
    void markAsRead_updatesNotification() {
        notification.setIsRead(false);
        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));

        service.markAsRead(1);

        assertThat(notification.getIsRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_notFound_doesNothing() {
        when(notificationRepository.findById(99)).thenReturn(Optional.empty());

        service.markAsRead(99);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsRead_marksAllUnread() {
        Notification n2 = Notification.builder().learnerId(10L).isRead(false).build();
        when(notificationRepository.findByLearnerIdAndIsReadFalseOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(notification, n2));

        service.markAllAsRead(10L);

        assertThat(notification.getIsRead()).isTrue();
        assertThat(n2.getIsRead()).isTrue();
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void deleteNotification_callsRepository() {
        service.deleteNotification(1);

        verify(notificationRepository).deleteById(1);
    }
}
