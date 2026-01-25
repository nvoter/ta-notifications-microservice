package org.fcs.notifications.microservice.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeConfirmationCodeGeneratedEvent(
        UUID eventId,
        UUID employeeId,
        String email,
        String lastName,
        String firstName,
        String middleName,
        String confirmationCode,
        OffsetDateTime generatedAt
) {
}
