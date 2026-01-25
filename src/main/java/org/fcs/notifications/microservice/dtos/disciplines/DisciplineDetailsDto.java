package org.fcs.notifications.microservice.dtos.disciplines;

import java.util.List;
import java.util.UUID;

public record DisciplineDetailsDto(
        UUID id,
        UUID educationalProgramId,
        String name,
        String course,
        List<Integer> modules,
        int groupsCount,
        int maxAssistantsCount,
        String assignment
) {
}
