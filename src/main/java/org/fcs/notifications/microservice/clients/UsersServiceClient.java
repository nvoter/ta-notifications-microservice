package org.fcs.notifications.microservice.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.config.props.ServiceClientProperties;
import org.fcs.notifications.microservice.dtos.internal.GetEmployeesByIdsRequest;
import org.fcs.notifications.microservice.dtos.internal.GetStudentsByIdsRequest;
import org.fcs.notifications.microservice.dtos.users.EmployeeProfileDto;
import org.fcs.notifications.microservice.dtos.users.StudentProfileDto;
import org.fcs.notifications.microservice.dtos.users.StudentReminderRecipientDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsersServiceClient {
    private static final ParameterizedTypeReference<List<StudentReminderRecipientDto>> STUDENT_REMINDERS_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<EmployeeProfileDto>> EMPLOYEES_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient.Builder restClientBuilder;
    private final ServiceClientProperties serviceClientProperties;

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    public StudentProfileDto getStudentById(UUID studentId) {
        log.info("Запрошен профиль студента: studentId={}", studentId);
        return restClientBuilder.build()
                .get()
                .uri(serviceClientProperties.users().baseUrl() + "/internal/students/{studentId}", studentId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(StudentProfileDto.class);
    }

    public List<StudentReminderRecipientDto> getStudentsByIds(List<UUID> studentIds) {
        log.info("Запрошены профили студентов: studentIdsCount={}", studentIds.size());
        return restClientBuilder.build()
                .post()
                .uri(serviceClientProperties.users().baseUrl() + "/internal/students/by-ids")
                .header("X-Internal-Api-Key", internalApiKey)
                .body(new GetStudentsByIdsRequest(studentIds))
                .retrieve()
                .body(STUDENT_REMINDERS_LIST_TYPE);
    }

    public List<EmployeeProfileDto> getEmployeesByIds(List<UUID> employeeIds) {
        log.info("Запрошены профили сотрудников: employeeIdsCount={}", employeeIds.size());
        return restClientBuilder.build()
                .post()
                .uri(serviceClientProperties.users().baseUrl() + "/internal/employees/by-ids")
                .header("X-Internal-Api-Key", internalApiKey)
                .body(new GetEmployeesByIdsRequest(employeeIds))
                .retrieve()
                .body(EMPLOYEES_LIST_TYPE);
    }

    public EmployeeProfileDto getEmployeeById(UUID employeeId) {
        log.info("Запрошен профиль сотрудника: employeeId={}", employeeId);
        return restClientBuilder.build()
                .get()
                .uri(serviceClientProperties.users().baseUrl() + "/internal/employees/{employeeId}", employeeId)
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .body(EmployeeProfileDto.class);
    }
}
