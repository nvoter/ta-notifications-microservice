package org.fcs.notifications.microservice.services.impl;

import org.fcs.notifications.microservice.events.EmployeeConfirmationCodeGeneratedEvent;
import org.fcs.notifications.microservice.services.ConfirmationCodeEmailTemplateService;
import org.fcs.notifications.microservice.services.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeConfirmationCodeNotificationsProcessorImplTest {
    @Mock
    private EmailService emailService;
    @Mock
    private ConfirmationCodeEmailTemplateService confirmationCodeEmailTemplateService;
    @InjectMocks
    private EmployeeConfirmationCodeNotificationsProcessorImpl service;

    @Test
    void process_whenOk_thenBuildTemplateAndSendEmail() {
        EmployeeConfirmationCodeGeneratedEvent event = new EmployeeConfirmationCodeGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "employee@test.com",
                "employee.backup@test.com",
                "Petrov Petr Petrovich",
                "654321",
                OffsetDateTime.now()
        );
        when(confirmationCodeEmailTemplateService.buildHtml("Petrov Petr Petrovich", null, null, "654321"))
                .thenReturn("<html/>");

        service.process(event);

        verify(emailService).sendHtmlEmail("employee@test.com", "Код подтверждения для входа", "<html/>");
        verify(emailService).sendHtmlEmail("employee.backup@test.com", "Код подтверждения для входа", "<html/>");
    }

    @Test
    void process_whenBackupEmailBlankOrSame_thenSendSingleEmail() {
        EmployeeConfirmationCodeGeneratedEvent event = new EmployeeConfirmationCodeGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "employee@test.com",
                " employee@test.com ",
                "Petrov Petr Petrovich",
                "654321",
                OffsetDateTime.now()
        );
        when(confirmationCodeEmailTemplateService.buildHtml("Petrov Petr Petrovich", null, null, "654321"))
                .thenReturn("<html/>");

        service.process(event);

        verify(emailService, times(1)).sendHtmlEmail("employee@test.com", "Код подтверждения для входа", "<html/>");
        verify(emailService, never()).sendHtmlEmail(" employee@test.com ", "Код подтверждения для входа", "<html/>");
    }

    @Test
    void process_whenPrimaryEmailBlankAndBackupPresent_thenSendOnlyBackupEmail() {
        EmployeeConfirmationCodeGeneratedEvent event = new EmployeeConfirmationCodeGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "   ",
                "backup@test.com",
                "Petrov Petr Petrovich",
                "654321",
                OffsetDateTime.now()
        );
        when(confirmationCodeEmailTemplateService.buildHtml("Petrov Petr Petrovich", null, null, "654321"))
                .thenReturn("<html/>");

        service.process(event);

        verify(emailService, never()).sendHtmlEmail("   ", "Код подтверждения для входа", "<html/>");
        verify(emailService).sendHtmlEmail("backup@test.com", "Код подтверждения для входа", "<html/>");
    }

    @Test
    void process_whenBackupEmailBlank_thenSkipBackupEmailCompletely() {
        EmployeeConfirmationCodeGeneratedEvent event = new EmployeeConfirmationCodeGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "employee@test.com",
                "   ",
                "Petrov Petr Petrovich",
                "654321",
                OffsetDateTime.now()
        );
        when(confirmationCodeEmailTemplateService.buildHtml("Petrov Petr Petrovich", null, null, "654321"))
                .thenReturn("<html/>");

        service.process(event);

        verify(emailService, times(1)).sendHtmlEmail("employee@test.com", "Код подтверждения для входа", "<html/>");
        verify(emailService, never()).sendHtmlEmail("   ", "Код подтверждения для входа", "<html/>");
    }

    @Test
    void process_whenEmailsNull_thenSkipAllEmails() {
        EmployeeConfirmationCodeGeneratedEvent event = new EmployeeConfirmationCodeGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                "Petrov Petr Petrovich",
                "654321",
                OffsetDateTime.now()
        );
        when(confirmationCodeEmailTemplateService.buildHtml("Petrov Petr Petrovich", null, null, "654321"))
                .thenReturn("<html/>");

        service.process(event);

        verify(emailService, never()).sendHtmlEmail(null, "Код подтверждения для входа", "<html/>");
    }

    @Test
    void process_whenPrimaryEmailNullAndBackupPresent_thenSendBackupEmail() {
        EmployeeConfirmationCodeGeneratedEvent event = new EmployeeConfirmationCodeGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "backup@test.com",
                "Petrov Petr Petrovich",
                "654321",
                OffsetDateTime.now()
        );
        when(confirmationCodeEmailTemplateService.buildHtml("Petrov Petr Petrovich", null, null, "654321"))
                .thenReturn("<html/>");

        service.process(event);

        verify(emailService, never()).sendHtmlEmail(null, "Код подтверждения для входа", "<html/>");
        verify(emailService).sendHtmlEmail("backup@test.com", "Код подтверждения для входа", "<html/>");
    }
}
