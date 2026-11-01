package com.bonefracture.notification;

import com.bonefracture.notification.observer.NotificationHistoryObserver;
import com.bonefracture.notification.observer.NotificationSubject;
import com.bonefracture.user.User;

import java.util.List;

/**
 * SOA: implementation of NotificationService.
 * Attaches a NotificationHistoryObserver to every tracked subject (OBSERVER pattern).
 */
public class NotificationServiceImpl implements NotificationService {

    private final NotificationHistoryObserver history = new NotificationHistoryObserver();

    @Override
    public void track(NotificationSubject subject) {
        subject.attach(history);
    }

    @Override
    public List<Notification> getNotificationsFor(User user) {
        return history.getNotificationsFor(user);
    }
}
