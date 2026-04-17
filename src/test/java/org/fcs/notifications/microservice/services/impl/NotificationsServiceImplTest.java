package org.fcs.notifications.microservice.services.impl;

import org.fcs.notifications.microservice.dtos.NotificationDto;
import org.fcs.notifications.microservice.entities.Notification;
import org.fcs.notifications.microservice.exceptions.NotificationNotFoundException;
import org.fcs.notifications.microservice.models.EntityType;
import org.fcs.notifications.microservice.models.NotificationType;
import org.fcs.notifications.microservice.repositories.NotificationsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationsServiceImplTest {
    @Mock
    private NotificationsRepository notificationsRepository;

    @InjectMocks
    private NotificationsServiceImpl service;

    @Test
    void getNotifications_whenUnreadOnly_thenReturnUnreadNotifications() {
        UUID userId = UUID.randomUUID();
        Notification notification = notification(userId);
        when(notificationsRepository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(notification));

        List<NotificationDto> result = service.getNotifications(userId, true);

        assertEquals(1, result.size());
        assertEquals(notification.getId(), result.getFirst().id());
    }

    @Test
    void getNotifications_whenAll_thenReturnAllNotifications() {
        UUID userId = UUID.randomUUID();
        when(notificationsRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(notification(userId)));

        List<NotificationDto> result = service.getNotifications(userId, false);

        assertEquals(1, result.size());
    }

    @Test
    void markNotificationAsRead_whenMissing_thenThrow() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        when(notificationsRepository.findByIdAndRecipientUserId(notificationId, userId)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> service.markNotificationAsRead(userId, notificationId));
    }

    @Test
    void markNotificationAsRead_whenFound_thenMarkAsRead() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Notification notification = notification(userId);
        notification.setId(notificationId);
        when(notificationsRepository.findByIdAndRecipientUserId(notificationId, userId)).thenReturn(Optional.of(notification));

        NotificationDto result = service.markNotificationAsRead(userId, notificationId);

        assertEquals(true, notification.isRead());
        assertEquals(notificationId, result.id());
    }

    @Test
    void markAllNotificationsAsRead_whenOk_thenReturnUpdatedCount() {
        UUID userId = UUID.randomUUID();
        when(notificationsRepository.markAllRead(userId)).thenReturn(3);

        assertEquals(3, service.markAllNotificationsAsRead(userId));
        verify(notificationsRepository).markAllRead(userId);
    }

    private static Notification notification(UUID userId) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setEventId(UUID.randomUUID());
        notification.setRecipientUserId(userId);
        notification.setEntityType(EntityType.APPLICATION_DISCIPLINE);
        notification.setEntityId(UUID.randomUUID());
        notification.setNotificationType(NotificationType.APPLICATION_STATUS_UPDATED);
        notification.setTitle("title");
        notification.setMessage("message");
        notification.setCreatedAt(OffsetDateTime.now());
        notification.setRead(false);
        return notification;
    }
}
