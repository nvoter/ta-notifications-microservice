package org.fcs.notifications.microservice.services.impl;

import org.fcs.notifications.microservice.events.StudentConfirmationCodeGeneratedEvent;
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
class StudentConfirmationCodeNotificationsProcessorImplTest {
    @Mock
    private EmailService emailService;
    @Mock
    private ConfirmationCodeEmailTemplateService confirmationCodeEmailTemplateService;
    @InjectMocks
    private StudentConfirmationCodeNotificationsProcessorImpl service;

    @Test
    void process_whenOk_thenBuildTemplateAndSendEmail() {
        StudentConfirmationCodeGeneratedEvent event = new StudentConfirmationCodeGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "student@test.com",
                "Ivanov",
                "Ivan",
                "Ivanovich",
                "123456",
                OffsetDateTime.now()
        );
        when(confirmationCodeEmailTemplateService.buildHtml("Ivan", "Ivanov", "Ivanovich", "123456"))
                .thenReturn("<html/>");

        service.process(event);

        verify(emailService).sendHtmlEmail("student@test.com", "Код подтверждения для входа", "<html/>");
    }
}
