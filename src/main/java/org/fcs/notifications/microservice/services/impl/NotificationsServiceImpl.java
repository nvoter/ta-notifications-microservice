package org.fcs.notifications.microservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.dtos.NotificationDto;
import org.fcs.notifications.microservice.entities.Notification;
import org.fcs.notifications.microservice.exceptions.NotificationNotFoundException;
import org.fcs.notifications.microservice.repositories.NotificationsRepository;
import org.fcs.notifications.microservice.services.NotificationsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsServiceImpl implements NotificationsService {
    private final NotificationsRepository notificationsRepository;

    @Override
    public List<NotificationDto> getNotifications(UUID userId, boolean unreadOnly) {
        if (unreadOnly) {
            return notificationsRepository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(NotificationDto::fromEntity)
                    .toList();
        }
        return notificationsRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public NotificationDto markNotificationAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationsRepository.findByIdAndRecipientUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException(userId, notificationId));
        notification.setRead(true);
        log.info("Уведомление помечено как прочитанное: userId={}, notificationId={}", userId, notificationId);
        return NotificationDto.fromEntity(notification);
    }

    @Override
    @Transactional
    public int markAllNotificationsAsRead(UUID userId) {
        int updatedCount = notificationsRepository.markAllRead(userId);
        log.info("Все уведомления пользователя помечены как прочитанные: userId={}, updatedCount={}", userId, updatedCount);
        return updatedCount;
    }
}
