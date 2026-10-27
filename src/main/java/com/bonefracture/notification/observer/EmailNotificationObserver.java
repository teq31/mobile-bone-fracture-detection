package com.bonefracture.notification.observer;

import com.bonefracture.notification.Notification;

/**
 * OBSERVER pattern: Concrete Observer.
 * Sends an e-mail through SendGrid (account and booking e-mails).
 */
public class EmailNotificationObserver implements NotificationObserver {

    private String sendGridApiKey;
    private String senderAddress;

    @Override
    public void update(Notification notification) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
