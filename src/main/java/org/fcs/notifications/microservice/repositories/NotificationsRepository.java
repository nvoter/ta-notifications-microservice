package org.fcs.notifications.microservice.repositories;

import org.fcs.notifications.microservice.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
    List<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(UUID recipientUserId);
}
