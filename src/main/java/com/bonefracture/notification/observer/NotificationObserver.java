package com.bonefracture.notification.observer;

import com.bonefracture.notification.Notification;

/**
 * OBSERVER pattern: Observer interface.
 */
public interface NotificationObserver {

    void update(Notification notification);
}
