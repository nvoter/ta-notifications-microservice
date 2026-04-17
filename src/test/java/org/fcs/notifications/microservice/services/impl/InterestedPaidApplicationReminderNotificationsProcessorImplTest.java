package org.fcs.notifications.microservice.services.impl;

import org.fcs.notifications.microservice.clients.UsersServiceClient;
import org.fcs.notifications.microservice.dtos.reminders.InterestedEmployeeReminderDto;
import org.fcs.notifications.microservice.dtos.users.EmployeeProfileDto;
import org.fcs.notifications.microservice.dtos.users.StudentReminderRecipientDto;
import org.fcs.notifications.microservice.events.InterestedPaidApplicationReminderEvent;
import org.fcs.notifications.microservice.services.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterestedPaidApplicationReminderNotificationsProcessorImplTest {
    @Mock
    private UsersServiceClient usersServiceClient;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private InterestedPaidApplicationReminderNotificationsProcessorImpl service;

    @Test
    void process_whenEventHasNoStudents_thenDoNothing() {
        service.process(new InterestedPaidApplicationReminderEvent(UUID.randomUUID(), List.of(), List.of(), OffsetDateTime.now()));

        verify(usersServiceClient, never()).getStudentsByIds(org.mockito.ArgumentMatchers.anyList());
        verify(usersServiceClient, never()).getEmployeesByIds(org.mockito.ArgumentMatchers.anyList());
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    void process_whenStudentsReturned_thenSendEmails() {
        UUID firstStudentId = UUID.randomUUID();
        UUID secondStudentId = UUID.randomUUID();
        InterestedPaidApplicationReminderEvent event = new InterestedPaidApplicationReminderEvent(
                UUID.randomUUID(),
                List.of(firstStudentId, secondStudentId),
                List.of(),
                OffsetDateTime.now()
        );
        when(usersServiceClient.getStudentsByIds(event.studentIds())).thenReturn(List.of(
                new StudentReminderRecipientDto(firstStudentId, "first@test.com", "Иванов", "Иван", "Иванович"),
                new StudentReminderRecipientDto(secondStudentId, "second@test.com", "Петров", "Петр", null)
        ));

        service.process(event);

        verify(emailService).sendHtmlEmail(
                eq("first@test.com"),
                eq("Напоминание о загрузке документов"),
                contains("Иван Иванович, Вы подавали заявку на трудоустройство учебным ассистентом на платной основе")
        );
        verify(emailService).sendHtmlEmail(
                eq("second@test.com"),
                eq("Напоминание о загрузке документов"),
                contains("Если Вы недавно уже заполняли форму, проигнорируйте это письмо")
        );
    }

    @Test
    void process_whenStudentEmailBlank_thenSkipEmail() {
        UUID studentId = UUID.randomUUID();
        InterestedPaidApplicationReminderEvent event = new InterestedPaidApplicationReminderEvent(
                UUID.randomUUID(),
                List.of(studentId),
                List.of(),
                OffsetDateTime.now()
        );
        when(usersServiceClient.getStudentsByIds(event.studentIds())).thenReturn(List.of(
                new StudentReminderRecipientDto(studentId, "   ", "Иванов", "Иван", null)
        ));

        service.process(event);

        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    void process_whenEmployeeRemindersReturned_thenSendGroupedEmailsToTeacherAndBackup() {
        UUID employeeId = UUID.randomUUID();
        UUID firstStudentId = UUID.randomUUID();
        UUID secondStudentId = UUID.randomUUID();
        InterestedPaidApplicationReminderEvent event = new InterestedPaidApplicationReminderEvent(
                UUID.randomUUID(),
                List.of(firstStudentId, secondStudentId),
                List.of(new InterestedEmployeeReminderDto(employeeId, List.of(secondStudentId, firstStudentId))),
                OffsetDateTime.now()
        );
        when(usersServiceClient.getStudentsByIds(event.studentIds())).thenReturn(List.of(
                new StudentReminderRecipientDto(firstStudentId, "first@test.com", "Иванов", "Иван", "Иванович"),
                new StudentReminderRecipientDto(secondStudentId, "second@test.com", "Петров", "Петр", null)
        ));
        when(usersServiceClient.getEmployeesByIds(List.of(employeeId))).thenReturn(List.of(
                new EmployeeProfileDto(
                        employeeId,
                        "teacher@test.com",
                        "Петров Петр Петрович",
                        "teacher.backup@test.com",
                        "TEACHER",
                        true,
                        true,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        ));

        service.process(event);

        verify(emailService).sendHtmlEmail(
                eq("teacher@test.com"),
                eq("Напоминание о загрузке документов студентов"),
                contains("Петров Петр, следующие студенты")
        );
        verify(emailService).sendHtmlEmail(
                eq("teacher.backup@test.com"),
                eq("Напоминание о загрузке документов студентов"),
                contains("Иванов Иван Иванович")
        );
        verify(emailService).sendHtmlEmail(
                eq("teacher@test.com"),
                eq("Напоминание о загрузке документов студентов"),
                contains("Петров Петр")
        );
        verify(emailService).sendHtmlEmail(
                eq("teacher@test.com"),
                eq("Напоминание о загрузке документов студентов"),
                contains("Пожалуйста, напомните им заполнить форму сбора документов")
        );
    }

    @Test
    void process_whenTeacherBackupEmailEqualsPrimary_thenSendTeacherEmailOnce() {
        UUID employeeId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        InterestedPaidApplicationReminderEvent event = new InterestedPaidApplicationReminderEvent(
                UUID.randomUUID(),
                List.of(studentId),
                List.of(new InterestedEmployeeReminderDto(employeeId, List.of(studentId))),
                OffsetDateTime.now()
        );
        when(usersServiceClient.getStudentsByIds(event.studentIds())).thenReturn(List.of(
                new StudentReminderRecipientDto(studentId, "student@test.com", "Иванов", "Иван", "Иванович")
        ));
        when(usersServiceClient.getEmployeesByIds(List.of(employeeId))).thenReturn(List.of(
                new EmployeeProfileDto(
                        employeeId,
                        "teacher@test.com",
                        "Петров Петр Петрович",
                        " teacher@test.com ",
                        "TEACHER",
                        true,
                        true,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        ));

        service.process(event);

        verify(emailService, times(1)).sendHtmlEmail(
                eq("teacher@test.com"),
                eq("Напоминание о загрузке документов студентов"),
                contains("Иванов Иван Иванович")
        );
    }
}
