package vn.tera.learn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tera.learn.entity.UserNotification;
import vn.tera.learn.entity.enums.NotificationType;

import java.time.Instant;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    @Query("SELECT notification FROM UserNotification notification "
            + "WHERE notification.id = :notificationId AND notification.user.id = :userId")
    Optional<UserNotification> findOwned(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId
    );

    Optional<UserNotification> findByUserIdAndDedupeKey(Long userId, String dedupeKey);

    @Query("""
            SELECT notification
            FROM UserNotification notification
            WHERE notification.user.id = :userId
              AND (:unreadOnly = FALSE OR notification.readAt IS NULL)
              AND (notification.expiresAt IS NULL OR notification.expiresAt > :now)
            ORDER BY
              CASE WHEN notification.readAt IS NULL THEN 0 ELSE 1 END,
              notification.createdAt DESC,
              notification.id DESC
            """)
    Page<UserNotification> findActive(
            @Param("userId") Long userId,
            @Param("unreadOnly") boolean unreadOnly,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("""
            SELECT notification
            FROM UserNotification notification
            WHERE notification.user.id = :userId
              AND notification.createdAt >= :cutoff
              AND (
                :unreadOnly = FALSE
                OR (
                  notification.readAt IS NULL
                  AND (notification.expiresAt IS NULL OR notification.expiresAt > :now)
                )
              )
            ORDER BY
              CASE
                WHEN notification.readAt IS NULL
                  AND (notification.expiresAt IS NULL OR notification.expiresAt > :now)
                THEN 0 ELSE 1
              END,
              notification.createdAt DESC,
              notification.id DESC
            """)
    Page<UserNotification> findRecentHistory(
            @Param("userId") Long userId,
            @Param("unreadOnly") boolean unreadOnly,
            @Param("now") Instant now,
            @Param("cutoff") Instant cutoff,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(notification)
            FROM UserNotification notification
            WHERE notification.user.id = :userId
              AND notification.readAt IS NULL
              AND (notification.expiresAt IS NULL OR notification.expiresAt > :now)
            """)
    long countActiveUnread(@Param("userId") Long userId, @Param("now") Instant now);

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE UserNotification notification
            SET notification.expiresAt = :now, notification.updatedAt = :now
            WHERE notification.user.id = :userId
              AND notification.type = :type
              AND (notification.expiresAt IS NULL OR notification.expiresAt > :now)
            """)
    void expireActiveByType(
            @Param("userId") Long userId,
            @Param("type") NotificationType type,
            @Param("now") Instant now
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE UserNotification notification
            SET notification.readAt = :now, notification.updatedAt = :now
            WHERE notification.user.id = :userId
              AND notification.readAt IS NULL
              AND (notification.expiresAt IS NULL OR notification.expiresAt > :now)
            """)
    int markAllActiveAsRead(@Param("userId") Long userId, @Param("now") Instant now);
}
