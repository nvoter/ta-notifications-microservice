package org.fcs.notifications.microservice.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StudentConfirmationCodeGeneratedEvent(
        UUID eventId,
        UUID studentId,
        String email,
        String lastName,
        String firstName,
        String middleName,
        String confirmationCode,
        OffsetDateTime generatedAt
) {
}
