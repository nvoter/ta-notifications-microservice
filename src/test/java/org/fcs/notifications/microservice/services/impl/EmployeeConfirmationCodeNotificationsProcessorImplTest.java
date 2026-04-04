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
                "Petrov Petr Petrovich",
                "654321",
                OffsetDateTime.now()
        );
        when(confirmationCodeEmailTemplateService.buildHtml("Petrov Petr Petrovich", null, null, "654321"))
                .thenReturn("<html/>");

        service.process(event);

        verify(emailService).sendHtmlEmail("employee@test.com", "Код подтверждения для входа", "<html/>");
    }
}
