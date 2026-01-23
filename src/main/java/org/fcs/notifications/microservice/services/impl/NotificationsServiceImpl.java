package org.fcs.notifications.microservice.services.impl;

import org.fcs.notifications.microservice.dtos.NotificationDto;
import org.fcs.notifications.microservice.services.NotificationsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationsServiceImpl implements NotificationsService {
    @Override
    public List<NotificationDto> getNotifications(UUID userId, boolean unreadOnly) {
        return List.of();
    }

    @Override
    public NotificationDto markNotificationAsRead(UUID userId, UUID notificationId) {
        return null;
    }

    @Override
    public int markAllNotificationsAsRead(UUID userId) {
        return 0;
    }
}
