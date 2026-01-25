package org.fcs.notifications.microservice.dtos.users;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeProfileDto(
        UUID id,
        String email,
        String lastName,
        String firstName,
        String middleName,
        String role,
        boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public String fullName() {
        return java.util.stream.Stream.of(lastName, firstName, middleName)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
