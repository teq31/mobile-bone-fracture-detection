package com.bonefracture.notification;

import com.bonefracture.notification.observer.NotificationSubject;
import com.bonefracture.user.User;

import java.util.List;

/**
 * SOA: service that records notifications and lets a user see their notifications.
 */
public interface NotificationService {

    /** Starts recording the notifications sent by the given subject (e.g. an appointment). */
    void track(NotificationSubject subject);

    List<Notification> getNotificationsFor(User user);
}
