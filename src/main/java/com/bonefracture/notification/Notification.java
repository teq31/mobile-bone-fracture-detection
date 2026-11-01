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
        this.type = type;
        this.recipient = recipient;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public NotificationType getType() {
        return type;
    }

    public User getRecipient() {
        return recipient;
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
