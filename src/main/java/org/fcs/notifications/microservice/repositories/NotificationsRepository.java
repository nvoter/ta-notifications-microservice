package org.fcs.notifications.microservice.repositories;

import org.fcs.notifications.microservice.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
    List<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(UUID recipientUserId);
    Optional<Notification> findByIdAndRecipientUserId(UUID notificationId, UUID recipientUserId);
    boolean existsByEventIdAndRecipientUserId(UUID eventId, UUID recipientUserId);
    @Modifying
    @Query("""
        update Notification n set n.read = true
        where n.recipientUserId = :userId and n.read = false
    """)
    int markAllRead(@Param("userId") UUID userId);
}
