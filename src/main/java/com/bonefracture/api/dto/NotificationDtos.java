package com.bonefracture.api.dto;

import com.bonefracture.notification.NotificationType;
import com.bonefracture.persistence.NotificationEntity;

import java.time.LocalDateTime;

/**
 * Response bodies of /api/notifications.
 */
public final class NotificationDtos {

    private NotificationDtos() {
    }

    public record NotificationResponse(Long id, NotificationType type, String message, LocalDateTime createdAt,
                                       boolean read) {

        public static NotificationResponse from(NotificationEntity notification) {
            return new NotificationResponse(notification.getId(), notification.getType(), notification.getMessage(),
                    notification.getCreatedAt(), notification.isRead());
        }
    }
}
