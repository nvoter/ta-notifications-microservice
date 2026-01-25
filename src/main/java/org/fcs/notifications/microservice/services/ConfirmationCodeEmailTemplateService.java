package org.fcs.notifications.microservice.services;

public interface ConfirmationCodeEmailTemplateService {
    String buildHtml(String firstName, String lastName, String middleName, String confirmationCode);
}
