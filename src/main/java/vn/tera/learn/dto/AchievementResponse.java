package vn.tera.learn.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AchievementResponse(
        Summary summary,
        LevelProgress levelProgress,
        List<WeeklyGrowth> growth,
        List<SubjectMastery> subjects,
        List<Milestone> milestones,
        List<Recommendation> recommendations,
        Instant calculatedAt
) {
    public record Summary(
            long totalXp,
            int level,
            String rank,
            long completedLessons,
            long completedSections,
            long answeredQuestions,
            long correctAnswers,
            double accuracyPercent,
            long totalStudySeconds,
            int activeDays,
            int currentStreakDays,
            int longestStreakDays,
            LocalDate lastActivityDate
    ) {}

    public record LevelProgress(
            long xpInLevel,
            long xpForNextLevel,
            long xpRemaining,
            int progressPercent,
            String nextRank
    ) {}

    public record WeeklyGrowth(
            LocalDate weekStart,
            String label,
            long earnedXp,
            long cumulativeXp,
            long studySeconds,
            long completedLessons
    ) {}

    public record SubjectMastery(
            String subjectCode,
            String subjectName,
            String accent,
            long completedLessons,
            long totalLessons,
            long answeredQuestions,
            long correctAnswers,
            int progressPercent,
            int accuracyPercent,
            int masteryPercent
    ) {}

    public record Milestone(
            String code,
            String title,
            String description,
            long currentValue,
            long targetValue,
            int progressPercent,
            boolean achieved
    ) {}

    public record Recommendation(
            String type,
            String title,
            String description,
            String actionLabel,
            Long lessonId,
            String subjectCode
    ) {}
}

