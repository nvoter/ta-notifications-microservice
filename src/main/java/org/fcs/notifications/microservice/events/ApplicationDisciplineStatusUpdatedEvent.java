package org.fcs.notifications.microservice.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationDisciplineStatusUpdatedEvent(
        UUID eventId,
        UUID applicationId,
        UUID applicationDisciplineId,
        UUID disciplineId,
        String previousStatus,
        String newStatus,
        UUID employeeId,
        UUID previousEmployeeId,
        UUID studentId,
        OffsetDateTime occurredAt
) {
}
