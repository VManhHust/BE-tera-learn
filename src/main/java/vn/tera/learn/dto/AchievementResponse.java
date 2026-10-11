package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {

    private Summary summary;
    private LevelProgress levelProgress;
    private List<WeeklyGrowth> growth;
    private List<SubjectMastery> subjects;
    private List<Milestone> milestones;
    private List<Recommendation> recommendations;
    private Instant calculatedAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long totalXp;
        private int level;
        private String rank;
        private long completedLessons;
        private long completedSections;
        private long answeredQuestions;
        private long correctAnswers;
        private double accuracyPercent;
        private long totalStudySeconds;
        private int activeDays;
        private int currentStreakDays;
        private int longestStreakDays;
        private LocalDate lastActivityDate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelProgress {
        private long xpInLevel;
        private long xpForNextLevel;
        private long xpRemaining;
        private int progressPercent;
        private String nextRank;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyGrowth {
        private LocalDate weekStart;
        private String label;
        private long earnedXp;
        private long cumulativeXp;
        private long studySeconds;
        private long completedLessons;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectMastery {
        private String subjectCode;
        private String subjectName;
        private String accent;
        private long completedLessons;
        private long totalLessons;
        private long answeredQuestions;
        private long correctAnswers;
        private int progressPercent;
        private int accuracyPercent;
        private int masteryPercent;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Milestone {
        private String code;
        private String title;
        private String description;
        private long currentValue;
        private long targetValue;
        private int progressPercent;
        private boolean achieved;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String type;
        private String title;
        private String description;
        private String actionLabel;
        private Long lessonId;
        private String subjectCode;
    }
}
