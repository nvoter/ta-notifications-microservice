package org.fcs.notifications.microservice.services;

import org.fcs.notifications.microservice.events.ApplicationDisciplineStatusUpdatedEvent;

public interface ApplicationStatusNotificationsProcessor {
    void process(ApplicationDisciplineStatusUpdatedEvent event);
}
