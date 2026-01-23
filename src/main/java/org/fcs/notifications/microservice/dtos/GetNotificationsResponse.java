package org.fcs.notifications.microservice.dtos;

import java.util.List;

public record GetNotificationsResponse(
        int count,
        List<NotificationDto> notifications
) {
}
