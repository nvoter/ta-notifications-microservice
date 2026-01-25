package org.fcs.notifications.microservice.messaging;

import lombok.RequiredArgsConstructor;
import org.fcs.notifications.microservice.events.StudentConfirmationCodeGeneratedEvent;
import org.fcs.notifications.microservice.services.StudentConfirmationCodeNotificationsProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentConfirmationCodeEventsListener {
    private final StudentConfirmationCodeNotificationsProcessor studentConfirmationCodeNotificationsProcessor;

    @RabbitListener(queues = "${app.rabbitmq.confirmation-code.queue}")
    public void handleStudentConfirmationCodeGenerated(StudentConfirmationCodeGeneratedEvent event) {
        studentConfirmationCodeNotificationsProcessor.process(event);
    }
}
