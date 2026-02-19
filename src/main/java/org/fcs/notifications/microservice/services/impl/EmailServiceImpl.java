package org.fcs.notifications.microservice.services.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.config.props.MailProperties;
import org.fcs.notifications.microservice.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
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
            log.warn("Письмо не отправлено, так как не настроен mail host: toEmail={}, subject={}", toEmail, subject);
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
            log.info("Письмо отправлено: toEmail={}, subject={}", toEmail, subject);
        } catch (Exception e) {
            log.error("Не удалось отправить письмо: toEmail={}, subject={}", toEmail, subject, e);
            throw new IllegalStateException("Failed to send email to " + toEmail, e);
        }
    }
}
