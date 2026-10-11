package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
public class NotificationItemResponse {
    private Long id;
    private String type;
    private String priority;
    private Map<String, Object> data;
    private String actionUrl;
    private boolean unread;
    private Instant readAt;
    private Instant createdAt;
    private Instant expiresAt;
}
