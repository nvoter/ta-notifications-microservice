package org.fcs.notifications.microservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.fcs.notifications.microservice.events.StudentConfirmationCodeGeneratedEvent;
import org.fcs.notifications.microservice.services.ConfirmationCodeEmailTemplateService;
import org.fcs.notifications.microservice.services.EmailService;
import org.fcs.notifications.microservice.services.StudentConfirmationCodeNotificationsProcessor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentConfirmationCodeNotificationsProcessorImpl implements StudentConfirmationCodeNotificationsProcessor {
    private final EmailService emailService;
    private final ConfirmationCodeEmailTemplateService confirmationCodeEmailTemplateService;

    @Override
    public void process(StudentConfirmationCodeGeneratedEvent event) {
        emailService.sendHtmlEmail(
                event.email(),
                "Код подтверждения для входа",
                confirmationCodeEmailTemplateService.buildHtml(
                        event.firstName(),
                        event.lastName(),
                        event.middleName(),
                        event.confirmationCode()
                )
        );
    }
}
