package org.fcs.notifications.microservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат отметки уведомлений как прочитанных")
public record MarkNotificationsAsReadResponse(
        @Schema(description = "Количество обновленных уведомлений", example = "5")
        int updatedCount
) {
}
