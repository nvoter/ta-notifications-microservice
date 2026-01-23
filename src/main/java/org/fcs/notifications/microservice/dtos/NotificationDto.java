package org.fcs.notifications.microservice.dtos;

import org.fcs.notifications.microservice.models.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        UUID eventId,
        NotificationType notificationType,
        String title,
        String message,
        LocalDateTime createdAt,
        boolean isRead
) {
}
