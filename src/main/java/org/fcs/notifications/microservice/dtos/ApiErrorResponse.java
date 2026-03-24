package org.fcs.notifications.microservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ошибка")
public record ApiErrorResponse(
        @Schema(description = "HTTP status code", example = "404")
        int code,
        @Schema(description = "Описание ошибки", example = "Notification not found")
        String message
) {
}
