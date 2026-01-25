package org.fcs.notifications.microservice.services.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.fcs.notifications.microservice.config.props.MailProperties;
import org.fcs.notifications.microservice.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Override
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        if (mailHost == null || mailHost.isBlank()) {
            System.out.println("Mail host is not configured. Email to " + toEmail + ": " + subject);
            return;
        }

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(mailProperties.from(), mailProperties.fromName());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send email to " + toEmail, e);
        }
    }
}
