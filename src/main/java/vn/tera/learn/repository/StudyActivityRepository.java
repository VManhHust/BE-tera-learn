package vn.tera.learn.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class StudyActivityRepository {
    private final JdbcTemplate jdbc;

    public StudyActivityRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Tracker> lockTracker(Long userId) {
        try {
            return Optional.ofNullable(jdbc.queryForObject("""
                    SELECT session_id, last_seen_at
                    FROM user_learning_activity_trackers
                    WHERE user_id = ?
                    FOR UPDATE
                    """, (result, row) -> new Tracker(
                    result.getLong("session_id"),
                    result.getTimestamp("last_seen_at").toInstant()
            ), userId));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public void saveTracker(Long userId, Long sessionId, Instant lastSeenAt) {
        jdbc.update("""
                INSERT INTO user_learning_activity_trackers(user_id, session_id, last_seen_at)
                VALUES (?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE
                SET session_id = EXCLUDED.session_id,
                    last_seen_at = EXCLUDED.last_seen_at
                """, userId, sessionId, Timestamp.from(lastSeenAt));
    }

    public void deleteTracker(Long userId, Long sessionId) {
        jdbc.update("""
                DELETE FROM user_learning_activity_trackers
                WHERE user_id = ? AND session_id = ?
                """, userId, sessionId);
    }

    public void addStudySeconds(Long userId, LocalDate date, long seconds) {
        if (seconds <= 0) return;
        jdbc.update("""
                INSERT INTO user_daily_learning_activity(user_id, activity_date, study_seconds, updated_at)
                VALUES (?, ?, ?, NOW())
                ON CONFLICT (user_id, activity_date) DO UPDATE
                SET study_seconds = user_daily_learning_activity.study_seconds + EXCLUDED.study_seconds,
                    updated_at = NOW()
                """, userId, date, seconds);
    }

    public static class Tracker {
        public final long sessionId;
        public final Instant lastSeenAt;

        public Tracker(long sessionId, Instant lastSeenAt) {
            this.sessionId = sessionId;
            this.lastSeenAt = lastSeenAt;
        }
    }
}
