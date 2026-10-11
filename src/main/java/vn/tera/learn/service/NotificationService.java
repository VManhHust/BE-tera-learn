package vn.tera.learn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.common.pagination.OffsetPageRequest;
import vn.tera.learn.dto.NotificationItemResponse;
import vn.tera.learn.dto.NotificationListResponse;
import vn.tera.learn.dto.NotificationUnreadCountResponse;
import vn.tera.learn.dto.StreakResponse;
import vn.tera.learn.entity.Lesson;
import vn.tera.learn.entity.User;
import vn.tera.learn.entity.UserLessonProgress;
import vn.tera.learn.entity.UserNotification;
import vn.tera.learn.entity.enums.LessonProgressStatus;
import vn.tera.learn.entity.enums.NotificationPriority;
import vn.tera.learn.entity.enums.NotificationType;
import vn.tera.learn.repository.UserLessonProgressRepository;
import vn.tera.learn.repository.UserNotificationRepository;
import vn.tera.learn.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationService {
    private static final ZoneId LEARNING_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int MAX_PAGE_SIZE = 50;
    private static final int HISTORY_DAYS = 30;

    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserLessonProgressRepository progressRepository;
    private final StreakService streakService;

    public NotificationService(
            UserNotificationRepository notificationRepository,
            UserRepository userRepository,
            UserLessonProgressRepository progressRepository,
            StreakService streakService
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.streakService = streakService;
    }

    public NotificationListResponse getNotifications(
            Long userId,
            int offset,
            int limit,
            boolean unreadOnly,
            boolean includeExpired
    ) {
        ensureCurrentReminders(userId);

        int safeLimit = Math.max(1, Math.min(limit, MAX_PAGE_SIZE));
        int safeOffset = Math.max(0, offset);
        Instant now = Instant.now();
        OffsetPageRequest pageRequest = new OffsetPageRequest(safeOffset, safeLimit);
        Page<UserNotification> page = includeExpired
                ? notificationRepository.findRecentHistory(
                        userId,
                        unreadOnly,
                        now,
                        now.minus(HISTORY_DAYS, ChronoUnit.DAYS),
                        pageRequest
                )
                : notificationRepository.findActive(
                        userId,
                        unreadOnly,
                        now,
                        pageRequest
                );

        List<NotificationItemResponse> items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        long unreadCount = notificationRepository.countActiveUnread(userId, now);
        return new NotificationListResponse(
                items,
                unreadCount,
                (long) safeOffset + items.size() < page.getTotalElements()
        );
    }

    public NotificationUnreadCountResponse getUnreadCount(Long userId) {
        ensureCurrentReminders(userId);
        return new NotificationUnreadCountResponse(
                notificationRepository.countActiveUnread(userId, Instant.now())
        );
    }

    public NotificationItemResponse markAsRead(Long userId, Long notificationId) {
        UserNotification notification = notificationRepository.findOwned(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy thông báo"
                ));

        if (notification.getReadAt() == null) {
            Instant now = Instant.now();
            notification.setReadAt(now);
            notification.setUpdatedAt(now);
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    public NotificationUnreadCountResponse markAllAsRead(Long userId) {
        notificationRepository.markAllActiveAsRead(userId, Instant.now());
        return new NotificationUnreadCountResponse(0);
    }

    private void ensureCurrentReminders(Long userId) {
        User user = userRepository.findForNotificationRefresh(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy người dùng"
                ));
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(LEARNING_ZONE);
        Instant endOfDay = today.plusDays(1).atStartOfDay(LEARNING_ZONE).toInstant();

        refreshContinueLessonReminder(user, today, now, endOfDay);
        refreshStreakReminder(user, today, now, endOfDay);
        refreshPlusUpgradeReminder(user, today, now);
    }

    private void refreshContinueLessonReminder(User user, LocalDate today, Instant now, Instant endOfDay) {
        NotificationType type = NotificationType.CONTINUE_LESSON;
        notificationRepository.expireActiveByType(user.getId(), type, now);

        List<UserLessonProgress> candidates = progressRepository.findRecentLearningProgress(
                user.getId(),
                LessonProgressStatus.IN_PROGRESS,
                now.minus(HISTORY_DAYS, ChronoUnit.DAYS),
                PageRequest.of(0, 1)
        );
        if (candidates.isEmpty()) {
            return;
        }

        UserLessonProgress progress = candidates.getFirst();
        Lesson lesson = progress.getLesson();
        long lessonId = lesson.getId();
        int currentUnit = progress.getCurrentUnit();
        int totalUnits = Math.max(1, lesson.getTotalUnits());
        int completionPercentage = Math.min(99, Math.round(currentUnit * 100F / totalUnits));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("lessonId", lessonId);
        data.put("lessonTitle", lesson.getTitle());
        data.put("subjectName", lesson.getSubject().getName());
        data.put("currentUnit", currentUnit);
        data.put("totalUnits", totalUnits);
        data.put("completionPercentage", completionPercentage);

        upsert(
                user,
                type,
                NotificationPriority.NORMAL,
                data,
                "/lessons/" + lessonId,
                type + ":" + lessonId + ":" + today,
                now,
                endOfDay
        );
    }

    private void refreshStreakReminder(User user, LocalDate today, Instant now, Instant endOfDay) {
        NotificationType type = NotificationType.STREAK_REMINDER;
        notificationRepository.expireActiveByType(user.getId(), type, now);

        StreakResponse streak = streakService.getStatus(user.getId());
        if (streak.isCheckedInToday()) {
            return;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentStreak", streak.getCurrentStreak());
        data.put("totalCheckIns", streak.getTotalCheckIns());

        upsert(
                user,
                type,
                streak.getCurrentStreak() > 0 ? NotificationPriority.HIGH : NotificationPriority.NORMAL,
                data,
                "/dashboard",
                type + ":" + today,
                now,
                endOfDay
        );
    }

    private void refreshPlusUpgradeReminder(User user, LocalDate today, Instant now) {
        NotificationType type = NotificationType.PLUS_UPGRADE;
        notificationRepository.expireActiveByType(user.getId(), type, now);

        boolean plusStarted = user.getPlusStartsAt() == null || !user.getPlusStartsAt().isAfter(now);
        boolean plusActive = plusStarted
                && user.getPlusExpiresAt() != null
                && user.getPlusExpiresAt().isAfter(now);
        if (plusActive) {
            return;
        }

        WeekFields weekFields = WeekFields.ISO;
        int weekBasedYear = today.get(weekFields.weekBasedYear());
        int weekOfYear = today.get(weekFields.weekOfWeekBasedYear());
        int daysUntilNextMonday = 8 - today.getDayOfWeek().getValue();
        Instant expiresAt = today.plusDays(daysUntilNextMonday)
                .atStartOfDay(LEARNING_ZONE)
                .toInstant();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("campaign", "WEEKLY_PLUS_DISCOVERY");

        upsert(
                user,
                type,
                NotificationPriority.NORMAL,
                data,
                "/dashboard",
                type + ":" + weekBasedYear + "-W" + weekOfYear,
                now,
                expiresAt
        );
    }

    private void upsert(
            User user,
            NotificationType type,
            NotificationPriority priority,
            Map<String, Object> data,
            String actionUrl,
            String dedupeKey,
            Instant now,
            Instant expiresAt
    ) {
        UserNotification notification = notificationRepository
                .findByUserIdAndDedupeKey(user.getId(), dedupeKey)
                .orElseGet(() -> {
                    UserNotification created = new UserNotification();
                    created.setUser(user);
                    created.setDedupeKey(dedupeKey);
                    created.setCreatedAt(now);
                    return created;
                });

        notification.setType(type);
        notification.setPriority(priority);
        notification.setData(new LinkedHashMap<>(data));
        notification.setActionUrl(actionUrl);
        notification.setExpiresAt(expiresAt);
        notification.setUpdatedAt(now);
        notificationRepository.save(notification);
    }

    private NotificationItemResponse toResponse(UserNotification notification) {
        Instant now = Instant.now();
        boolean unread = notification.getReadAt() == null
                && (notification.getExpiresAt() == null
                || notification.getExpiresAt().isAfter(now));
        return new NotificationItemResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getPriority().name(),
                notification.getData(),
                notification.getActionUrl(),
                unread,
                notification.getReadAt(),
                notification.getCreatedAt(),
                notification.getExpiresAt()
        );
    }

}
