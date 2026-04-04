package org.fcs.notifications.microservice.services;

import org.fcs.notifications.microservice.events.InterestedPaidApplicationReminderEvent;

public interface InterestedPaidApplicationReminderNotificationsProcessor {
    void process(InterestedPaidApplicationReminderEvent event);
}
