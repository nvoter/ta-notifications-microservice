package org.fcs.notifications.microservice.services;

import org.fcs.notifications.microservice.dtos.NotificationDto;

import java.util.List;
import java.util.UUID;

public interface NotificationsService {
    List<NotificationDto> getNotifications(UUID userId, boolean unreadOnly);
    NotificationDto markNotificationAsRead(UUID userId, UUID notificationId);
    int markAllNotificationsAsRead(UUID userId);
}
