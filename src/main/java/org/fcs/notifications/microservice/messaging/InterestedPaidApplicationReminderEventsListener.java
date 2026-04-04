package org.fcs.notifications.microservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.events.InterestedPaidApplicationReminderEvent;
import org.fcs.notifications.microservice.services.InterestedPaidApplicationReminderNotificationsProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterestedPaidApplicationReminderEventsListener {
    private final InterestedPaidApplicationReminderNotificationsProcessor interestedPaidApplicationReminderNotificationsProcessor;

    @RabbitListener(queues = "${app.rabbitmq.interested-paid-reminder.queue}")
    public void handleInterestedPaidApplicationReminder(InterestedPaidApplicationReminderEvent event) {
        log.info(
                "Получено событие-напоминание о загрузке документов: eventId={}, recipientsCount={}",
                event.eventId(),
                event.studentIds() == null ? 0 : event.studentIds().size()
        );
        interestedPaidApplicationReminderNotificationsProcessor.process(event);
    }
}
