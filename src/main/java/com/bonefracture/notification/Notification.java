package com.bonefracture.notification;

import com.bonefracture.user.User;

import java.time.LocalDateTime;

/**
 * The message sent from a Subject to its Observers.
 */
public class Notification {

    private Long id;
    private NotificationType type;
    private User recipient;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;

    public Notification(NotificationType type, User recipient, String message) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void markAsRead() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public NotificationType getType() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public User getRecipient() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String getMessage() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public LocalDateTime getCreatedAt() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean isRead() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
