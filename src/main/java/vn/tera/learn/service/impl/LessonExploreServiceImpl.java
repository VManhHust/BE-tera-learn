package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.LessonBookmarkResponse;
import vn.tera.learn.dto.LessonExploreResponse;
import vn.tera.learn.entity.Lesson;
import vn.tera.learn.entity.LearningSession;
import vn.tera.learn.entity.Subject;
import vn.tera.learn.entity.User;
import vn.tera.learn.entity.UserLessonBookmark;
import vn.tera.learn.entity.UserLessonId;
import vn.tera.learn.entity.UserLessonProgress;
import vn.tera.learn.entity.enums.LessonProgressStatus;
import vn.tera.learn.entity.enums.LearningSessionStatus;
import vn.tera.learn.repository.LearningSessionAnswerRepository;
import vn.tera.learn.repository.LearningSessionRepository;
import vn.tera.learn.repository.FocusedChoiceRepository;
import vn.tera.learn.repository.LessonRepository;
import vn.tera.learn.repository.SubjectRepository;
import vn.tera.learn.repository.UserLessonBookmarkRepository;
import vn.tera.learn.repository.UserLessonProgressRepository;
import vn.tera.learn.repository.UserRepository;
import vn.tera.learn.service.LessonExploreService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LessonExploreServiceImpl implements LessonExploreService {

    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final UserLessonProgressRepository progressRepository;
    private final UserLessonBookmarkRepository bookmarkRepository;
    private final LearningSessionRepository sessionRepository;
    private final LearningSessionAnswerRepository answerRepository;
    private final FocusedChoiceRepository focusedChoices;

    public LessonExploreServiceImpl(
            SubjectRepository subjectRepository,
            LessonRepository lessonRepository,
            UserRepository userRepository,
            UserLessonProgressRepository progressRepository,
            UserLessonBookmarkRepository bookmarkRepository,
            LearningSessionRepository sessionRepository,
            LearningSessionAnswerRepository answerRepository,
            FocusedChoiceRepository focusedChoices
    ) {
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.sessionRepository = sessionRepository;
        this.answerRepository = answerRepository;
        this.focusedChoices = focusedChoices;
    }

    @Override
    @Transactional(readOnly = true)
    public LessonExploreResponse getExploreData(Long userId) {
        List<Lesson> lessons = lessonRepository.findAllByPublishedTrueOrderByPublishedAtDesc();
        Map<Long, UserLessonProgress> progressByLesson = progressRepository.findAllByIdUserId(userId).stream()
                .collect(Collectors.toMap(progress -> progress.getId().getLessonId(), Function.identity()));
        Set<Long> bookmarkedLessonIds = bookmarkRepository.findAllByIdUserId(userId).stream()
                .map(bookmark -> bookmark.getId().getLessonId())
                .collect(Collectors.toSet());

        Map<String, Long> lessonCountBySubject = lessons.stream()
                .collect(Collectors.groupingBy(lesson -> lesson.getSubject().getCode(), Collectors.counting()));

        List<LessonExploreResponse.SubjectFilter> subjects = subjectRepository
                .findAllByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(subject -> new LessonExploreResponse.SubjectFilter(
                        subject.getCode(),
                        subject.getName(),
                        subject.getShortName(),
                        subject.getAccent(),
                        lessonCountBySubject.getOrDefault(subject.getCode(), 0L)
                ))
                .toList();

        List<LessonExploreResponse.LessonCard> lessonCards = lessons.stream()
                .map(lesson -> toLessonCard(
                        lesson,
                        progressByLesson.get(lesson.getId()),
                        bookmarkedLessonIds.contains(lesson.getId())
                ))
                .toList();

        int completedLessons = (int) lessonCards.stream()
                .filter(lesson -> LessonProgressStatus.COMPLETED.name().equals(lesson.getStatus()))
                .count();

        LessonExploreResponse.Summary summary = new LessonExploreResponse.Summary(
                lessonCards.size(),
                completedLessons,
                (int) subjects.stream().filter(subject -> subject.getLessonCount() > 0).count()
        );
        return new LessonExploreResponse(summary, subjects, lessonCards);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonExploreResponse getSubjectExploreData(Long userId, String subjectCode) {
        Subject subject = subjectRepository.findByCodeAndActiveTrue(subjectCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy môn học"));
        List<Lesson> lessons = lessonRepository
                .findAllBySubjectCodeAndPublishedTrueOrderByPublishedAtDesc(subjectCode);
        Set<Long> lessonIds = lessons.stream().map(Lesson::getId).collect(Collectors.toSet());
        Map<Long, UserLessonProgress> progressByLesson = progressRepository.findAllByIdUserId(userId).stream()
                .filter(progress -> lessonIds.contains(progress.getId().getLessonId()))
                .collect(Collectors.toMap(progress -> progress.getId().getLessonId(), Function.identity()));
        Set<Long> bookmarkedLessonIds = bookmarkRepository.findAllByIdUserId(userId).stream()
                .map(bookmark -> bookmark.getId().getLessonId())
                .collect(Collectors.toSet());

        List<LessonExploreResponse.LessonCard> lessonCards = lessons.stream()
                .map(lesson -> toLessonCard(
                        lesson,
                        progressByLesson.get(lesson.getId()),
                        bookmarkedLessonIds.contains(lesson.getId())
                ))
                .toList();
        int completedLessons = (int) lessonCards.stream()
                .filter(lesson -> LessonProgressStatus.COMPLETED.name().equals(lesson.getStatus()))
                .count();
        LessonExploreResponse.SubjectFilter subjectData = new LessonExploreResponse.SubjectFilter(
                subject.getCode(),
                subject.getName(),
                subject.getShortName(),
                subject.getAccent(),
                lessonCards.size()
        );
        LessonExploreResponse.Summary summary = new LessonExploreResponse.Summary(
                lessonCards.size(),
                completedLessons,
                lessonCards.isEmpty() ? 0 : 1
        );
        return new LessonExploreResponse(summary, List.of(subjectData), lessonCards);
    }

    @Override
    @Transactional
    public LessonBookmarkResponse saveBookmark(Long userId, Long lessonId) {
        UserLessonId id = new UserLessonId(userId, lessonId);
        if (!bookmarkRepository.existsById(id)) {
            UserLessonBookmark bookmark = new UserLessonBookmark();
            bookmark.setId(id);
            bookmark.setUser(requireUser(userId));
            bookmark.setLesson(requireLesson(lessonId));
            bookmark.setCreatedAt(Instant.now());
            bookmarkRepository.save(bookmark);
        }
        return new LessonBookmarkResponse(lessonId, true);
    }

    @Override
    @Transactional
    public LessonBookmarkResponse removeBookmark(Long userId, Long lessonId) {
        UserLessonId id = new UserLessonId(userId, lessonId);
        if (bookmarkRepository.existsById(id)) {
            bookmarkRepository.deleteById(id);
        }
        return new LessonBookmarkResponse(lessonId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonExploreResponse.LessonCard startLesson(Long userId, Long lessonId) {
        requireUser(userId);
        Lesson lesson = requireLesson(lessonId);
        UserLessonId id = new UserLessonId(userId, lessonId);
        UserLessonProgress progress = progressRepository.findById(id).orElse(null);
        return toLessonCard(lesson, progress, bookmarkRepository.existsById(id));
    }

    @Override
    @Transactional
    public LessonExploreResponse.LessonCard restartLesson(Long userId, Long lessonId) {
        Lesson lesson = requireLesson(lessonId);
        UserLessonId id = new UserLessonId(userId, lessonId);
        UserLessonProgress progress = progressRepository.findById(id)
                .orElseGet(() -> createProgress(requireUser(userId), lesson, id));

        Instant now = Instant.now();
        List<LearningSession> sessions = sessionRepository
                .findAllByUserIdAndLessonIdOrderBySectionSortOrderAsc(userId, lessonId);
        sessions.forEach(session -> {
            focusedChoices.deleteBySession(session.getId());
            answerRepository.deleteAllBySessionId(session.getId());
            session.setStatus(LearningSessionStatus.IN_PROGRESS);
            session.setStartedAt(now);
            session.setCompletedAt(null);
            if ("FOCUSED".equals(session.getMode().getCode())) {
                int durationSeconds = lesson.getFocusedTimeLimitMinutes() * 60;
                session.setFocusedDurationSeconds(durationSeconds);
                session.setFocusedRemainingSeconds(durationSeconds);
                session.setFocusedResumedAt(null);
                session.setFocusedTimedOut(false);
                session.setExpiresAt(null);
            } else {
                session.setExpiresAt(session.getMode().getTimeLimitMinutes() == null
                        ? null
                        : now.plus(session.getMode().getTimeLimitMinutes(), ChronoUnit.MINUTES));
            }
        });
        sessionRepository.saveAll(sessions);

        progress.setCurrentUnit((short) 0);
        progress.setStatus(LessonProgressStatus.NOT_STARTED);
        progress.setStartedAt(null);
        progress.setCompletedAt(null);
        progress.setUpdatedAt(now);
        progressRepository.save(progress);
        return toLessonCard(lesson, progress, bookmarkRepository.existsById(id));
    }

    @Override
    @Transactional
    public LessonExploreResponse.LessonCard updateProgress(Long userId, Long lessonId, short currentUnit) {
        Lesson lesson = requireLesson(lessonId);
        if (currentUnit < 0 || currentUnit > lesson.getTotalUnits()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tiến độ bài học không hợp lệ");
        }

        UserLessonId id = new UserLessonId(userId, lessonId);
        UserLessonProgress progress = progressRepository.findById(id)
                .orElseGet(() -> createProgress(requireUser(userId), lesson, id));
        Instant now = Instant.now();

        progress.setCurrentUnit(currentUnit);
        progress.setUpdatedAt(now);
        if (currentUnit == 0) {
            progress.setStatus(LessonProgressStatus.NOT_STARTED);
            progress.setStartedAt(null);
            progress.setCompletedAt(null);
        } else if (currentUnit == lesson.getTotalUnits()) {
            progress.setStatus(LessonProgressStatus.COMPLETED);
            if (progress.getStartedAt() == null) {
                progress.setStartedAt(now);
            }
            progress.setCompletedAt(now);
        } else {
            progress.setStatus(LessonProgressStatus.IN_PROGRESS);
            if (progress.getStartedAt() == null) {
                progress.setStartedAt(now);
            }
            progress.setCompletedAt(null);
        }

        progressRepository.save(progress);
        return toLessonCard(lesson, progress, bookmarkRepository.existsById(id));
    }

    private UserLessonProgress createProgress(User user, Lesson lesson, UserLessonId id) {
        UserLessonProgress progress = new UserLessonProgress();
        progress.setId(id);
        progress.setUser(user);
        progress.setLesson(lesson);
        return progress;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private Lesson requireLesson(Long lessonId) {
        return lessonRepository.findByIdAndPublishedTrue(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài học"));
    }

    private LessonExploreResponse.LessonCard toLessonCard(
            Lesson lesson,
            UserLessonProgress progress,
            boolean bookmarked
    ) {
        Subject subject = lesson.getSubject();
        short currentUnit = progress == null ? 0 : progress.getCurrentUnit();
        LessonProgressStatus status = progress == null
                ? LessonProgressStatus.NOT_STARTED
                : progress.getStatus();

        LessonExploreResponse.LessonCard response = new LessonExploreResponse.LessonCard();
        response.setId(lesson.getId());
        response.setSlug(lesson.getSlug());
        response.setSubjectCode(subject.getCode());
        response.setSubjectName(subject.getName());
        response.setSubjectShortName(subject.getShortName());
        response.setAccent(subject.getAccent());
        response.setGrade(lesson.getGrade());
        response.setTitle(lesson.getTitle());
        response.setDescription(lesson.getDescription());
        response.setThumbnailUrl(lesson.getThumbnailUrl());
        response.setDifficulty(lesson.getDifficulty().name());
        response.setProgram(lesson.getProgram().name());
        response.setTotalUnits(lesson.getTotalUnits());
        response.setCurrentUnit(currentUnit);
        response.setProgressPercent(Math.round(currentUnit * 100F / lesson.getTotalUnits()));
        response.setStatus(status.name());
        response.setBookmarked(bookmarked);
        response.setViewCount(lesson.getViewCount());
        response.setLikeCount(lesson.getLikeCount());
        response.setAverageRating(lesson.getAverageRating().doubleValue());
        response.setPublishedAt(lesson.getPublishedAt());
        return response;
    }
}
