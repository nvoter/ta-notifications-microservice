package org.fcs.notifications.microservice.dtos.reminders;

import java.util.List;
import java.util.UUID;

public record InterestedEmployeeReminderDto(
        UUID employeeId,
        List<UUID> studentIds
) {
}
