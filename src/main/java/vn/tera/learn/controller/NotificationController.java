package vn.tera.learn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.tera.learn.dto.NotificationItemResponse;
import vn.tera.learn.dto.NotificationListResponse;
import vn.tera.learn.dto.NotificationUnreadCountResponse;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<NotificationListResponse> getNotifications(
            @AuthenticationPrincipal JwtClaims claims,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "false") boolean includeExpired
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(
                claims.getUserId(),
                offset,
                limit,
                unreadOnly,
                includeExpired
        ));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<NotificationUnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal JwtClaims claims
    ) {
        return ResponseEntity.ok(notificationService.getUnreadCount(claims.getUserId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationItemResponse> markAsRead(
            @AuthenticationPrincipal JwtClaims claims,
            @PathVariable Long notificationId
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(claims.getUserId(), notificationId));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<NotificationUnreadCountResponse> markAllAsRead(
            @AuthenticationPrincipal JwtClaims claims
    ) {
        return ResponseEntity.ok(notificationService.markAllAsRead(claims.getUserId()));
    }
}
