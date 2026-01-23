package org.fcs.notifications.microservice.dtos;

import org.fcs.notifications.microservice.entities.Notification;
import org.fcs.notifications.microservice.models.EntityType;
import org.fcs.notifications.microservice.models.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        EntityType entityType,
        UUID entityId,
        NotificationType notificationType,
        String title,
        String message,
        LocalDateTime createdAt,
        boolean isRead
) {
    public static NotificationDto fromEntity(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getEntityType(),
                notification.getEntityId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}
