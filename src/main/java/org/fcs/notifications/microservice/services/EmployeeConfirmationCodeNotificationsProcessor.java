package org.fcs.notifications.microservice.services;

import org.fcs.notifications.microservice.events.EmployeeConfirmationCodeGeneratedEvent;

public interface EmployeeConfirmationCodeNotificationsProcessor {
    void process(EmployeeConfirmationCodeGeneratedEvent event);
}
