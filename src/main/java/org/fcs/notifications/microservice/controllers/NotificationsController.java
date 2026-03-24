package org.fcs.notifications.microservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.fcs.notifications.microservice.dtos.ApiErrorResponse;
import org.fcs.notifications.microservice.dtos.GetNotificationsResponse;
import org.fcs.notifications.microservice.dtos.MarkNotificationsAsReadResponse;
import org.fcs.notifications.microservice.dtos.NotificationDto;
import org.fcs.notifications.microservice.services.NotificationsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Управление уведомлениями пользователя")
public class NotificationsController {
    private final NotificationsService notificationsService;

    @GetMapping
    @Operation(
            summary = "Получить уведомления пользователя",
            description = "Возвращает уведомления текущего пользователя. При необходимости можно запросить только непрочитанные"
    )
    @SecurityRequirement(name = "userIdHeader")
    @ApiResponse(responseCode = "200", description = "Уведомления успешно получены")
    public GetNotificationsResponse getNotifications(
            @Parameter(description = "Идентификатор пользователя", required = true)
            @RequestHeader UUID userId,
            @Parameter(description = "Если `true`, вернуть только непрочитанные уведомления", example = "true")
            @RequestParam(required = false) boolean unreadOnly
    ) {
        List<NotificationDto> notifications = notificationsService.getNotifications(userId, unreadOnly);
        return new GetNotificationsResponse(notifications.size(), notifications);
    }

    @PatchMapping
    @Operation(
            summary = "Отметить все уведомления как прочитанные",
            description = "Меняет статус всех уведомлений пользователя на прочитанные"
    )
    @SecurityRequirement(name = "userIdHeader")
    @ApiResponse(responseCode = "200", description = "Уведомления успешно отмечены как прочитанные")
    public MarkNotificationsAsReadResponse markAllAsRead(
            @Parameter(description = "Идентификатор пользователя", required = true)
            @RequestHeader UUID userId
    ) {
        return new MarkNotificationsAsReadResponse(notificationsService.markAllNotificationsAsRead(userId));
    }

    @PatchMapping("/{notificationId}")
    @Operation(
            summary = "Отметить одно уведомление как прочитанное",
            description = "Меняет статус конкретного уведомления пользователя на прочитанное"
    )
    @SecurityRequirement(name = "userIdHeader")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Уведомление успешно обновлено"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Уведомление не найдено",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public NotificationDto markNotificationAsRead(
            @Parameter(description = "Идентификатор пользователя", required = true)
            @RequestHeader UUID userId,
            @Parameter(description = "Идентификатор уведомления")
            @PathVariable UUID notificationId
    ) {
        return notificationsService.markNotificationAsRead(userId, notificationId);
    }
}
