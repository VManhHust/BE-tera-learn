package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.AchievementResponse;
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

    public AchievementServiceImpl(
            AchievementRepository achievementRepository,
            UserRepository userRepository
    ) {
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
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
        AchievementMath.StreakStats streak = AchievementMath.streak(
                achievementRepository.findStreakDates(userId), today
        );

        long totalXp = AchievementMath.totalXp(
                source.answeredQuestions(), source.correctAnswers(), source.completedLessons()
        );
        int level = Math.toIntExact(totalXp / XP_PER_LEVEL + 1);
        double accuracy = percent(source.correctAnswers(), source.answeredQuestions());
        String rank = rankFor(level);
        LocalDate lastActivityDate = learningDates.isEmpty() ? null : learningDates.get(learningDates.size() - 1);

        AchievementResponse.Summary summary = new AchievementResponse.Summary(
                totalXp,
                level,
                rank,
                source.completedLessons(),
                source.completedSections(),
                source.answeredQuestions(),
                source.correctAnswers(),
                roundOneDecimal(accuracy),
                source.totalStudySeconds(),
                learningDates.size(),
                streak.current(),
                streak.longest(),
                lastActivityDate
        );

        achievementRepository.saveSnapshot(userId, new AchievementRepository.AchievementSnapshot(
                totalXp, level, rank, source.completedLessons(), source.completedSections(),
                source.answeredQuestions(), source.correctAnswers(), roundOneDecimal(accuracy),
                source.totalStudySeconds(), learningDates.size(), streak.current(), streak.longest(),
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
                    week.weekStart(),
                    week.weekStart().format(formatter),
                    earnedXp,
                    cumulativeXp,
                    week.studySeconds(),
                    week.completedLessons()
            ));
        }
        return result;
    }

    private long earnedXp(AchievementRepository.WeeklyGrowthSource week) {
        return AchievementMath.totalXp(
                week.answeredQuestions(), week.correctAnswers(), week.completedLessons()
        );
    }

    private AchievementResponse.SubjectMastery toSubjectMastery(
            AchievementRepository.SubjectMasterySource source
    ) {
        int progress = clampPercent(Math.round((float) percent(source.completedUnits(), source.totalUnits())));
        int accuracy = clampPercent(Math.round((float) percent(source.correctAnswers(), source.answeredQuestions())));
        int mastery = source.answeredQuestions() == 0
                ? progress
                : clampPercent(Math.round(progress * 0.65f + accuracy * 0.35f));
        return new AchievementResponse.SubjectMastery(
                source.subjectCode(), source.subjectName(), source.accent(), source.completedLessons(),
                source.totalLessons(), source.answeredQuestions(), source.correctAnswers(), progress,
                accuracy, mastery
        );
    }

    private List<AchievementResponse.Milestone> buildMilestones(AchievementResponse.Summary summary) {
        return List.of(
                milestone("FIRST_LESSON", "Bước chân đầu tiên", "Hoàn thành bài học đầu tiên", summary.completedLessons(), 1),
                milestone("FOCUS_HOUR", "Một giờ tập trung", "Tích lũy 60 phút học thật", summary.totalStudySeconds(), 3600),
                milestone("TEN_LESSONS", "Nhà khám phá bền bỉ", "Hoàn thành 10 bài học", summary.completedLessons(), 10),
                milestone("HUNDRED_ANSWERS", "Tư duy sắc bén", "Trả lời 100 câu hỏi", summary.answeredQuestions(), 100),
                milestone("SEVEN_DAY_STREAK", "Nhịp học 7 ngày", "Duy trì chuỗi điểm danh 7 ngày", summary.longestStreakDays(), 7)
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
                        "Bạn đang ở phần " + lesson.currentUnit() + "/" + lesson.totalUnits()
                                + " của “" + lesson.title() + "”. Học tiếp để giữ mạch kiến thức.",
                        "Học tiếp",
                        lesson.lessonId(),
                        lesson.subjectCode()
                )
        ));

        subjects.stream()
                .filter(subject -> subject.totalLessons() > 0 && subject.progressPercent() < 100)
                .min(Comparator
                        .comparingInt((AchievementResponse.SubjectMastery subject) ->
                                subject.progressPercent() > 0 || subject.answeredQuestions() > 0 ? 0 : 1)
                        .thenComparingInt(AchievementResponse.SubjectMastery::masteryPercent))
                .flatMap(subject -> achievementRepository.findRecommendedLesson(userId, subject.subjectCode())
                        .map(lesson -> new AchievementResponse.Recommendation(
                                "STRENGTHEN_SUBJECT",
                                subject.answeredQuestions() > 0 && subject.accuracyPercent() < 70
                                        ? "Củng cố điểm yếu ở " + subject.subjectName()
                                        : "Mở rộng lộ trình " + subject.subjectName(),
                                "Bài “" + lesson.title() + "” phù hợp để nâng mức làm chủ hiện tại từ "
                                        + subject.masteryPercent() + "%.",
                                "Xem bài gợi ý",
                                lesson.lessonId(),
                                subject.subjectCode()
                        )))
                .ifPresent(result::add);

        long currentWeekSeconds = growth.isEmpty() ? 0 : growth.get(growth.size() - 1).studySeconds();
        if (currentWeekSeconds < 90 * 60L || summary.currentStreakDays() < 3) {
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
