package org.fcs.notifications.microservice.services;

import org.fcs.notifications.microservice.events.StudentConfirmationCodeGeneratedEvent;

public interface StudentConfirmationCodeNotificationsProcessor {
    void process(StudentConfirmationCodeGeneratedEvent event);
}
