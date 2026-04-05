package org.fcs.notifications.microservice.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeConfirmationCodeGeneratedEvent(
        UUID eventId,
        UUID employeeId,
        String email,
        String backupEmail,
        String fullName,
        String confirmationCode,
        OffsetDateTime generatedAt
) {
}
