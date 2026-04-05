package org.fcs.notifications.microservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.fcs.notifications.microservice.events.EmployeeConfirmationCodeGeneratedEvent;
import org.fcs.notifications.microservice.services.ConfirmationCodeEmailTemplateService;
import org.fcs.notifications.microservice.services.EmailService;
import org.fcs.notifications.microservice.services.EmployeeConfirmationCodeNotificationsProcessor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeConfirmationCodeNotificationsProcessorImpl implements EmployeeConfirmationCodeNotificationsProcessor {
    private final EmailService emailService;
    private final ConfirmationCodeEmailTemplateService confirmationCodeEmailTemplateService;

    @Override
    public void process(EmployeeConfirmationCodeGeneratedEvent event) {
        String subject = "Код подтверждения для входа";
        String htmlBody = confirmationCodeEmailTemplateService.buildHtml(
                event.fullName(),
                null,
                null,
                event.confirmationCode()
        );

        sendEmailIfPresent(event.email(), subject, htmlBody);
        if (isDistinctEmail(event.backupEmail(), event.email())) {
            sendEmailIfPresent(event.backupEmail(), subject, htmlBody);
        }
    }

    private void sendEmailIfPresent(String email, String subject, String htmlBody) {
        if (email == null || email.isBlank()) {
            return;
        }

        emailService.sendHtmlEmail(email, subject, htmlBody);
    }

    private boolean isDistinctEmail(String email, String referenceEmail) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (referenceEmail == null || referenceEmail.isBlank()) {
            return true;
        }

        return !email.trim().equalsIgnoreCase(referenceEmail.trim());
    }
}
