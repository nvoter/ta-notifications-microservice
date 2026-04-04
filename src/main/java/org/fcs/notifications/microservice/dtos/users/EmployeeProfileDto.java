package org.fcs.notifications.microservice.dtos.users;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeProfileDto(
        UUID id,
        String email,
        String fullName,
        String role,
        boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
