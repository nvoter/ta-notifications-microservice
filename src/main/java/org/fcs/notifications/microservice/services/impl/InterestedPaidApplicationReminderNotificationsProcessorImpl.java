package org.fcs.notifications.microservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.clients.UsersServiceClient;
import org.fcs.notifications.microservice.dtos.users.StudentReminderRecipientDto;
import org.fcs.notifications.microservice.events.InterestedPaidApplicationReminderEvent;
import org.fcs.notifications.microservice.services.EmailService;
import org.fcs.notifications.microservice.services.InterestedPaidApplicationReminderNotificationsProcessor;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestedPaidApplicationReminderNotificationsProcessorImpl
        implements InterestedPaidApplicationReminderNotificationsProcessor {
    private final UsersServiceClient usersServiceClient;
    private final EmailService emailService;

    @Override
    public void process(InterestedPaidApplicationReminderEvent event) {
        if (event.studentIds() == null || event.studentIds().isEmpty()) {
            log.info("Событие-напоминание не содержит получателей: eventId={}", event.eventId());
            return;
        }

        List<StudentReminderRecipientDto> students = usersServiceClient.getStudentsByIds(event.studentIds());

        for (StudentReminderRecipientDto student : students) {
            if (student.email() == null || student.email().isBlank()) {
                log.warn("Пропущена отправка напоминания студенту без email: studentId={}", student.id());
                continue;
            }

            emailService.sendHtmlEmail(
                    student.email(),
                    "Напоминание о загрузке документов",
                    buildEmailHtml(student)
            );
        }

        log.info(
                "Отправлены напоминания о загрузке документов: eventId={}, recipientsCount={}",
                event.eventId(),
                students.size()
        );
    }

    private String buildEmailHtml(StudentReminderRecipientDto student) {
        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                  <meta charset="UTF-8">
                  <style>
                    body {
                      margin: 0;
                      padding: 40px 16px;
                      background:
                        radial-gradient(circle at top left, rgba(16, 45, 105, 0.08), transparent 28%%),
                        linear-gradient(180deg, #f7f9fc 0%%, #eef2f8 100%%);
                      font-family: Roboto, Arial, sans-serif;
                      color: #102d69;
                    }

                    .shell {
                      max-width: 520px;
                      margin: 0 auto;
                    }

                    .card {
                      width: 100%%;
                      box-sizing: border-box;
                      border: 1px solid rgba(16, 45, 105, 0.12);
                      border-radius: 28px;
                      padding: 36px;
                      background-color: rgba(255, 255, 255, 0.94);
                      box-shadow: 0 28px 80px rgba(16, 45, 105, 0.1);
                    }

                    .title {
                      margin: 0;
                      font-size: 36px;
                      line-height: 1.05;
                      letter-spacing: -0.03em;
                      font-weight: 700;
                      color: #102d69;
                    }

                    .description {
                      margin: 12px 0 0;
                      color: #56637f;
                      font-size: 16px;
                      line-height: 1.5;
                    }

                    .footnotes {
                      margin-top: 32px;
                    }

                    .footnote {
                      margin: 8px 0 0;
                      text-align: left;
                      color: #56637f;
                      font-size: 16px;
                      line-height: 1.5;
                    }
                  </style>
                </head>
                <body>
                  <div class="shell">
                    <div class="card">
                      <h1 class="title">Напоминание о документах</h1>
                      <p class="description">%s, Вы подавали заявку на трудоустройство учебным ассистентом на платной основе. Пожалуйста, заполните форму сбора документов в личном кабинете для дальнейшей обработки заявки.</p>
                      <div class="footnotes">
                        <p class="footnote">Если Вы недавно уже заполняли форму, проигнорируйте это письмо.</p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(escapeHtml(fallback(student.displayName(), "Здравствуйте")));
    }

    private String fallback(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
