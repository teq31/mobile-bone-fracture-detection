package com.bonefracture.notification.observer;

import com.bonefracture.notification.Notification;

import java.util.List;

/**
 * OBSERVER pattern: base Subject that keeps the list of observers.
 * Extended by Appointment; other classes that produce events
 * (e.g. Comment -> NEW_COMMENT, AnalysisResult -> ANALYSIS_FINISHED) can extend it too.
 */
public abstract class AbstractNotificationSubject implements NotificationSubject {

    protected List<NotificationObserver> observers;

    @Override
    public void attach(NotificationObserver observer) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void detach(NotificationObserver observer) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void notifyObservers(Notification notification) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
