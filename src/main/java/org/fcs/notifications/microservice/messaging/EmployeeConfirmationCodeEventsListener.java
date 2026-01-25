package org.fcs.notifications.microservice.messaging;

import lombok.RequiredArgsConstructor;
import org.fcs.notifications.microservice.events.EmployeeConfirmationCodeGeneratedEvent;
import org.fcs.notifications.microservice.services.EmployeeConfirmationCodeNotificationsProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeConfirmationCodeEventsListener {
    private final EmployeeConfirmationCodeNotificationsProcessor employeeConfirmationCodeNotificationsProcessor;

    @RabbitListener(queues = "${app.rabbitmq.employee-confirmation-code.queue}")
    public void handleEmployeeConfirmationCodeGenerated(EmployeeConfirmationCodeGeneratedEvent event) {
        employeeConfirmationCodeNotificationsProcessor.process(event);
    }
}
