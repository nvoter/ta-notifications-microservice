package org.fcs.notifications.microservice.events;

import org.fcs.notifications.microservice.dtos.reminders.InterestedEmployeeReminderDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record InterestedPaidApplicationReminderEvent(
        UUID eventId,
        List<UUID> studentIds,
        List<InterestedEmployeeReminderDto> employeeReminders,
        OffsetDateTime occurredAt
) {
}
