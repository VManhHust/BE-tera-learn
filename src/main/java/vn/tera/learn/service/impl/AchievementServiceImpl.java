package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.AchievementResponse;
import vn.tera.learn.dto.StreakStats;
import vn.tera.learn.repository.AchievementRepository;
import vn.tera.learn.repository.UserRepository;
import vn.tera.learn.service.AchievementService;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AchievementServiceImpl implements AchievementService {
    private static final ZoneId LEARNING_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final long XP_PER_LEVEL = 500;
    private static final int GROWTH_WEEKS = 8;

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final AchievementMath achievementMath;

    public AchievementServiceImpl(
            AchievementRepository achievementRepository,
            UserRepository userRepository,
            AchievementMath achievementMath
    ) {
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
        this.achievementMath = achievementMath;
    }

    @Override
    @Transactional
    public AchievementResponse getAchievements(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }

        LocalDate today = LocalDate.now(LEARNING_ZONE);
        Instant calculatedAt = Instant.now();
        AchievementRepository.SummarySource source = achievementRepository.findSummary(userId);
        List<LocalDate> learningDates = achievementRepository.findLearningDates(userId);
        StreakStats streak = achievementMath.streak(
                achievementRepository.findStreakDates(userId), today
        );

        long totalXp = achievementMath.totalXp(
                source.getAnsweredQuestions(), source.getCorrectAnswers(), source.getCompletedLessons()
        );
        int level = Math.toIntExact(totalXp / XP_PER_LEVEL + 1);
        double accuracy = percent(source.getCorrectAnswers(), source.getAnsweredQuestions());
        String rank = rankFor(level);
        LocalDate lastActivityDate = learningDates.isEmpty() ? null : learningDates.get(learningDates.size() - 1);

        AchievementResponse.Summary summary = new AchievementResponse.Summary(
                totalXp,
                level,
                rank,
                source.getCompletedLessons(),
                source.getCompletedSections(),
                source.getAnsweredQuestions(),
                source.getCorrectAnswers(),
                roundOneDecimal(accuracy),
                source.getTotalStudySeconds(),
                learningDates.size(),
                streak.getCurrent(),
                streak.getLongest(),
                lastActivityDate
        );

        achievementRepository.saveSnapshot(userId, new AchievementRepository.AchievementSnapshot(
                totalXp, level, rank, source.getCompletedLessons(), source.getCompletedSections(),
                source.getAnsweredQuestions(), source.getCorrectAnswers(), roundOneDecimal(accuracy),
                source.getTotalStudySeconds(), learningDates.size(), streak.getCurrent(), streak.getLongest(),
                lastActivityDate, calculatedAt
        ));

        List<AchievementResponse.SubjectMastery> subjects = achievementRepository.findSubjectMastery(userId)
                .stream()
                .map(this::toSubjectMastery)
                .toList();
        List<AchievementResponse.WeeklyGrowth> growth = buildGrowth(userId, today, totalXp);

        return new AchievementResponse(
                summary,
                buildLevelProgress(totalXp, level),
                growth,
                subjects,
                buildMilestones(summary),
                buildRecommendations(userId, summary, growth, subjects),
                calculatedAt
        );
    }

    private AchievementResponse.LevelProgress buildLevelProgress(long totalXp, int level) {
        long xpInLevel = totalXp % XP_PER_LEVEL;
        long remaining = XP_PER_LEVEL - xpInLevel;
        return new AchievementResponse.LevelProgress(
                xpInLevel,
                XP_PER_LEVEL,
                remaining,
                clampPercent(Math.round(xpInLevel * 100f / XP_PER_LEVEL)),
                nextRankFor(level)
        );
    }

    private List<AchievementResponse.WeeklyGrowth> buildGrowth(Long userId, LocalDate today, long totalXp) {
        LocalDate currentWeek = today.with(DayOfWeek.MONDAY);
        LocalDate start = currentWeek.minusWeeks(GROWTH_WEEKS - 1L);
        LocalDate end = currentWeek.plusWeeks(1);
        List<AchievementRepository.WeeklyGrowthSource> source = achievementRepository
                .findWeeklyGrowth(userId, start, end);
        long periodXp = source.stream().mapToLong(this::earnedXp).sum();
        long cumulativeXp = Math.max(0, totalXp - periodXp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        List<AchievementResponse.WeeklyGrowth> result = new ArrayList<>();
        for (AchievementRepository.WeeklyGrowthSource week : source) {
            long earnedXp = earnedXp(week);
            cumulativeXp += earnedXp;
            result.add(new AchievementResponse.WeeklyGrowth(
                    week.getWeekStart(),
                    week.getWeekStart().format(formatter),
                    earnedXp,
                    cumulativeXp,
                    week.getStudySeconds(),
                    week.getCompletedLessons()
            ));
        }
        return result;
    }

    private long earnedXp(AchievementRepository.WeeklyGrowthSource week) {
        return achievementMath.totalXp(
                week.getAnsweredQuestions(), week.getCorrectAnswers(), week.getCompletedLessons()
        );
    }

    private AchievementResponse.SubjectMastery toSubjectMastery(
            AchievementRepository.SubjectMasterySource source
    ) {
        int progress = clampPercent(Math.round((float) percent(source.getCompletedUnits(), source.getTotalUnits())));
        int accuracy = clampPercent(Math.round((float) percent(source.getCorrectAnswers(), source.getAnsweredQuestions())));
        int mastery = source.getAnsweredQuestions() == 0
                ? progress
                : clampPercent(Math.round(progress * 0.65f + accuracy * 0.35f));
        return new AchievementResponse.SubjectMastery(
                source.getSubjectCode(), source.getSubjectName(), source.getAccent(), source.getCompletedLessons(),
                source.getTotalLessons(), source.getAnsweredQuestions(), source.getCorrectAnswers(), progress,
                accuracy, mastery
        );
    }

    private List<AchievementResponse.Milestone> buildMilestones(AchievementResponse.Summary summary) {
        return List.of(
                milestone("FIRST_LESSON", "Bước chân đầu tiên", "Hoàn thành bài học đầu tiên", summary.getCompletedLessons(), 1),
                milestone("FOCUS_HOUR", "Một giờ tập trung", "Tích lũy 60 phút học thật", summary.getTotalStudySeconds(), 3600),
                milestone("TEN_LESSONS", "Nhà khám phá bền bỉ", "Hoàn thành 10 bài học", summary.getCompletedLessons(), 10),
                milestone("HUNDRED_ANSWERS", "Tư duy sắc bén", "Trả lời 100 câu hỏi", summary.getAnsweredQuestions(), 100),
                milestone("SEVEN_DAY_STREAK", "Nhịp học 7 ngày", "Duy trì chuỗi điểm danh 7 ngày", summary.getLongestStreakDays(), 7)
        );
    }

    private AchievementResponse.Milestone milestone(
            String code,
            String title,
            String description,
            long current,
            long target
    ) {
        return new AchievementResponse.Milestone(
                code, title, description, Math.min(current, target), target,
                clampPercent(Math.round(Math.min(current, target) * 100f / target)), current >= target
        );
    }

    private List<AchievementResponse.Recommendation> buildRecommendations(
            Long userId,
            AchievementResponse.Summary summary,
            List<AchievementResponse.WeeklyGrowth> growth,
            List<AchievementResponse.SubjectMastery> subjects
    ) {
        List<AchievementResponse.Recommendation> result = new ArrayList<>();
        achievementRepository.findInProgressLesson(userId).ifPresent(lesson -> result.add(
                new AchievementResponse.Recommendation(
                        "CONTINUE_LESSON",
                        "Hoàn thành bài đang học dở",
                        "Bạn đang ở phần " + lesson.getCurrentUnit() + "/" + lesson.getTotalUnits()
                                + " của “" + lesson.getTitle() + "”. Học tiếp để giữ mạch kiến thức.",
                        "Học tiếp",
                        lesson.getLessonId(),
                        lesson.getSubjectCode()
                )
        ));

        subjects.stream()
                .filter(subject -> subject.getTotalLessons() > 0 && subject.getProgressPercent() < 100)
                .min(Comparator
                        .comparingInt((AchievementResponse.SubjectMastery subject) ->
                                subject.getProgressPercent() > 0 || subject.getAnsweredQuestions() > 0 ? 0 : 1)
                        .thenComparingInt(AchievementResponse.SubjectMastery::getMasteryPercent))
                .flatMap(subject -> achievementRepository.findRecommendedLesson(userId, subject.getSubjectCode())
                        .map(lesson -> new AchievementResponse.Recommendation(
                                "STRENGTHEN_SUBJECT",
                                subject.getAnsweredQuestions() > 0 && subject.getAccuracyPercent() < 70
                                        ? "Củng cố điểm yếu ở " + subject.getSubjectName()
                                        : "Mở rộng lộ trình " + subject.getSubjectName(),
                                "Bài “" + lesson.getTitle() + "” phù hợp để nâng mức làm chủ hiện tại từ "
                                        + subject.getMasteryPercent() + "%.",
                                "Xem bài gợi ý",
                                lesson.getLessonId(),
                                subject.getSubjectCode()
                        )))
                .ifPresent(result::add);

        long currentWeekSeconds = growth.isEmpty() ? 0 : growth.get(growth.size() - 1).getStudySeconds();
        if (currentWeekSeconds < 90 * 60L || summary.getCurrentStreakDays() < 3) {
            result.add(new AchievementResponse.Recommendation(
                    "BUILD_HABIT",
                    "Tạo nhịp học 15 phút mỗi ngày",
                    "Một phiên ngắn hôm nay sẽ giúp biểu đồ tuần đều hơn và duy trì chuỗi học tập.",
                    "Chọn bài để học",
                    null,
                    null
            ));
        }
        return result.stream().limit(3).toList();
    }

    private double percent(long value, long total) {
        return total <= 0 ? 0 : value * 100.0 / total;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private int clampPercent(long value) {
        return (int) Math.max(0, Math.min(100, value));
    }

    private String rankFor(int level) {
        if (level >= 10) return "Bậc thầy tri thức";
        if (level >= 6) return "Học giả trẻ";
        if (level >= 3) return "Học viên chăm chỉ";
        return "Người khám phá";
    }

    private String nextRankFor(int level) {
        if (level < 3) return "Học viên chăm chỉ";
        if (level < 6) return "Học giả trẻ";
        return "Bậc thầy tri thức";
    }
}
