package org.fcs.notifications.microservice.dtos.users;

import java.util.UUID;

public record StudentReminderRecipientDto(
        UUID id,
        String email,
        String lastName,
        String firstName,
        String middleName
) {
    public String displayName() {
        return java.util.stream.Stream.of(firstName, middleName)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.joining(" "));
    }

    public String fullName() {
        return java.util.stream.Stream.of(lastName, firstName, middleName)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
