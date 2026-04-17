package org.fcs.notifications.microservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.clients.DisciplinesServiceClient;
import org.fcs.notifications.microservice.clients.UsersServiceClient;
import org.fcs.notifications.microservice.dtos.disciplines.DisciplineDetailsDto;
import org.fcs.notifications.microservice.dtos.users.EmployeeProfileDto;
import org.fcs.notifications.microservice.dtos.users.StudentProfileDto;
import org.fcs.notifications.microservice.entities.Notification;
import org.fcs.notifications.microservice.events.ApplicationDisciplineStatusUpdatedEvent;
import org.fcs.notifications.microservice.models.EntityType;
import org.fcs.notifications.microservice.models.NotificationType;
import org.fcs.notifications.microservice.repositories.NotificationsRepository;
import org.fcs.notifications.microservice.services.ApplicationStatusNotificationsProcessor;
import org.fcs.notifications.microservice.services.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationStatusNotificationsProcessorImpl implements ApplicationStatusNotificationsProcessor {
    private final NotificationsRepository notificationsRepository;
    private final UsersServiceClient usersServiceClient;
    private final DisciplinesServiceClient disciplinesServiceClient;
    private final EmailService emailService;

    @Override
    @Transactional
    public void process(ApplicationDisciplineStatusUpdatedEvent event) {
        boolean studentNotificationExists =
                notificationsRepository.existsByEventIdAndRecipientUserId(event.eventId(), event.studentId());
        boolean employeeNotificationExists =
                notificationsRepository.existsByEventIdAndRecipientUserId(event.eventId(), event.employeeId());
        if (studentNotificationExists && employeeNotificationExists) {
            log.info("Пропущена повторная обработка события статуса заявки: eventId={}", event.eventId());
            return;
        }

        EmployeeProfileDto employee = usersServiceClient.getEmployeeById(event.employeeId());
        StudentProfileDto student = usersServiceClient.getStudentById(event.studentId());
        DisciplineDetailsDto discipline = disciplinesServiceClient.getDisciplineById(event.disciplineId());

        saveNotificationIfMissing(
                event,
                student.id(),
                "Статус заявки обновлен",
                buildStudentNotificationMessage(discipline.name(), event.newStatus(), employee)
        );

        saveNotificationIfMissing(
                event,
                employee.id(),
                "Статус заявки обновлен",
                buildEmployeeNotificationMessage(student, discipline.name(), event.newStatus())
        );

        sendStudentEmail(student, employee, discipline.name(), discipline.assignment(), event.newStatus());
        if (employee.isStatusNotificationEmailEnabled()) {
            sendEmployeeEmail(employee, student, discipline.name(), discipline.assignment(), event.newStatus());
        }
        sendPreviousEmployeeMandatoryEmailIfNeeded(event, employee, student, discipline);
        log.info(
                "Обработано событие обновления статуса заявки: eventId={}, applicationDisciplineId={}",
                event.eventId(),
                event.applicationDisciplineId()
        );
    }

    private void saveNotificationIfMissing(
            ApplicationDisciplineStatusUpdatedEvent event,
            UUID recipientUserId,
            String title,
            String message
    ) {
        if (notificationsRepository.existsByEventIdAndRecipientUserId(event.eventId(), recipientUserId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setEventId(event.eventId());
        notification.setRecipientUserId(recipientUserId);
        notification.setEntityType(EntityType.APPLICATION_DISCIPLINE);
        notification.setEntityId(event.applicationDisciplineId());
        notification.setNotificationType(NotificationType.APPLICATION_STATUS_UPDATED);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setCreatedAt(OffsetDateTime.now());
        notification.setRead(false);
        notificationsRepository.save(notification);
    }

    private void sendStudentEmail(
            StudentProfileDto student,
            EmployeeProfileDto employee,
            String disciplineName,
            String assignment,
            String newStatus
    ) {
        emailService.sendHtmlEmail(
                student.email(),
                "Изменен статус заявки на дисциплину",
                buildStudentEmailHtml(student, employee, disciplineName, assignment, newStatus)
        );
    }

    private void sendEmployeeEmail(
            EmployeeProfileDto employee,
            StudentProfileDto student,
            String disciplineName,
            String assignment,
            String newStatus
    ) {
        String subject = "Подтверждение изменения статуса заявки";
        String htmlBody = buildEmployeeEmailHtml(student, disciplineName, assignment, newStatus);
        sendEmailToEmployeeAddresses(employee.email(), employee.backupEmail(), subject, htmlBody);
    }

    private void sendPreviousEmployeeMandatoryEmailIfNeeded(
            ApplicationDisciplineStatusUpdatedEvent event,
            EmployeeProfileDto currentEmployee,
            StudentProfileDto student,
            DisciplineDetailsDto discipline
    ) {
        if (!isMandatoryPreviousEmployeeEmail(event)) {
            return;
        }
        if (event.previousEmployeeId() == null || event.previousEmployeeId().equals(currentEmployee.id())) {
            return;
        }

        EmployeeProfileDto previousEmployee = usersServiceClient.getEmployeeById(event.previousEmployeeId());
        String subject = "Заявка студента была " + russianStatus(event.newStatus()).toLowerCase();
        String htmlBody = buildPreviousEmployeeEmailHtml(previousEmployee, currentEmployee, student, discipline.name(), event.newStatus());
        sendEmailToEmployeeAddresses(previousEmployee.email(), previousEmployee.backupEmail(), subject, htmlBody);
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

    private String buildStudentNotificationMessage(String disciplineName, String newStatus, EmployeeProfileDto employee) {
        return "По дисциплине \"" + disciplineName + "\" установлен статус " + russianStatus(newStatus)
                + ". Ответственный сотрудник: " + employee.fullName() + " (" + employee.email() + ").";
    }

    private String buildEmployeeNotificationMessage(StudentProfileDto student, String disciplineName, String newStatus) {
        return "Вы установили статус " + russianStatus(newStatus)
                + " заявке студента " + student.fullName()
                + " на дисциплину " + disciplineName + ".";
    }

    private String buildStudentEmailHtml(
            StudentProfileDto student,
            EmployeeProfileDto employee,
            String disciplineName,
            String assignment,
            String newStatus
    ) {
        return buildEmailShell(
                "Статус заявки обновлен",
                """
                <p class="description">%s, статус Вашей заявки на дисциплину %s изменен на %s</p>
                <div class="details">
                  %s
                  <p class="detail-line">Сотрудник, изменивший статус:</p>
                  <p class="detail-line">%s</p>
                  <p class="detail-line">При возникновении вопросов можете связаться с ним по почте</p>
                  <p class="detail-line">%s</p>
                </div>
                """.formatted(
                        escapeHtml(displayName(student.firstName(), student.middleName())),
                        escapeHtml(disciplineName),
                        escapeHtml(russianStatus(newStatus)),
                        buildAssignmentHtml(newStatus, assignment),
                        escapeHtml(employee.fullName()),
                        escapeHtml(employee.email())
                )
        );
    }

    private String buildEmployeeEmailHtml(
            StudentProfileDto student,
            String disciplineName,
            String assignment,
            String newStatus
    ) {
        return buildEmailShell(
                "Статус заявки обновлен",
                """
                <p class="description">Вы установили статус %s заявке студента %s на дисциплину %s</p>
                <div class="details">
                  %s
                  <p class="detail-line">Контакты студента для связи:</p>
                  <p class="detail-line">Telegram: %s</p>
                  <p class="detail-line">Почта: %s</p>
                </div>
                """.formatted(
                        escapeHtml(russianStatus(newStatus)),
                        escapeHtml(student.fullName()),
                        escapeHtml(disciplineName),
                        buildAssignmentHtml(newStatus, assignment),
                        escapeHtml(fallback(student.telegram(), "Не указан")),
                        escapeHtml(fallback(student.email(), "Не указана"))
                )
        );
    }

    private String buildPreviousEmployeeEmailHtml(
            EmployeeProfileDto previousEmployee,
            EmployeeProfileDto currentEmployee,
            StudentProfileDto student,
            String disciplineName,
            String newStatus
    ) {
        return buildEmailShell(
                "Статус заявки изменен",
                """
                <p class="description">%s, заявка студента %s на дисциплину %s была %s</p>
                <div class="details">
                  <p class="detail-line">Новый статус был установлен сотрудником:</p>
                  <p class="detail-line">%s</p>
                  <p class="detail-line">Email сотрудника:</p>
                  <p class="detail-line">%s</p>
                </div>
                """.formatted(
                        escapeHtml(fallback(previousEmployee.fullName(), "Здравствуйте")),
                        escapeHtml(student.fullName()),
                        escapeHtml(disciplineName),
                        escapeHtml(russianStatus(newStatus).toLowerCase()),
                        escapeHtml(currentEmployee.fullName()),
                        escapeHtml(fallback(currentEmployee.email(), "Не указан"))
                )
        );
    }

    private String buildAssignmentHtml(String status, String assignment) {
        if (!"INTERESTED".equals(normalizeStatus(status))) {
            return "";
        }

        return """
                <p class="detail-line">Задание дисциплины:</p>
                <p class="detail-line">%s</p>
                """.formatted(escapeHtml(fallback(assignment, "Не указано")));
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
                      margin-top: 32px;
                    }

                    .detail-line {
                      margin: 8px 0 0;
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
                """.formatted(
                escapeHtml(title),
                content
        );
    }

    private boolean isMandatoryPreviousEmployeeEmail(ApplicationDisciplineStatusUpdatedEvent event) {
        String normalizedNewStatus = normalizeStatus(event.newStatus());
        String normalizedPreviousStatus = normalizeStatus(event.previousStatus());
        return ("REJECTED".equals(normalizedNewStatus) || "DELETED".equals(normalizedNewStatus))
                && !normalizedPreviousStatus.isBlank()
                && !"NEW".equals(normalizedPreviousStatus);
    }

    private String russianStatus(String status) {
        return switch (normalizeStatus(status)) {
            case "NEW" -> "Новый";
            case "INTERESTED" -> "Заинтересован";
            case "AGREED" -> "На согласовании";
            case "REJECTED" -> "Отклонено";
            case "APPROVED" -> "Утвержден";
            case "DELETED" -> "Удалено";
            default -> status;
        };
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private String displayName(String firstName, String middleName) {
        String value = String.join(" ", fallback(firstName, ""), fallback(middleName, "")).trim();
        return value.isBlank() ? "Здравствуйте" : value;
    }

    private String fallback(String value, String fallbackValue) {
        return value == null || value.isBlank() ? fallbackValue : value;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
