package org.fcs.notifications.microservice.clients;

import lombok.RequiredArgsConstructor;
import org.fcs.notifications.microservice.config.props.ServiceClientProperties;
import org.fcs.notifications.microservice.dtos.users.EmployeeProfileDto;
import org.fcs.notifications.microservice.dtos.users.StudentProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsersServiceClient {
    private final RestClient.Builder restClientBuilder;
    private final ServiceClientProperties serviceClientProperties;

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    public StudentProfileDto getStudentById(UUID studentId) {
        return restClientBuilder.build()
                .get()
                .uri(serviceClientProperties.users().baseUrl() + "/internal/students/{studentId}", studentId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(StudentProfileDto.class);
    }

    public EmployeeProfileDto getEmployeeById(UUID employeeId) {
        return restClientBuilder.build()
                .get()
                .uri(serviceClientProperties.users().baseUrl() + "/internal/employees/{employeeId}", employeeId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(EmployeeProfileDto.class);
    }
}
