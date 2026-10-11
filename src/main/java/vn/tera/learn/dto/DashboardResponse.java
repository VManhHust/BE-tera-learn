package vn.tera.learn.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardResponse {
    private Overview overview;
    private Learner learner;
    private ContinueLearning continueLearning;
    private WeeklyActivity weeklyActivity;
    private List<SubjectProgress> subjectProgress;
    private DailyChallenge dailyChallenge;
    private KnowledgeSpotlight knowledgeSpotlight;

    @Getter @Setter @NoArgsConstructor
    public static class Overview {
        private long completedLessons;
        private long completedLessonsThisWeek;
        private long totalStudySeconds;
        private long studySecondsThisWeek;
        private double averageScore;
        private double averageScoreChange;
    }

    @Getter @Setter @NoArgsConstructor
    public static class Learner {
        private int level;
        private long xp;
        private String rank;
    }

    @Getter @Setter @NoArgsConstructor
    public static class ContinueLearning {
        private long lessonId;
        private String title;
        private String subjectName;
        private short grade;
        private short currentUnit;
        private short totalUnits;
        private int progressPercent;
    }

    @Getter @Setter @NoArgsConstructor
    public static class WeeklyActivity {
        private LocalDate weekStart;
        private long totalSeconds;
        private int changePercent;
        private List<Day> days;
    }

    @Getter @Setter @NoArgsConstructor
    public static class Day {
        private LocalDate date;
        private String label;
        private long studySeconds;
        private boolean today;
    }

    @Getter @Setter @NoArgsConstructor
    public static class SubjectProgress {
        private String subjectCode;
        private long totalLessons;
        private int progressPercent;
    }

    @Getter @Setter @NoArgsConstructor
    public static class DailyChallenge {
        private long lessonId;
        private long sectionId;
        private long modeId;
        private String lessonTitle;
        private String subjectName;
        private int questionCount;
        private int durationMinutes;
        private long potentialXp;
    }

    @Getter @Setter @NoArgsConstructor
    public static class KnowledgeSpotlight {
        private long lessonId;
        private String title;
        private String description;
    }
}
