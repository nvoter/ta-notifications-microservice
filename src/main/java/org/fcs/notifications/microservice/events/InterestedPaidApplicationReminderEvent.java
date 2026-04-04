package org.fcs.notifications.microservice.events;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record InterestedPaidApplicationReminderEvent(
        UUID eventId,
        List<UUID> studentIds,
        OffsetDateTime occurredAt
) {
}
