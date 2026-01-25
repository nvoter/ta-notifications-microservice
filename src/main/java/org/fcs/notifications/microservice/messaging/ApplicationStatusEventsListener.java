package org.fcs.notifications.microservice.messaging;

import lombok.RequiredArgsConstructor;
import org.fcs.notifications.microservice.events.ApplicationDisciplineStatusUpdatedEvent;
import org.fcs.notifications.microservice.services.ApplicationStatusNotificationsProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationStatusEventsListener {
    private final ApplicationStatusNotificationsProcessor applicationStatusNotificationsProcessor;

    @RabbitListener(queues = "${app.rabbitmq.application-status.queue}")
    public void handleApplicationStatusUpdated(ApplicationDisciplineStatusUpdatedEvent event) {
        applicationStatusNotificationsProcessor.process(event);
    }
}
