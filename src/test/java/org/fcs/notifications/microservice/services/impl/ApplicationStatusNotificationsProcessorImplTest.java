package org.fcs.notifications.microservice.services.impl;

import org.fcs.notifications.microservice.clients.DisciplinesServiceClient;
import org.fcs.notifications.microservice.clients.UsersServiceClient;
import org.fcs.notifications.microservice.dtos.disciplines.DisciplineDetailsDto;
import org.fcs.notifications.microservice.dtos.users.EmployeeProfileDto;
import org.fcs.notifications.microservice.dtos.users.StudentProfileDto;
import org.fcs.notifications.microservice.entities.Notification;
import org.fcs.notifications.microservice.events.ApplicationDisciplineStatusUpdatedEvent;
import org.fcs.notifications.microservice.repositories.NotificationsRepository;
import org.fcs.notifications.microservice.services.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationStatusNotificationsProcessorImplTest {
    @Mock
    private NotificationsRepository notificationsRepository;
    @Mock
    private UsersServiceClient usersServiceClient;
    @Mock
    private DisciplinesServiceClient disciplinesServiceClient;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private ApplicationStatusNotificationsProcessorImpl service;

    @Test
    void process_whenBothNotificationsAlreadyExist_thenSkip() {
        ApplicationDisciplineStatusUpdatedEvent event = event("APPROVED");
        when(notificationsRepository.existsByEventIdAndRecipientUserId(event.eventId(), event.studentId())).thenReturn(true);
        when(notificationsRepository.existsByEventIdAndRecipientUserId(event.eventId(), event.employeeId())).thenReturn(true);

        service.process(event);

        verify(usersServiceClient, never()).getEmployeeById(any());
        verify(emailService, never()).sendHtmlEmail(any(), any(), any());
    }

    @Test
    void process_whenNotificationsMissing_thenSaveAndSendEmails() {
        ApplicationDisciplineStatusUpdatedEvent event = event("APPROVED");
        StudentProfileDto student = student(event.studentId(), "student@test.com", "Иванов", "Иван", " ", " ");
        EmployeeProfileDto employee = employee(event.employeeId(), "teacher@test.com", "Петров", "Петр", "Петрович");
        DisciplineDetailsDto discipline = new DisciplineDetailsDto(event.disciplineId(), UUID.randomUUID(), "Math & <Stats>", "1", java.util.List.of(1), 1, 1, "assignment");
        when(notificationsRepository.existsByEventIdAndRecipientUserId(any(), any())).thenReturn(false);
        when(usersServiceClient.getEmployeeById(event.employeeId())).thenReturn(employee);
        when(usersServiceClient.getStudentById(event.studentId())).thenReturn(student);
        when(disciplinesServiceClient.getDisciplineById(event.disciplineId())).thenReturn(discipline);

        service.process(event);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationsRepository, times(2)).save(notificationCaptor.capture());
        assertEquals(2, notificationCaptor.getAllValues().size());
        assertTrue(notificationCaptor.getAllValues().getFirst().getMessage().contains("Утвержден"));
        assertTrue(notificationCaptor.getAllValues().getLast().getMessage().contains("Иванов Иван"));
        verify(emailService).sendHtmlEmail(eq("student@test.com"), eq("Изменен статус заявки на дисциплину"), any());
        verify(emailService).sendHtmlEmail(eq("teacher@test.com"), eq("Подтверждение изменения статуса заявки"), any());
    }

    @Test
    void process_whenStudentNotificationExists_thenSaveOnlyEmployeeNotification() {
        ApplicationDisciplineStatusUpdatedEvent event = event("UNKNOWN");
        when(notificationsRepository.existsByEventIdAndRecipientUserId(event.eventId(), event.studentId())).thenReturn(true);
        when(notificationsRepository.existsByEventIdAndRecipientUserId(event.eventId(), event.employeeId())).thenReturn(false);
        when(usersServiceClient.getEmployeeById(event.employeeId())).thenReturn(employee(event.employeeId(), "teacher@test.com", "Petrov", "Petr", null));
        when(usersServiceClient.getStudentById(event.studentId())).thenReturn(student(event.studentId(), null, null, " ", null, "tg"));
        when(disciplinesServiceClient.getDisciplineById(event.disciplineId()))
                .thenReturn(new DisciplineDetailsDto(event.disciplineId(), UUID.randomUUID(), "Discipline", "1", java.util.List.of(1), 1, 1, "assignment"));

        service.process(event);

        verify(notificationsRepository, times(1)).save(any(Notification.class));
        verify(emailService, times(2)).sendHtmlEmail(any(), any(), any());
    }

    private static ApplicationDisciplineStatusUpdatedEvent event(String status) {
        return new ApplicationDisciplineStatusUpdatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                status,
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.now()
        );
    }

    private static StudentProfileDto student(UUID id, String email, String lastName, String firstName, String middleName, String telegram) {
        return new StudentProfileDto(
                id,
                email,
                lastName,
                firstName,
                middleName,
                null,
                telegram,
                null,
                null,
                null,
                null,
                null,
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    private static EmployeeProfileDto employee(UUID id, String email, String lastName, String firstName, String middleName) {
        return new EmployeeProfileDto(
                id,
                email,
                lastName,
                firstName,
                middleName,
                "TEACHER",
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}
