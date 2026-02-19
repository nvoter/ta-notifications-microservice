package org.fcs.notifications.microservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.events.StudentConfirmationCodeGeneratedEvent;
import org.fcs.notifications.microservice.services.StudentConfirmationCodeNotificationsProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentConfirmationCodeEventsListener {
    private final StudentConfirmationCodeNotificationsProcessor studentConfirmationCodeNotificationsProcessor;

    @RabbitListener(queues = "${app.rabbitmq.confirmation-code.queue}")
    public void handleStudentConfirmationCodeGenerated(StudentConfirmationCodeGeneratedEvent event) {
        log.info(
                "Получено событие с кодом подтверждения студента: eventId={}, studentId={}",
                event.eventId(), event.studentId()
        );
        studentConfirmationCodeNotificationsProcessor.process(event);
    }
}
