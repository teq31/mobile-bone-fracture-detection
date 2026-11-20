package com.bonefracture.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /** Newest first, as shown in the app. */
    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
