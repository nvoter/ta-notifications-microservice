package org.fcs.notifications.microservice.exceptions;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(UUID userId, UUID notificationId) {
        super("Could not find notification with id " + notificationId + " for user with id " + userId);
    }
}
