package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.*;
import vn.tera.learn.entity.*;
import vn.tera.learn.entity.enums.*;
import vn.tera.learn.repository.*;
import vn.tera.learn.service.FocusedLearningService;
import java.time.Instant;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FocusedLearningServiceImpl implements FocusedLearningService {
    private final LearningSessionRepository sessions;
    private final LessonSectionQuestionRepository questions;
    private final LessonQuestionOptionRepository options;
    private final LearningSessionAnswerRepository answers;
    private final FocusedChoiceRepository choices;
    private final UserLessonProgressRepository progress;

    public FocusedLearningServiceImpl(LearningSessionRepository sessions, LessonSectionQuestionRepository questions,
            LessonQuestionOptionRepository options, LearningSessionAnswerRepository answers,
            FocusedChoiceRepository choices, UserLessonProgressRepository progress) {
        this.sessions = sessions;
        this.questions = questions;
        this.options = options;
        this.answers = answers;
        this.choices = choices;
        this.progress = progress;
    }

    @Override
    public FocusedSessionResponse getSession(Long userId, Long sessionId) {
        LearningSession session = requireSession(userId, sessionId);
        expireIfNeeded(session);
        if (session.getStatus() == LearningSessionStatus.IN_PROGRESS) resumeClock(session);
        return response(session);
    }

    @Override
    public FocusedSessionResponse pause(Long userId, Long sessionId) {
        LearningSession session = requireSession(userId, sessionId);
        expireIfNeeded(session);
        if (session.getStatus() == LearningSessionStatus.IN_PROGRESS) pauseClock(session);
        return response(session);
    }

    @Override
    public FocusedSessionResponse saveChoice(Long userId, Long sessionId, Long questionId, SaveFocusedChoiceRequest request) {
        LearningSession session = requireSession(userId, sessionId);
        expireIfNeeded(session);
        // Late writes return the authoritative result without accepting the late choice.
        if (session.getStatus() == LearningSessionStatus.COMPLETED) return response(session);
        LessonSectionQuestion question = topicQuestions(session).stream().filter(q -> q.getId().equals(questionId))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy câu hỏi"));
        Set<Long> selected = new LinkedHashSet<>(request.getOptionIds());
        Set<Long> valid = options.findAllByQuestionIdOrderBySortOrderAsc(questionId).stream()
                .map(LessonQuestionOption::getId).collect(Collectors.toSet());
        if (!valid.containsAll(selected) || (question.getQuestionType() == LessonQuestionType.SINGLE_CHOICE && selected.size() > 1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phương án không hợp lệ");
        }
        choices.replace(sessionId, questionId, selected);
        return response(session);
    }

    @Override
    public FocusedSessionResponse finish(Long userId, Long sessionId) {
        LearningSession session = requireSession(userId, sessionId);
        expireIfNeeded(session);
        if (session.getStatus() != LearningSessionStatus.COMPLETED) finalizeSession(session, false);
        return response(session);
    }

    @Override
    public FocusedSessionResponse restart(Long userId, Long sessionId) {
        LearningSession session = requireSession(userId, sessionId);
        expireIfNeeded(session);
        if (session.getStatus() != LearningSessionStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hãy nộp phiên hiện tại trước khi làm lại");
        }
        choices.deleteBySession(sessionId);
        answers.deleteAllBySessionId(sessionId);
        session.setStatus(LearningSessionStatus.IN_PROGRESS);
        session.setCompletedAt(null);
        initializeClock(session, true);
        sessions.saveAndFlush(session);
        // Other modes' submitted work remains intact.
        boolean hasOtherWork = sessions.findAllByUserIdAndLessonIdOrderBySectionSortOrderAsc(userId, session.getLesson().getId())
                .stream().filter(s -> !s.getId().equals(sessionId)).anyMatch(s -> answers.findAllBySessionId(s.getId())
                        .stream().anyMatch(a -> !a.getSelectedOptions().isEmpty()));
        if (!hasOtherWork) {
            var p = progress.findById(new UserLessonId(userId, session.getLesson().getId())).orElse(null);
            if (p != null) {
                p.setStatus(LessonProgressStatus.NOT_STARTED);
                p.setCurrentUnit((short) 0);
                p.setStartedAt(null);
                p.setCompletedAt(null);
                p.setUpdatedAt(Instant.now());
                progress.save(p);
            }
        }
        return response(session);
    }

    private LearningSession requireSession(Long userId, Long sessionId) {
        LearningSession session = sessions.findOwnedForUpdate(sessionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên học"));
        if (!"FOCUSED".equals(session.getMode().getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phiên này không thuộc thử thách tập trung");
        }
        if (session.getStatus() == LearningSessionStatus.ABANDONED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phiên học đã đóng");
        }
        if (session.getFocusedDurationSeconds() == null || session.getFocusedRemainingSeconds() == null) {
            initializeClock(session, false);
            sessions.saveAndFlush(session);
        } else if (session.getFocusedResumedAt() != null && session.getExpiresAt() == null) {
            session.setExpiresAt(session.getFocusedResumedAt().plusSeconds(session.getFocusedRemainingSeconds()));
            sessions.saveAndFlush(session);
        } else if (session.getFocusedResumedAt() == null && session.getExpiresAt() != null) {
            session.setExpiresAt(null);
            sessions.saveAndFlush(session);
        }
        return session;
    }

    private void initializeClock(LearningSession session, boolean running) {
        int minutes = session.getLesson().getFocusedTimeLimitMinutes();
        if (minutes < 1 || minutes > 1440) throw new ResponseStatusException(HttpStatus.CONFLICT, "Chủ đề chưa có thời lượng hợp lệ");
        Instant now = Instant.now();
        session.setStartedAt(now);
        session.setFocusedDurationSeconds(minutes * 60);
        session.setFocusedRemainingSeconds(minutes * 60);
        session.setFocusedResumedAt(running ? now : null);
        session.setFocusedTimedOut(false);
        session.setExpiresAt(running ? now.plusSeconds(minutes * 60L) : null);
    }

    private void resumeClock(LearningSession session) {
        if (session.getFocusedResumedAt() != null) return;
        int remaining = session.getFocusedRemainingSeconds();
        if (remaining <= 0) {
            finalizeSession(session, true);
            return;
        }
        Instant now = Instant.now();
        session.setFocusedResumedAt(now);
        session.setExpiresAt(now.plusSeconds(remaining));
        sessions.saveAndFlush(session);
    }

    private void pauseClock(LearningSession session) {
        if (session.getFocusedResumedAt() == null) return;
        session.setFocusedRemainingSeconds((int) remainingAt(session, Instant.now()));
        session.setFocusedResumedAt(null);
        session.setExpiresAt(null);
        sessions.saveAndFlush(session);
    }

    private long remainingAt(LearningSession session, Instant now) {
        if (session.getStatus() == LearningSessionStatus.COMPLETED) return 0;
        if (session.getFocusedResumedAt() == null || session.getExpiresAt() == null) {
            return Math.max(0, session.getFocusedRemainingSeconds());
        }
        return Math.max(0, (Duration.between(now, session.getExpiresAt()).toMillis() + 999) / 1000);
    }

    private List<LessonSectionQuestion> topicQuestions(LearningSession session) {
        return questions.findAllBySectionLessonIdAndActiveTrueOrderBySectionSortOrderAscSortOrderAsc(session.getLesson().getId());
    }

    private void expireIfNeeded(LearningSession session) {
        if (session.getStatus() != LearningSessionStatus.IN_PROGRESS) return;
        boolean expiredWhileRunning = session.getFocusedResumedAt() != null
                && session.getExpiresAt() != null
                && !Instant.now().isBefore(session.getExpiresAt());
        boolean expiredWhilePaused = session.getFocusedResumedAt() == null
                && session.getFocusedRemainingSeconds() <= 0;
        if (expiredWhileRunning || expiredWhilePaused) finalizeSession(session, true);
    }

    private void finalizeSession(LearningSession session, boolean timedOut) {
        List<LessonSectionQuestion> topic = topicQuestions(session);
        if (topic.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Chủ đề chưa có câu hỏi");
        Map<Long, Set<Long>> selected = choices.findBySession(session.getId());
        Instant now = Instant.now();
        Instant submitted = timedOut && session.getExpiresAt() != null ? session.getExpiresAt() : now;
        for (LessonSectionQuestion question : topic) {
            var available = options.findAllByQuestionIdOrderBySortOrderAsc(question.getId());
            Set<Long> ids = selected.getOrDefault(question.getId(), Set.of());
            Set<Long> correctIds = available.stream().filter(LessonQuestionOption::isCorrect)
                    .map(LessonQuestionOption::getId).collect(Collectors.toSet());
            LearningSessionAnswer answer = answers.findBySessionIdAndQuestionId(session.getId(), question.getId())
                    .orElseGet(LearningSessionAnswer::new);
            answer.setSession(session);
            answer.setQuestion(question);
            answer.setSubmittedAt(submitted);
            answer.setSelectedOptions(available.stream().filter(o -> ids.contains(o.getId()))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            answer.setCorrect(!ids.isEmpty() && ids.equals(correctIds));
            answers.save(answer);
        }
        session.setStatus(LearningSessionStatus.COMPLETED);
        session.setCompletedAt(submitted);
        session.setFocusedRemainingSeconds(0);
        session.setFocusedResumedAt(null);
        session.setFocusedTimedOut(timedOut);
        session.setExpiresAt(null);
        sessions.saveAndFlush(session);
        // Merely opening or saving a draft is not counted as starting the topic.
        if (!selected.isEmpty()) {
            UserLessonId id = new UserLessonId(session.getUser().getId(), session.getLesson().getId());
            UserLessonProgress p = progress.findById(id).orElseGet(UserLessonProgress::new);
            p.setId(id);
            p.setUser(session.getUser());
            p.setLesson(session.getLesson());
            if (p.getStartedAt() == null) p.setStartedAt(submitted);
            p.setCurrentUnit(session.getLesson().getTotalUnits());
            p.setStatus(LessonProgressStatus.COMPLETED);
            p.setCompletedAt(submitted);
            p.setUpdatedAt(now);
            progress.save(p);
        }
    }

    private FocusedSessionResponse response(LearningSession session) {
        boolean complete = session.getStatus() == LearningSessionStatus.COMPLETED;
        var topic = topicQuestions(session);
        var drafts = choices.findBySession(session.getId());
        Map<Long, LearningSessionAnswer> graded = new HashMap<>();
        if (complete) answers.findAllBySessionId(session.getId()).forEach(a -> graded.put(a.getQuestion().getId(), a));
        FocusedSessionResponse result = new FocusedSessionResponse();
        result.sessionId = session.getId();
        result.lessonId = session.getLesson().getId();
        result.lessonTitle = session.getLesson().getTitle();
        result.status = session.getStatus().name();
        result.serverNow = Instant.now();
        result.expiresAt = session.getExpiresAt();
        result.durationSeconds = session.getFocusedDurationSeconds();
        result.remainingSeconds = complete ? 0 : remainingAt(session, result.serverNow);
        result.timedOut = session.isFocusedTimedOut();
        result.answeredCount = (int) topic.stream().filter(q -> !drafts.getOrDefault(q.getId(), Set.of()).isEmpty()).count();
        result.correctCount = (int) graded.values().stream().filter(LearningSessionAnswer::isCorrect).count();
        result.questions = topic.stream().map(question -> {
            FocusedSessionResponse.Question item = new FocusedSessionResponse.Question();
            item.id = question.getId();
            item.sectionId = question.getSection().getId();
            item.sectionNumber = question.getSection().getSortOrder();
            item.sectionTitle = question.getSection().getTitle();
            item.prompt = question.getPrompt();
            item.questionType = question.getQuestionType().name();
            var available = options.findAllByQuestionIdOrderBySortOrderAsc(question.getId());
            item.options = available.stream().map(o -> new ChillLearningSessionResponse.Option(o.getId(), o.getLabel(), o.getContent())).toList();
            item.selectedOptionIds = new ArrayList<>(drafts.getOrDefault(question.getId(), Set.of()));
            if (complete && graded.containsKey(question.getId())) {
                var answer = graded.get(question.getId());
                ChillAnswerResponse review = new ChillAnswerResponse();
                review.setQuestionId(question.getId());
                review.setCorrect(answer.isCorrect());
                review.setSelectedOptionIds(answer.getSelectedOptions().stream().map(LessonQuestionOption::getId).toList());
                review.setCorrectOptionIds(available.stream().filter(LessonQuestionOption::isCorrect).map(LessonQuestionOption::getId).toList());
                review.setExplanation(question.getExplanation());
                review.setYoutubeTitle(question.getYoutubeTitle());
                review.setYoutubeUrl(question.getYoutubeUrl());
                review.setSubmittedAt(answer.getSubmittedAt());
                review.setAnsweredCount(result.answeredCount);
                review.setTotalQuestions(topic.size());
                review.setSectionCompleted(true);
                item.answer = review;
            }
            return item;
        }).toList();
        return result;
    }
}
