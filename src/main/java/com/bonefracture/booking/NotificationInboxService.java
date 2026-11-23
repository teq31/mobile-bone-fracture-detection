package com.bonefracture.booking;

import com.bonefracture.persistence.NotificationEntity;

import java.util.List;

/**
 * SOA: the notifications a user sees in the app.
 */
public interface NotificationInboxService {

    /** Newest first. */
    List<NotificationEntity> findForUser(Long userId);

    /** Only the recipient can mark a notification as read. */
    NotificationEntity markAsRead(Long notificationId, Long userId);
}
