package com.bonefracture.notification.observer;

import com.bonefracture.notification.Notification;

/**
 * OBSERVER pattern: Concrete Observer.
 * Sends a push notification through Firebase Cloud Messaging.
 */
public class PushNotificationObserver implements NotificationObserver {

    private String firebaseServerKey;

    @Override
    public void update(Notification notification) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
