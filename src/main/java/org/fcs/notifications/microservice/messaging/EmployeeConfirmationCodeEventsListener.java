package org.fcs.notifications.microservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.events.EmployeeConfirmationCodeGeneratedEvent;
import org.fcs.notifications.microservice.services.EmployeeConfirmationCodeNotificationsProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeConfirmationCodeEventsListener {
    private final EmployeeConfirmationCodeNotificationsProcessor employeeConfirmationCodeNotificationsProcessor;

    @RabbitListener(queues = "${app.rabbitmq.employee-confirmation-code.queue}")
    public void handleEmployeeConfirmationCodeGenerated(EmployeeConfirmationCodeGeneratedEvent event) {
        log.info(
                "Получено событие с кодом подтверждения сотрудника: eventId={}, employeeId={}",
                event.eventId(),
                event.employeeId()
        );
        employeeConfirmationCodeNotificationsProcessor.process(event);
    }
}
