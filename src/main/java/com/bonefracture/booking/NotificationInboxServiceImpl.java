package com.bonefracture.booking;

import com.bonefracture.persistence.NotificationEntity;
import com.bonefracture.persistence.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SOA: implementation of NotificationInboxService.
 */
@Service
@Transactional
public class NotificationInboxServiceImpl implements NotificationInboxService {

    private final NotificationRepository notificationRepository;

    public NotificationInboxServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationEntity> findForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public NotificationEntity markAsRead(Long notificationId, Long userId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new NotAllowedException("This notification belongs to another user");
        }
        notification.markAsRead();
        return notification;
    }
}
