package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.ChillAnswerResponse;
import vn.tera.learn.dto.ChillLearningSessionResponse;
import vn.tera.learn.dto.SubmitChillAnswerRequest;
import vn.tera.learn.entity.LearningSession;
import vn.tera.learn.entity.LearningSessionAnswer;
import vn.tera.learn.entity.LessonSection;
import vn.tera.learn.entity.LessonQuestionOption;
import vn.tera.learn.entity.LessonSectionQuestion;
import vn.tera.learn.entity.UserLessonId;
import vn.tera.learn.entity.UserLessonProgress;
import vn.tera.learn.entity.enums.LearningSessionStatus;
import vn.tera.learn.entity.enums.LessonProgressStatus;
import vn.tera.learn.entity.enums.LessonQuestionType;
import vn.tera.learn.repository.LearningSessionAnswerRepository;
import vn.tera.learn.repository.LearningSessionRepository;
import vn.tera.learn.repository.LessonQuestionOptionRepository;
import vn.tera.learn.repository.LessonSectionQuestionRepository;
import vn.tera.learn.repository.LessonSectionRepository;
import vn.tera.learn.repository.UserLessonProgressRepository;
import vn.tera.learn.service.ChillLearningService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChillLearningServiceImpl implements ChillLearningService {

    private final LearningSessionRepository sessionRepository;
    private final LessonSectionQuestionRepository questionRepository;
    private final LessonQuestionOptionRepository optionRepository;
    private final LearningSessionAnswerRepository answerRepository;
    private final LessonSectionRepository sectionRepository;
    private final UserLessonProgressRepository progressRepository;

    public ChillLearningServiceImpl(
            LearningSessionRepository sessionRepository,
            LessonSectionQuestionRepository questionRepository,
            LessonQuestionOptionRepository optionRepository,
            LearningSessionAnswerRepository answerRepository,
            LessonSectionRepository sectionRepository,
            UserLessonProgressRepository progressRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.answerRepository = answerRepository;
        this.sectionRepository = sectionRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ChillLearningSessionResponse getSession(Long userId, Long sessionId) {
        LearningSession session = requireChillSession(userId, sessionId);
        List<LessonSectionQuestion> questions = questionRepository
                .findAllBySectionIdAndActiveTrueOrderBySortOrderAsc(session.getSection().getId());
        Map<Long, LearningSessionAnswer> answersByQuestion = new HashMap<>();
        answerRepository.findAllBySessionId(sessionId)
                .forEach(answer -> answersByQuestion.put(answer.getQuestion().getId(), answer));

        ChillLearningSessionResponse response = new ChillLearningSessionResponse();
        response.setSessionId(session.getId());
        response.setLessonId(session.getLesson().getId());
        response.setLessonTitle(session.getLesson().getTitle());
        response.setSubjectName(session.getLesson().getSubject().getName());
        response.setSubjectAccent(session.getLesson().getSubject().getAccent());
        response.setSectionId(session.getSection().getId());
        response.setSectionTitle(session.getSection().getTitle());
        response.setSectionNumber(session.getSection().getSortOrder());
        response.setModeId(session.getMode().getId());
        response.setModeCode(session.getMode().getCode());
        response.setModeName(session.getMode().getName());
        response.setStatus(session.getStatus().name());
        response.setStartedAt(session.getStartedAt());
        response.setExpiresAt(session.getExpiresAt());
        response.setAnsweredCount(answersByQuestion.size());
        response.setTotalQuestions(questions.size());
        LessonSection nextSection = sectionRepository
                .findFirstByLessonIdAndSortOrderGreaterThanOrderBySortOrderAsc(
                        session.getLesson().getId(),
                        session.getSection().getSortOrder()
                )
                .orElse(null);
        if (nextSection != null) {
            response.setNextSectionId(nextSection.getId());
            response.setNextSectionNumber(nextSection.getSortOrder());
            response.setNextSectionTitle(nextSection.getTitle());
        }
        response.setQuestions(questions.stream().map(question -> {
            ChillLearningSessionResponse.Question item = new ChillLearningSessionResponse.Question();
            item.setId(question.getId());
            item.setQuestionType(question.getQuestionType().name());
            item.setPrompt(question.getPrompt());
            item.setSortOrder(question.getSortOrder());
            item.setOptions(optionRepository.findAllByQuestionIdOrderBySortOrderAsc(question.getId()).stream()
                    .map(option -> new ChillLearningSessionResponse.Option(
                            option.getId(),
                            option.getLabel(),
                            option.getContent()
                    ))
                    .toList());
            LearningSessionAnswer answer = answersByQuestion.get(question.getId());
            if (answer != null) {
                item.setAnswer(toAnswerResponse(answer, question, questions.size(), answersByQuestion.size()));
            }
            return item;
        }).toList());
        if (nextSection == null && session.getStatus() == LearningSessionStatus.COMPLETED) {
            response.setLessonReviewSections(buildLessonReview(userId, session));
        }
        return response;
    }

    @Override
    @Transactional
    public ChillLearningSessionResponse restartSession(Long userId, Long sessionId) {
        LearningSession session = requireChillSession(userId, sessionId);
        answerRepository.deleteAllBySessionId(sessionId);

        Instant now = Instant.now();
        session.setStatus(LearningSessionStatus.IN_PROGRESS);
        session.setStartedAt(now);
        session.setCompletedAt(null);
        session.setExpiresAt(session.getMode().getTimeLimitMinutes() == null
                ? null
                : now.plus(session.getMode().getTimeLimitMinutes(), ChronoUnit.MINUTES));
        sessionRepository.saveAndFlush(session);
        return getSession(userId, sessionId);
    }

    @Override
    @Transactional
    public ChillLearningSessionResponse restartLesson(Long userId, Long sessionId) {
        LearningSession currentSession = requireChillSession(userId, sessionId);
        List<LearningSession> lessonSessions = sessionRepository
                .findAllByUserIdAndLessonIdAndModeIdOrderBySectionSortOrderAsc(
                        userId,
                        currentSession.getLesson().getId(),
                        currentSession.getMode().getId()
                );
        if (lessonSessions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên học của bài học");
        }

        lessonSessions.forEach(session -> answerRepository.deleteAllBySessionId(session.getId()));
        Instant now = Instant.now();
        lessonSessions.forEach(session -> {
            session.setStatus(LearningSessionStatus.IN_PROGRESS);
            session.setStartedAt(now);
            session.setCompletedAt(null);
            session.setExpiresAt(session.getMode().getTimeLimitMinutes() == null
                    ? null
                    : now.plus(session.getMode().getTimeLimitMinutes(), ChronoUnit.MINUTES));
        });
        sessionRepository.saveAllAndFlush(lessonSessions);

        LearningSession firstSession = lessonSessions.get(0);
        UserLessonId progressId = new UserLessonId(userId, currentSession.getLesson().getId());
        UserLessonProgress progress = progressRepository.findById(progressId).orElse(null);
        if (progress != null) {
            progress.setCurrentUnit(firstSession.getSection().getSortOrder());
            progress.setStatus(LessonProgressStatus.IN_PROGRESS);
            progress.setCompletedAt(null);
            progress.setUpdatedAt(now);
            progressRepository.save(progress);
        }
        return getSession(userId, firstSession.getId());
    }

    @Override
    @Transactional
    public ChillAnswerResponse submitAnswer(
            Long userId,
            Long sessionId,
            Long questionId,
            SubmitChillAnswerRequest request
    ) {
        LearningSession session = requireChillSession(userId, sessionId);
        LessonSectionQuestion question = questionRepository
                .findByIdAndSectionIdAndActiveTrue(questionId, session.getSection().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy câu hỏi"));
        List<LessonQuestionOption> options = optionRepository.findAllByQuestionIdOrderBySortOrderAsc(questionId);
        Set<Long> requestedIds = new LinkedHashSet<>(request.getOptionIds());
        Set<Long> validIds = options.stream().map(LessonQuestionOption::getId).collect(Collectors.toSet());

        if (requestedIds.isEmpty() || !validIds.containsAll(requestedIds)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phương án đã chọn không hợp lệ");
        }
        if (question.getQuestionType() == LessonQuestionType.SINGLE_CHOICE && requestedIds.size() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Câu hỏi này chỉ được chọn một phương án");
        }

        LearningSessionAnswer existing = answerRepository.findBySessionIdAndQuestionId(sessionId, questionId).orElse(null);
        int totalQuestions = Math.toIntExact(questionRepository.countBySectionIdAndActiveTrue(session.getSection().getId()));
        if (existing != null) {
            int answeredCount = Math.toIntExact(answerRepository.countBySessionId(sessionId));
            return toAnswerResponse(existing, question, totalQuestions, answeredCount);
        }
        if (session.getStatus() != LearningSessionStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phiên học đã kết thúc");
        }

        Instant submittedAt = Instant.now();
        markLessonStarted(session, submittedAt);
        Set<Long> correctIds = options.stream()
                .filter(LessonQuestionOption::isCorrect)
                .map(LessonQuestionOption::getId)
                .collect(Collectors.toSet());
        LearningSessionAnswer answer = new LearningSessionAnswer();
        answer.setSession(session);
        answer.setQuestion(question);
        answer.setCorrect(correctIds.equals(requestedIds));
        answer.setSubmittedAt(submittedAt);
        answer.setSelectedOptions(options.stream()
                .filter(option -> requestedIds.contains(option.getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        answerRepository.saveAndFlush(answer);

        int answeredCount = Math.toIntExact(answerRepository.countBySessionId(sessionId));
        if (answeredCount >= totalQuestions) {
            completeSection(session);
        }
        return toAnswerResponse(answer, question, totalQuestions, answeredCount);
    }

    private List<ChillLearningSessionResponse.SectionReview> buildLessonReview(
            Long userId,
            LearningSession currentSession
    ) {
        return sessionRepository.findAllByUserIdAndLessonIdAndModeIdOrderBySectionSortOrderAsc(
                        userId,
                        currentSession.getLesson().getId(),
                        currentSession.getMode().getId()
                ).stream()
                .map(session -> {
                    List<LessonSectionQuestion> questions = questionRepository
                            .findAllBySectionIdAndActiveTrueOrderBySortOrderAsc(session.getSection().getId());
                    Map<Long, LearningSessionAnswer> answersByQuestion = new HashMap<>();
                    answerRepository.findAllBySessionId(session.getId())
                            .forEach(answer -> answersByQuestion.put(answer.getQuestion().getId(), answer));

                    ChillLearningSessionResponse.SectionReview review =
                            new ChillLearningSessionResponse.SectionReview();
                    review.setSectionId(session.getSection().getId());
                    review.setSectionNumber(session.getSection().getSortOrder());
                    review.setSectionTitle(session.getSection().getTitle());
                    review.setCorrectCount((int) answersByQuestion.values().stream()
                            .filter(LearningSessionAnswer::isCorrect)
                            .count());
                    review.setTotalQuestions(questions.size());
                    review.setQuestions(questions.stream().map(question -> {
                        ChillLearningSessionResponse.Question item = new ChillLearningSessionResponse.Question();
                        item.setId(question.getId());
                        item.setQuestionType(question.getQuestionType().name());
                        item.setPrompt(question.getPrompt());
                        item.setSortOrder(question.getSortOrder());
                        item.setOptions(optionRepository
                                .findAllByQuestionIdOrderBySortOrderAsc(question.getId()).stream()
                                .map(option -> new ChillLearningSessionResponse.Option(
                                        option.getId(),
                                        option.getLabel(),
                                        option.getContent()
                                ))
                                .toList());
                        LearningSessionAnswer answer = answersByQuestion.get(question.getId());
                        if (answer != null) {
                            item.setAnswer(toAnswerResponse(
                                    answer,
                                    question,
                                    questions.size(),
                                    answersByQuestion.size()
                            ));
                        }
                        return item;
                    }).toList());
                    return review;
                })
                .toList();
    }

    private ChillAnswerResponse toAnswerResponse(
            LearningSessionAnswer answer,
            LessonSectionQuestion question,
            int totalQuestions,
            int answeredCount
    ) {
        List<Long> correctOptionIds = optionRepository.findAllByQuestionIdOrderBySortOrderAsc(question.getId()).stream()
                .filter(LessonQuestionOption::isCorrect)
                .map(LessonQuestionOption::getId)
                .toList();
        ChillAnswerResponse response = new ChillAnswerResponse();
        response.setQuestionId(question.getId());
        response.setCorrect(answer.isCorrect());
        response.setSelectedOptionIds(answer.getSelectedOptions().stream().map(LessonQuestionOption::getId).toList());
        response.setCorrectOptionIds(correctOptionIds);
        response.setExplanation(question.getExplanation());
        response.setYoutubeTitle(question.getYoutubeTitle());
        response.setYoutubeUrl(question.getYoutubeUrl());
        response.setSubmittedAt(answer.getSubmittedAt());
        response.setAnsweredCount(answeredCount);
        response.setTotalQuestions(totalQuestions);
        response.setSectionCompleted(answeredCount >= totalQuestions);
        return response;
    }

    private void markLessonStarted(LearningSession session, Instant now) {
        UserLessonId progressId = new UserLessonId(session.getUser().getId(), session.getLesson().getId());
        UserLessonProgress progress = progressRepository.findById(progressId).orElse(null);
        if (progress != null && progress.getStatus() == LessonProgressStatus.COMPLETED) {
            return;
        }
        if (progress == null) {
            progress = new UserLessonProgress();
            progress.setId(progressId);
            progress.setUser(session.getUser());
            progress.setLesson(session.getLesson());
        }

        if (progress.getCurrentUnit() < session.getSection().getSortOrder()) {
            progress.setCurrentUnit(session.getSection().getSortOrder());
        }
        progress.setStatus(LessonProgressStatus.IN_PROGRESS);
        if (progress.getStartedAt() == null) {
            progress.setStartedAt(now);
        }
        progress.setCompletedAt(null);
        progress.setUpdatedAt(now);
        progressRepository.save(progress);
    }

    private void completeSection(LearningSession session) {
        Instant now = Instant.now();
        session.setStatus(LearningSessionStatus.COMPLETED);
        session.setCompletedAt(now);
        sessionRepository.save(session);

        UserLessonId progressId = new UserLessonId(session.getUser().getId(), session.getLesson().getId());
        UserLessonProgress progress = progressRepository.findById(progressId).orElse(null);
        if (progress == null) {
            return;
        }

        short sectionNumber = session.getSection().getSortOrder();
        short totalUnits = session.getLesson().getTotalUnits();
        if (sectionNumber >= totalUnits) {
            progress.setCurrentUnit(totalUnits);
            progress.setStatus(LessonProgressStatus.COMPLETED);
            progress.setCompletedAt(now);
        } else {
            progress.setCurrentUnit((short) (sectionNumber + 1));
            progress.setStatus(LessonProgressStatus.IN_PROGRESS);
        }
        progress.setUpdatedAt(now);
        progressRepository.save(progress);
    }

    private LearningSession requireChillSession(Long userId, Long sessionId) {
        LearningSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiên học"));
        if (!session.getMode().isImmediateFeedback()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phiên học không thuộc chế độ vừa học vừa chill");
        }
        return session;
    }
}
