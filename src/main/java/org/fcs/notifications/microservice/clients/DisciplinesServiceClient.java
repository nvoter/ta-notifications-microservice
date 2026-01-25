package org.fcs.notifications.microservice.clients;

import lombok.RequiredArgsConstructor;
import org.fcs.notifications.microservice.config.props.ServiceClientProperties;
import org.fcs.notifications.microservice.dtos.disciplines.DisciplineDetailsDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DisciplinesServiceClient {
    private final RestClient.Builder restClientBuilder;
    private final ServiceClientProperties serviceClientProperties;

    public DisciplineDetailsDto getDisciplineById(UUID disciplineId) {
        return restClientBuilder.build()
                .get()
                .uri(serviceClientProperties.disciplines().baseUrl() + "/disciplines/{disciplineId}", disciplineId)
                .retrieve()
                .body(DisciplineDetailsDto.class);
    }
}
