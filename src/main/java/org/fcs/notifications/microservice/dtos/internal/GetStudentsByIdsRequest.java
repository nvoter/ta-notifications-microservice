package org.fcs.notifications.microservice.dtos.internal;

import java.util.List;
import java.util.UUID;

public record GetStudentsByIdsRequest(
        List<UUID> ids
) {
}
