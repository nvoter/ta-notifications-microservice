package org.fcs.notifications.microservice.controllers;

import lombok.RequiredArgsConstructor;
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
public class NotificationsController {
    private final NotificationsService notificationsService;

    @GetMapping
    public GetNotificationsResponse getNotifications(
            @RequestHeader UUID userId,
            @RequestParam(required = false) boolean unreadOnly
    ) {
        List<NotificationDto> notifications = notificationsService.getNotifications(userId, unreadOnly);
        return new GetNotificationsResponse(notifications.size(), notifications);
    }

    @PatchMapping
    public MarkNotificationsAsReadResponse markAllAsRead(
            @RequestHeader UUID userId
    ) {
        return new MarkNotificationsAsReadResponse(notificationsService.markAllNotificationsAsRead(userId));
    }

    @PatchMapping("/{notificationId}")
    public NotificationDto markNotificationAsRead(
            @RequestHeader UUID userId,
            @PathVariable UUID notificationId
    ) {
        return notificationsService.markNotificationAsRead(userId, notificationId);
    }
}
