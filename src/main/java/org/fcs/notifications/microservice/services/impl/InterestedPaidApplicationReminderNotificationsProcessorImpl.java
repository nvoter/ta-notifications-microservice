package org.fcs.notifications.microservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.clients.UsersServiceClient;
import org.fcs.notifications.microservice.dtos.reminders.InterestedEmployeeReminderDto;
import org.fcs.notifications.microservice.dtos.users.EmployeeProfileDto;
import org.fcs.notifications.microservice.dtos.users.StudentReminderRecipientDto;
import org.fcs.notifications.microservice.events.InterestedPaidApplicationReminderEvent;
import org.fcs.notifications.microservice.services.EmailService;
import org.fcs.notifications.microservice.services.InterestedPaidApplicationReminderNotificationsProcessor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestedPaidApplicationReminderNotificationsProcessorImpl
        implements InterestedPaidApplicationReminderNotificationsProcessor {
    private static final String DOCUMENTS_URL = "https://assistent.cs.hse.ru/student/documents";

    private final UsersServiceClient usersServiceClient;
    private final EmailService emailService;

    @Override
    public void process(InterestedPaidApplicationReminderEvent event) {
        boolean hasStudentRecipients = event.studentIds() != null && !event.studentIds().isEmpty();
        boolean hasEmployeeRecipients = event.employeeReminders() != null && !event.employeeReminders().isEmpty();
        if (!hasStudentRecipients && !hasEmployeeRecipients) {
            log.info("Событие-напоминание не содержит получателей: eventId={}", event.eventId());
            return;
        }

        List<StudentReminderRecipientDto> students = hasStudentRecipients
                ? usersServiceClient.getStudentsByIds(event.studentIds())
                : List.of();
        Map<UUID, StudentReminderRecipientDto> studentsById = students.stream()
                .collect(Collectors.toMap(StudentReminderRecipientDto::id, Function.identity()));

        sendStudentEmails(students);
        sendEmployeeEmails(event.employeeReminders(), studentsById);

        log.info(
                "Отправлены напоминания о загрузке документов: eventId={}, studentRecipientsCount={}, employeeRecipientsCount={}",
                event.eventId(),
                students.size(),
                hasEmployeeRecipients ? event.employeeReminders().size() : 0
        );
    }

    private void sendStudentEmails(List<StudentReminderRecipientDto> students) {
        for (StudentReminderRecipientDto student : students) {
            if (student.email() == null || student.email().isBlank()) {
                log.warn("Пропущена отправка напоминания студенту без email: studentId={}", student.id());
                continue;
            }

            emailService.sendHtmlEmail(student.email(), "Напоминание о загрузке документов", buildStudentEmailHtml(student));
        }
    }

    private void sendEmployeeEmails(
            List<InterestedEmployeeReminderDto> employeeReminders,
            Map<UUID, StudentReminderRecipientDto> studentsById
    ) {
        if (employeeReminders == null || employeeReminders.isEmpty()) {
            return;
        }

        List<UUID> employeeIds = employeeReminders.stream()
                .map(InterestedEmployeeReminderDto::employeeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (employeeIds.isEmpty()) {
            return;
        }

        Map<UUID, EmployeeProfileDto> employeesById = usersServiceClient.getEmployeesByIds(employeeIds).stream()
                .collect(Collectors.toMap(EmployeeProfileDto::id, Function.identity()));

        for (InterestedEmployeeReminderDto reminder : employeeReminders) {
            EmployeeProfileDto employee = employeesById.get(reminder.employeeId());
            if (employee == null) {
                log.warn("Пропущена отправка напоминания преподавателю: employeeId={} не найден", reminder.employeeId());
                continue;
            }

            List<StudentReminderRecipientDto> students = reminder.studentIds().stream()
                    .map(studentsById::get)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(StudentReminderRecipientDto::fullName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            if (students.isEmpty()) {
                continue;
            }

            String subject = "Напоминание о загрузке документов студентов";
            String htmlBody = buildEmployeeEmailHtml(employee, students);
            sendEmailToEmployeeAddresses(employee.email(), employee.backupEmail(), subject, htmlBody);
        }
    }

    private String buildStudentEmailHtml(StudentReminderRecipientDto student) {
        return buildEmailShell(
                "Напоминание о документах",
                """
                <p class="description">%s, Вы подавали заявку на трудоустройство учебным ассистентом на платной основе. Пожалуйста, заполните <a href="%s">форму сбора документов в личном кабинете</a> для дальнейшей обработки заявки</p>
                <div class="footnotes">
                  <p class="footnote">Если Вы недавно уже заполняли форму, проигнорируйте это письмо</p>
                </div>
                """.formatted(
                        escapeHtml(fallback(student.displayName(), "Здравствуйте")),
                        DOCUMENTS_URL
                )
        );
    }

    private String buildEmployeeEmailHtml(EmployeeProfileDto employee, List<StudentReminderRecipientDto> students) {
        String studentsHtml = students.stream()
                .map(student -> "<p class=\"detail-line\">" + escapeHtml(student.fullName()) + "</p>")
                .collect(Collectors.joining());

        return buildEmailShell(
                "Напоминание о документах",
                """
                <p class="description">%s, следующие студенты, заявкам которых Вы выставили статус «Заинтересован», все еще не предоставили пакет необходимых документов:</p>
                <div class="details">
                  %s
                </div>
                <div class="footnotes">
                  <p class="footnote">Пожалуйста, напомните им заполнить <a href="%s">форму сбора документов в личном кабинете</a> для дальнейшей обработки заявки</p>
                </div>
                """.formatted(
                        escapeHtml(fallback(shortEmployeeName(employee.fullName()), "Здравствуйте")),
                        studentsHtml,
                        DOCUMENTS_URL
                )
        );
    }

    private String buildEmailShell(String title, String content) {
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

                    .details {
                      margin-top: 24px;
                      border-radius: 24px;
                      padding: 24px;
                      background: rgba(16, 45, 105, 0.05);
                    }

                    .detail-line {
                      margin: 8px 0 0;
                      color: #102d69;
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
                      <h1 class="title">%s</h1>
                      %s
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(escapeHtml(title), content);
    }

    private void sendEmailToEmployeeAddresses(String email, String backupEmail, String subject, String htmlBody) {
        sendEmailIfPresent(email, subject, htmlBody);
        if (!isDistinctEmail(backupEmail, email)) {
            return;
        }

        sendEmailIfPresent(backupEmail, subject, htmlBody);
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

    private String shortEmployeeName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Здравствуйте";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length < 2) {
            return fullName.trim();
        }

        return parts[0] + " " + parts[1];
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
