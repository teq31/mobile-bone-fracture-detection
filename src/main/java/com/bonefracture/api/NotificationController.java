package com.bonefracture.api;

import com.bonefracture.api.dto.NotificationDtos.NotificationResponse;
import com.bonefracture.booking.NotificationInboxService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Notifications of the logged-in user: GET /api/notifications, POST /api/notifications/{id}/read.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationInboxService inboxService;

    public NotificationController(NotificationInboxService inboxService) {
        this.inboxService = inboxService;
    }

    @GetMapping
    public List<NotificationResponse> myNotifications(@AuthenticationPrincipal Jwt jwt) {
        return inboxService.findForUser(userId(jwt)).stream().map(NotificationResponse::from).toList();
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markAsRead(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return NotificationResponse.from(inboxService.markAsRead(id, userId(jwt)));
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
