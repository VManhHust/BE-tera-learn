package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.entity.LearningSession;
import vn.tera.learn.entity.enums.LearningSessionStatus;
import vn.tera.learn.repository.LearningSessionRepository;
import vn.tera.learn.repository.StudyActivityRepository;
import vn.tera.learn.service.StudyActivityService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class StudyActivityServiceImpl implements StudyActivityService {
    private static final ZoneId LEARNING_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final long MAX_HEARTBEAT_GAP_SECONDS = 45;

    private final LearningSessionRepository sessionRepository;
    private final StudyActivityRepository activityRepository;

    public StudyActivityServiceImpl(
            LearningSessionRepository sessionRepository,
            StudyActivityRepository activityRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    @Transactional
    public void heartbeat(Long userId, Long sessionId) {
        LearningSession session = requireOwnedSession(userId, sessionId);
        Instant now = Instant.now();
        StudyActivityRepository.Tracker tracker = activityRepository.lockTracker(userId).orElse(null);

        if (session.getStatus() != LearningSessionStatus.IN_PROGRESS) {
            finishTracker(userId, sessionId, tracker, now);
            return;
        }

        if (tracker != null && tracker.sessionId == sessionId) {
            creditValidInterval(userId, tracker.lastSeenAt, now);
        }
        activityRepository.saveTracker(userId, sessionId, now);
    }

    @Override
    @Transactional
    public void pause(Long userId, Long sessionId) {
        requireOwnedSession(userId, sessionId);
        StudyActivityRepository.Tracker tracker = activityRepository.lockTracker(userId).orElse(null);
        finishTracker(userId, sessionId, tracker, Instant.now());
    }

    private LearningSession requireOwnedSession(Long userId, Long sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên học"));
    }

    private void finishTracker(
            Long userId,
            Long sessionId,
            StudyActivityRepository.Tracker tracker,
            Instant now
    ) {
        if (tracker == null || tracker.sessionId != sessionId) return;
        creditValidInterval(userId, tracker.lastSeenAt, now);
        activityRepository.deleteTracker(userId, sessionId);
    }

    private void creditValidInterval(Long userId, Instant start, Instant end) {
        long seconds = Duration.between(start, end).getSeconds();
        if (seconds <= 0 || seconds > MAX_HEARTBEAT_GAP_SECONDS) return;

        ZonedDateTime cursor = start.atZone(LEARNING_ZONE);
        ZonedDateTime finish = end.atZone(LEARNING_ZONE);
        while (cursor.isBefore(finish)) {
            LocalDate date = cursor.toLocalDate();
            ZonedDateTime boundary = date.plusDays(1).atStartOfDay(LEARNING_ZONE);
            ZonedDateTime intervalEnd = finish.isBefore(boundary) ? finish : boundary;
            long creditedSeconds = Duration.between(cursor, intervalEnd).getSeconds();
            activityRepository.addStudySeconds(userId, date, creditedSeconds);
            cursor = intervalEnd;
        }
    }
}
