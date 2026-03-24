package org.fcs.notifications.microservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Ответ со списком уведомлений")
public record GetNotificationsResponse(
        @Schema(description = "Количество уведомлений в ответе", example = "5")
        int count,
        @Schema(description = "Список уведомлений")
        List<NotificationDto> notifications
) {
}
