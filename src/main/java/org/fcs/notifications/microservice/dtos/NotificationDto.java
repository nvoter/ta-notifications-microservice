package org.fcs.notifications.microservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fcs.notifications.microservice.entities.Notification;
import org.fcs.notifications.microservice.models.EntityType;
import org.fcs.notifications.microservice.models.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Уведомление пользователя")
public record NotificationDto(
        @Schema(description = "Идентификатор уведомления", example = "550e8400-e29b-41d4-a716-446655440030")
        UUID id,
        @Schema(description = "Тип связанной сущности", example = "APPLICATION_DISCIPLINE")
        EntityType entityType,
        @Schema(description = "Идентификатор связанной сущности", example = "550e8400-e29b-41d4-a716-446655440031")
        UUID entityId,
        @Schema(description = "Тип уведомления", example = "STATUS_UPDATED")
        NotificationType notificationType,
        @Schema(description = "Короткий заголовок уведомления", example = "Статус дисциплины обновлен")
        String title,
        @Schema(description = "Текст уведомления", example = "По выбранной дисциплине изменен статус рассмотрения.")
        String message,
        @Schema(description = "Дата и время создания", example = "2026-03-27T12:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Признак прочитанного уведомления", example = "false")
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
