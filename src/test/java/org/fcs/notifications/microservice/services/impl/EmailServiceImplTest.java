package org.fcs.notifications.microservice.services.impl;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.fcs.notifications.microservice.config.props.MailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {
    @Mock
    private JavaMailSender javaMailSender;

    private EmailServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmailServiceImpl(javaMailSender, new MailProperties("from@test.com", "FCS"));
    }

    @Test
    void sendHtmlEmail_whenMailHostMissing_thenDoNotSend() {
        ReflectionTestUtils.setField(service, "mailHost", "");

        service.sendHtmlEmail("user@test.com", "subject", "<b>body</b>");

        verify(javaMailSender, never()).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }

    @Test
    void sendHtmlEmail_whenMailHostNull_thenDoNotSend() {
        ReflectionTestUtils.setField(service, "mailHost", null);

        service.sendHtmlEmail("user@test.com", "subject", "<b>body</b>");

        verify(javaMailSender, never()).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }

    @Test
    void sendHtmlEmail_whenOk_thenSendMessage() {
        ReflectionTestUtils.setField(service, "mailHost", "smtp.local");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendHtmlEmail("user@test.com", "subject", "<b>body</b>");

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void sendHtmlEmail_whenSendFails_thenWrapException() {
        ReflectionTestUtils.setField(service, "mailHost", "smtp.local");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("boom")).when(javaMailSender).send(mimeMessage);

        assertThrows(IllegalStateException.class, () ->
                service.sendHtmlEmail("user@test.com", "subject", "<b>body</b>")
        );
    }
}
