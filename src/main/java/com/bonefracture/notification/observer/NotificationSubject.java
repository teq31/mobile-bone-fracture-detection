package com.bonefracture.notification.observer;

import com.bonefracture.notification.Notification;

/**
 * OBSERVER pattern: Subject interface.
 */
public interface NotificationSubject {

    void attach(NotificationObserver observer);

    void detach(NotificationObserver observer);

    void notifyObservers(Notification notification);
}
