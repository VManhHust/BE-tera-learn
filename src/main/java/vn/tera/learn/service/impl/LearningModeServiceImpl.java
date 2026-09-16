package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.LearningModeSelectionResponse;
import vn.tera.learn.dto.LearningSessionResponse;
import vn.tera.learn.dto.StartLearningSessionRequest;
import vn.tera.learn.entity.LearningMode;
import vn.tera.learn.entity.LearningSession;
import vn.tera.learn.entity.Lesson;
import vn.tera.learn.entity.LessonSection;
import vn.tera.learn.entity.LessonSectionLearningMode;
import vn.tera.learn.entity.User;
import vn.tera.learn.entity.enums.LearningSessionStatus;
import vn.tera.learn.repository.LearningSessionRepository;
import vn.tera.learn.repository.LessonRepository;
import vn.tera.learn.repository.LessonSectionLearningModeRepository;
import vn.tera.learn.repository.LessonSectionRepository;
import vn.tera.learn.repository.UserRepository;
import vn.tera.learn.service.LearningModeService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class LearningModeServiceImpl implements LearningModeService {

    private final LessonRepository lessonRepository;
    private final LessonSectionRepository sectionRepository;
    private final LessonSectionLearningModeRepository sectionModeRepository;
    private final LearningSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public LearningModeServiceImpl(
            LessonRepository lessonRepository,
            LessonSectionRepository sectionRepository,
            LessonSectionLearningModeRepository sectionModeRepository,
            LearningSessionRepository sessionRepository,
            UserRepository userRepository
    ) {
        this.lessonRepository = lessonRepository;
        this.sectionRepository = sectionRepository;
        this.sectionModeRepository = sectionModeRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LearningModeSelectionResponse getLearningModes(Long userId, Long lessonId, Long sectionId) {
        requireUser(userId);
        Lesson lesson = requireLesson(lessonId);
        LessonSection section = requireSection(lessonId, sectionId);

        LearningModeSelectionResponse response = new LearningModeSelectionResponse();
        response.setLessonId(lesson.getId());
        response.setLessonTitle(lesson.getTitle());
        response.setSubjectName(lesson.getSubject().getName());
        response.setSubjectAccent(lesson.getSubject().getAccent());
        response.setSectionId(section.getId());
        response.setSectionTitle(section.getTitle());
        response.setSectionSummary(section.getSummary());
        response.setSectionNumber(section.getSortOrder());
        response.setTotalSections(sectionRepository.countByLessonId(lessonId));
        response.setModes(sectionModeRepository.findAllBySectionIdAndEnabledTrueOrderBySortOrderAsc(sectionId).stream()
                .map(LessonSectionLearningMode::getMode)
                .filter(LearningMode::isActive)
                .map(mode -> {
                    boolean focused = "FOCUSED".equals(mode.getCode());
                    Short configuredTimeLimit = focused
                            ? Short.valueOf((short) lesson.getFocusedTimeLimitMinutes())
                            : mode.getTimeLimitMinutes();

                    return new LearningModeSelectionResponse.Mode(
                            mode.getId(),
                            mode.getCode(),
                            mode.getName(),
                            mode.getDescription(),
                            focused
                                    ? "Thời gian toàn chủ đề: " + lesson.getFocusedTimeLimitMinutes() + " phút"
                                    : mode.getTimeCaption(),
                            mode.getFeedbackCaption(),
                            configuredTimeLimit,
                            mode.isImmediateFeedback(),
                            mode.getAccent(),
                            mode.getIllustrationKey(),
                            mode.getIllustrationUrl()
                    );
                })
                .toList());
        return response;
    }

    @Override
    @Transactional
    public LearningSessionResponse startSession(
            Long userId,
            Long lessonId,
            Long sectionId,
            StartLearningSessionRequest request
    ) {
        User user = userRepository.findForSessionStart(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        Lesson lesson = requireLesson(lessonId);
        LessonSection section = requireSection(lessonId, sectionId);
        LearningMode mode = sectionModeRepository
                .findBySectionIdAndModeIdAndEnabledTrue(sectionId, request.getModeId())
                .map(LessonSectionLearningMode::getMode)
                .filter(LearningMode::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chế độ học không khả dụng"));

        if ("FOCUSED".equals(mode.getCode())) {
            var previous = sessionRepository.findAllByUserIdAndLessonIdAndModeIdOrderBySectionSortOrderAsc(
                    userId, lessonId, mode.getId());
            if (!previous.isEmpty()) return toSessionResponse(previous.get(0));
            section = sectionRepository.findAllByLessonIdOrderBySortOrderAsc(lessonId).stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chủ đề chưa có phần học"));
        }

        LearningSession existingSession = sessionRepository
                .findByUserIdAndLessonIdAndSectionIdAndModeId(
                        userId,
                        lessonId,
                        sectionId,
                        mode.getId()
                )
                .orElse(null);
        if (existingSession != null) {
            return toSessionResponse(existingSession);
        }

        Instant now = Instant.now();
        LearningSession session = new LearningSession();
        session.setUser(user);
        session.setLesson(lesson);
        session.setSection(section);
        session.setMode(mode);
        session.setStatus(LearningSessionStatus.IN_PROGRESS);
        session.setStartedAt(now);
        if ("FOCUSED".equals(mode.getCode())) {
            int minutes = lesson.getFocusedTimeLimitMinutes();
            if (minutes < 1 || minutes > 1440) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chủ đề chưa có thời lượng hợp lệ");
            }
            session.setFocusedDurationSeconds(minutes * 60);
            session.setFocusedRemainingSeconds(minutes * 60);
            session.setFocusedResumedAt(null);
            session.setFocusedTimedOut(false);
            session.setExpiresAt(null);
        } else if (mode.getTimeLimitMinutes() != null) {
            session.setExpiresAt(now.plus(mode.getTimeLimitMinutes(), ChronoUnit.MINUTES));
        }
        sessionRepository.save(session);
        return toSessionResponse(session);
    }

    private LearningSessionResponse toSessionResponse(LearningSession session) {
        LearningSessionResponse response = new LearningSessionResponse();
        response.setSessionId(session.getId());
        response.setLessonId(session.getLesson().getId());
        response.setSectionId(session.getSection().getId());
        response.setSectionNumber(session.getSection().getSortOrder());
        response.setSectionTitle(session.getSection().getTitle());
        response.setModeId(session.getMode().getId());
        response.setModeCode(session.getMode().getCode());
        response.setModeName(session.getMode().getName());
        response.setStatus(session.getStatus().name());
        response.setStartedAt(session.getStartedAt());
        response.setExpiresAt(session.getExpiresAt());
        return response;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private Lesson requireLesson(Long lessonId) {
        return lessonRepository.findByIdAndPublishedTrue(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài học"));
    }

    private LessonSection requireSection(Long lessonId, Long sectionId) {
        return sectionRepository.findByIdAndLessonId(sectionId, lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phần học"));
    }
}
