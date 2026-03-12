package org.fcs.notifications.microservice.controllers;

import org.fcs.notifications.microservice.dtos.NotificationDto;
import org.fcs.notifications.microservice.exceptions.NotificationNotFoundException;
import org.fcs.notifications.microservice.models.EntityType;
import org.fcs.notifications.microservice.models.NotificationType;
import org.fcs.notifications.microservice.services.NotificationsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationsController.class)
@Import(ApiExceptionHandler.class)
class NotificationsControllerApiTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationsService notificationsService;

    @Test
    void getNotifications_whenOk_thenReturnResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        when(notificationsService.getNotifications(userId, true)).thenReturn(List.of(notificationDto()));

        mockMvc.perform(get("/api/v1/notifications")
                        .header("userId", userId)
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.notifications[0].title").value("title"));
    }

    @Test
    void markAllAsRead_whenOk_thenReturnUpdatedCount() throws Exception {
        UUID userId = UUID.randomUUID();
        when(notificationsService.markAllNotificationsAsRead(userId)).thenReturn(3);

        mockMvc.perform(patch("/api/v1/notifications")
                        .header("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount").value(3));
    }

    @Test
    void markNotificationAsRead_whenMissing_thenReturn404() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        when(notificationsService.markNotificationAsRead(userId, notificationId))
                .thenThrow(new NotificationNotFoundException(userId, notificationId));

        mockMvc.perform(patch("/api/v1/notifications/{notificationId}", notificationId)
                        .header("userId", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void markNotificationAsRead_whenOk_thenReturnNotification() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        when(notificationsService.markNotificationAsRead(userId, notificationId)).thenReturn(notificationDto());

        mockMvc.perform(patch("/api/v1/notifications/{notificationId}", notificationId)
                        .header("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title"));
    }

    private static NotificationDto notificationDto() {
        return new NotificationDto(
                UUID.randomUUID(),
                EntityType.APPLICATION_DISCIPLINE,
                UUID.randomUUID(),
                NotificationType.APPLICATION_STATUS_UPDATED,
                "title",
                "message",
                LocalDateTime.now(),
                false
        );
    }
}
