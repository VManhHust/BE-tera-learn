package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationUnreadCountResponse {
    private long unreadCount;
}
