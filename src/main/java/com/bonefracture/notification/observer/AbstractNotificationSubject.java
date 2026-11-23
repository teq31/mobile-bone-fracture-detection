package com.bonefracture.notification.observer;

import com.bonefracture.notification.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER pattern: base Subject that keeps the list of observers.
 * Extended by Appointment; other classes that produce events
 * (e.g. Comment -> NEW_COMMENT, AnalysisResult -> ANALYSIS_FINISHED) can extend it too.
 */
public abstract class AbstractNotificationSubject implements NotificationSubject {

    protected List<NotificationObserver> observers = new ArrayList<>();

    @Override
    public void attach(NotificationObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detach(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Notification notification) {
        for (NotificationObserver observer : observers) {
            observer.update(notification);
        }
    }
}
