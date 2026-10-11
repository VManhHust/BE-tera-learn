package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationListResponse {
    private List<NotificationItemResponse> items;
    private long unreadCount;
    private boolean hasMore;
}
