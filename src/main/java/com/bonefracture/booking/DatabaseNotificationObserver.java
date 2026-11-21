package com.bonefracture.booking;

import com.bonefracture.notification.Notification;
import com.bonefracture.notification.observer.NotificationObserver;
import com.bonefracture.persistence.NotificationEntity;
import com.bonefracture.persistence.NotificationRepository;
import com.bonefracture.persistence.UserRepository;
import org.springframework.stereotype.Component;

/**
 * OBSERVER pattern: Concrete Observer that saves every notification in the database,
 * so the user sees it in the app (GET /api/notifications).
 */
@Component
public class DatabaseNotificationObserver implements NotificationObserver {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public DatabaseNotificationObserver(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void update(Notification notification) {
        userRepository.findByEmail(notification.getRecipient().getEmail())
                .ifPresent(recipient -> notificationRepository.save(
                        new NotificationEntity(recipient, notification.getType(), notification.getMessage())));
    }
}
