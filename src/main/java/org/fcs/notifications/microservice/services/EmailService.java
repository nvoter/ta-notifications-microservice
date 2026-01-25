package org.fcs.notifications.microservice.services;

public interface EmailService {
    void sendHtmlEmail(String toEmail, String subject, String htmlBody);
}
