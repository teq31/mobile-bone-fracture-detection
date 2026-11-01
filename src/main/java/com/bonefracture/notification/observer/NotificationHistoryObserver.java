package com.bonefracture.notification.observer;

import com.bonefracture.notification.Notification;
import com.bonefracture.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER pattern: Concrete Observer.
 * Saves every notification so the user can see the list in the app.
 */
public class NotificationHistoryObserver implements NotificationObserver {

    private List<Notification> history = new ArrayList<>();

    @Override
    public void update(Notification notification) {
        history.add(notification);
    }

    public List<Notification> getNotificationsFor(User user) {
        List<Notification> result = new ArrayList<>();
        for (Notification notification : history) {
            if (notification.getRecipient() == user) {
                result.add(notification);
            }
        }
        return result;
    }
}
