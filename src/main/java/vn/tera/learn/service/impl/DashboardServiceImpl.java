package vn.tera.learn.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tera.learn.dto.DashboardResponse;
import vn.tera.learn.repository.DashboardRepository;
import vn.tera.learn.service.DashboardService;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DashboardServiceImpl implements DashboardService {
    private static final ZoneId LEARNING_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final long XP_PER_SUBMITTED_ANSWER = 10;
    private static final long XP_PER_CORRECT_ANSWER = 10;
    private static final long XP_PER_COMPLETED_LESSON = 100;
    private static final long XP_PER_LEVEL = 500;

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        LocalDate today = LocalDate.now(LEARNING_ZONE);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(7);
        LocalDate previousWeekStart = weekStart.minusDays(7);
        Instant weekStartInstant = weekStart.atStartOfDay(LEARNING_ZONE).toInstant();
        Instant weekEndInstant = weekEnd.atStartOfDay(LEARNING_ZONE).toInstant();
        Instant previousWeekStartInstant = previousWeekStart.atStartOfDay(LEARNING_ZONE).toInstant();

        DashboardRepository.CompletionStats completion = dashboardRepository.findCompletionStats(
                userId, weekStartInstant, weekEndInstant
        );
        DashboardRepository.AnswerStats answers = dashboardRepository.findAnswerStats(
                userId, previousWeekStartInstant, weekStartInstant, weekEndInstant
        );
        Map<LocalDate, Long> activity = dashboardRepository.findActivityByDate(
                userId, previousWeekStart, weekEnd
        );

        long currentWeekSeconds = sumActivity(activity, weekStart, weekEnd);
        long previousWeekSeconds = sumActivity(activity, previousWeekStart, weekStart);
        double averageScore = score(answers.correctAnswers, answers.gradedAnswers);
        double currentWeekScore = score(answers.currentWeekCorrect, answers.currentWeekAnswers);
        double previousWeekScore = score(answers.previousWeekCorrect, answers.previousWeekAnswers);
        double scoreChange = answers.currentWeekAnswers == 0
                ? 0
                : roundOneDecimal(currentWeekScore - previousWeekScore);

        DashboardResponse response = new DashboardResponse();
        response.setOverview(buildOverview(
                completion,
                dashboardRepository.findTotalStudySeconds(userId),
                currentWeekSeconds,
                averageScore,
                scoreChange
        ));
        response.setLearner(buildLearner(completion.completedLessons, answers));
        response.setContinueLearning(dashboardRepository.findContinueLearning(userId)
                .map(this::buildContinueLearning)
                .orElse(null));
        response.setWeeklyActivity(buildWeeklyActivity(
                today, weekStart, currentWeekSeconds, previousWeekSeconds, activity
        ));
        response.setSubjectProgress(dashboardRepository.findSubjectProgress(userId).stream()
                .map(this::buildSubjectProgress)
                .toList());
        DashboardRepository.DailyChallengeProjection dailyChallenge = selectDailyChallenge(userId, today);
        response.setDailyChallenge(dailyChallenge == null ? null : buildDailyChallenge(dailyChallenge));
        response.setKnowledgeSpotlight(buildKnowledgeSpotlight(userId, today, dailyChallenge));
        return response;
    }

    private DashboardResponse.Overview buildOverview(
            DashboardRepository.CompletionStats completion,
            long totalStudySeconds,
            long currentWeekSeconds,
            double averageScore,
            double scoreChange
    ) {
        DashboardResponse.Overview overview = new DashboardResponse.Overview();
        overview.setCompletedLessons(completion.completedLessons);
        overview.setCompletedLessonsThisWeek(completion.completedThisWeek);
        overview.setTotalStudySeconds(totalStudySeconds);
        overview.setStudySecondsThisWeek(currentWeekSeconds);
        overview.setAverageScore(averageScore);
        overview.setAverageScoreChange(scoreChange);
        return overview;
    }

    private DashboardResponse.Learner buildLearner(
            long completedLessons,
            DashboardRepository.AnswerStats answers
    ) {
        long xp = answers.submittedAnswers * XP_PER_SUBMITTED_ANSWER
                + answers.correctAnswers * XP_PER_CORRECT_ANSWER
                + completedLessons * XP_PER_COMPLETED_LESSON;
        long calculatedLevel = xp / XP_PER_LEVEL + 1;
        int level = (int) Math.min(Integer.MAX_VALUE, calculatedLevel);

        DashboardResponse.Learner learner = new DashboardResponse.Learner();
        learner.setXp(xp);
        learner.setLevel(level);
        learner.setRank(rankFor(level));
        return learner;
    }

    private DashboardResponse.ContinueLearning buildContinueLearning(
            DashboardRepository.ContinueLearningProjection projection
    ) {
        short currentUnit = (short) Math.max(1, Math.min(projection.currentUnit, projection.totalUnits));
        int progress = projection.totalUnits == 0
                ? 0
                : Math.min(100, Math.round(currentUnit * 100f / projection.totalUnits));

        DashboardResponse.ContinueLearning item = new DashboardResponse.ContinueLearning();
        item.setLessonId(projection.lessonId);
        item.setTitle(projection.title);
        item.setSubjectName(projection.subjectName);
        item.setGrade(projection.grade);
        item.setCurrentUnit(currentUnit);
        item.setTotalUnits(projection.totalUnits);
        item.setProgressPercent(progress);
        return item;
    }

    private DashboardResponse.WeeklyActivity buildWeeklyActivity(
            LocalDate today,
            LocalDate weekStart,
            long currentWeekSeconds,
            long previousWeekSeconds,
            Map<LocalDate, Long> activity
    ) {
        List<DashboardResponse.Day> days = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            LocalDate date = weekStart.plusDays(index);
            DashboardResponse.Day day = new DashboardResponse.Day();
            day.setDate(date);
            day.setLabel(dayLabel(date.getDayOfWeek()));
            day.setStudySeconds(activity.getOrDefault(date, 0L));
            day.setToday(date.equals(today));
            days.add(day);
        }

        int changePercent;
        if (previousWeekSeconds == 0) {
            changePercent = currentWeekSeconds == 0 ? 0 : 100;
        } else {
            changePercent = (int) Math.round(
                    (currentWeekSeconds - previousWeekSeconds) * 100.0 / previousWeekSeconds
            );
        }

        DashboardResponse.WeeklyActivity weekly = new DashboardResponse.WeeklyActivity();
        weekly.setWeekStart(weekStart);
        weekly.setTotalSeconds(currentWeekSeconds);
        weekly.setChangePercent(changePercent);
        weekly.setDays(days);
        return weekly;
    }

    private DashboardResponse.SubjectProgress buildSubjectProgress(
            DashboardRepository.SubjectProgressProjection projection
    ) {
        DashboardResponse.SubjectProgress subject = new DashboardResponse.SubjectProgress();
        subject.setSubjectCode(projection.subjectCode);
        subject.setTotalLessons(projection.totalLessons);
        subject.setProgressPercent(Math.max(0, Math.min(100, projection.progressPercent)));
        return subject;
    }

    private DashboardRepository.DailyChallengeProjection selectDailyChallenge(Long userId, LocalDate today) {
        List<DashboardRepository.DailyChallengeProjection> candidates = dashboardRepository
                .findDailyChallengeCandidates(userId)
                .stream()
                .filter(candidate -> !candidate.inProgress)
                .toList();
        if (candidates.isEmpty()) return null;

        List<DashboardRepository.DailyChallengeProjection> unfinished = candidates.stream()
                .filter(candidate -> !candidate.completed)
                .toList();
        List<DashboardRepository.DailyChallengeProjection> selectionPool = unfinished.isEmpty()
                ? candidates
                : unfinished;
        return stableSelection(selectionPool, userId, today, "daily-challenge");
    }

    private DashboardResponse.DailyChallenge buildDailyChallenge(
            DashboardRepository.DailyChallengeProjection projection
    ) {
        long answerXp = projection.questionCount * (XP_PER_SUBMITTED_ANSWER + XP_PER_CORRECT_ANSWER);
        long completionXp = projection.completed ? 0 : XP_PER_COMPLETED_LESSON;

        DashboardResponse.DailyChallenge challenge = new DashboardResponse.DailyChallenge();
        challenge.setLessonId(projection.lessonId);
        challenge.setSectionId(projection.sectionId);
        challenge.setModeId(projection.modeId);
        challenge.setLessonTitle(projection.lessonTitle);
        challenge.setSubjectName(projection.subjectName);
        challenge.setQuestionCount(projection.questionCount);
        challenge.setDurationMinutes(projection.durationMinutes);
        challenge.setPotentialXp(answerXp + completionXp);
        return challenge;
    }

    private DashboardResponse.KnowledgeSpotlight buildKnowledgeSpotlight(
            Long userId,
            LocalDate today,
            DashboardRepository.DailyChallengeProjection dailyChallenge
    ) {
        List<DashboardRepository.KnowledgeSpotlightProjection> candidates = dashboardRepository
                .findKnowledgeSpotlightCandidates();
        if (dailyChallenge != null && candidates.size() > 1) {
            candidates = candidates.stream()
                    .filter(candidate -> candidate.lessonId != dailyChallenge.lessonId)
                    .toList();
        }
        if (candidates.isEmpty()) return null;

        DashboardRepository.KnowledgeSpotlightProjection projection = stableSelection(
                candidates, userId, today, "knowledge-spotlight"
        );
        DashboardResponse.KnowledgeSpotlight spotlight = new DashboardResponse.KnowledgeSpotlight();
        spotlight.setLessonId(projection.lessonId);
        spotlight.setTitle(projection.title);
        spotlight.setDescription(projection.description);
        return spotlight;
    }

    private <T> T stableSelection(List<T> candidates, Long userId, LocalDate today, String salt) {
        int index = Math.floorMod(Objects.hash(userId, today.toEpochDay(), salt), candidates.size());
        return candidates.get(index);
    }

    private long sumActivity(Map<LocalDate, Long> activity, LocalDate start, LocalDate endExclusive) {
        return activity.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(start) && entry.getKey().isBefore(endExclusive))
                .mapToLong(Map.Entry::getValue)
                .sum();
    }

    private double score(long correct, long total) {
        if (total == 0) return 0;
        return roundOneDecimal(correct * 10.0 / total);
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String rankFor(int level) {
        if (level >= 10) return "Bậc thầy tri thức";
        if (level >= 6) return "Học giả trẻ";
        if (level >= 3) return "Học viên chăm chỉ";
        return "Người khám phá";
    }

    private String dayLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "T2";
            case TUESDAY -> "T3";
            case WEDNESDAY -> "T4";
            case THURSDAY -> "T5";
            case FRIDAY -> "T6";
            case SATURDAY -> "T7";
            case SUNDAY -> "CN";
        };
    }
}
