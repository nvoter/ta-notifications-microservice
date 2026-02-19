package org.fcs.notifications.microservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.events.ApplicationDisciplineStatusUpdatedEvent;
import org.fcs.notifications.microservice.services.ApplicationStatusNotificationsProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStatusEventsListener {
    private final ApplicationStatusNotificationsProcessor applicationStatusNotificationsProcessor;

    @RabbitListener(queues = "${app.rabbitmq.application-status.queue}")
    public void handleApplicationStatusUpdated(ApplicationDisciplineStatusUpdatedEvent event) {
        log.info(
                "Получено событие обновления статуса заявки: eventId={}, applicationDisciplineId={}",
                event.eventId(),
                event.applicationDisciplineId()
        );
        applicationStatusNotificationsProcessor.process(event);
    }
}
