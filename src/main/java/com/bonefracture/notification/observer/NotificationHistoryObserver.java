package com.bonefracture.notification.observer;

import com.bonefracture.notification.Notification;
import com.bonefracture.user.User;

import java.util.List;

/**
 * OBSERVER pattern: Concrete Observer.
 * Saves every notification so the user can see the list in the app.
 */
public class NotificationHistoryObserver implements NotificationObserver {

    private List<Notification> history;

    @Override
    public void update(Notification notification) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<Notification> getNotificationsFor(User user) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
