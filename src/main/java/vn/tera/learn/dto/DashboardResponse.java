package vn.tera.learn.dto;

import java.time.LocalDate;
import java.util.List;

public class DashboardResponse {
    private Overview overview;
    private Learner learner;
    private ContinueLearning continueLearning;
    private WeeklyActivity weeklyActivity;
    private List<SubjectProgress> subjectProgress;
    private DailyChallenge dailyChallenge;
    private KnowledgeSpotlight knowledgeSpotlight;

    public Overview getOverview() { return overview; }
    public void setOverview(Overview overview) { this.overview = overview; }
    public Learner getLearner() { return learner; }
    public void setLearner(Learner learner) { this.learner = learner; }
    public ContinueLearning getContinueLearning() { return continueLearning; }
    public void setContinueLearning(ContinueLearning continueLearning) { this.continueLearning = continueLearning; }
    public WeeklyActivity getWeeklyActivity() { return weeklyActivity; }
    public void setWeeklyActivity(WeeklyActivity weeklyActivity) { this.weeklyActivity = weeklyActivity; }
    public List<SubjectProgress> getSubjectProgress() { return subjectProgress; }
    public void setSubjectProgress(List<SubjectProgress> subjectProgress) { this.subjectProgress = subjectProgress; }
    public DailyChallenge getDailyChallenge() { return dailyChallenge; }
    public void setDailyChallenge(DailyChallenge dailyChallenge) { this.dailyChallenge = dailyChallenge; }
    public KnowledgeSpotlight getKnowledgeSpotlight() { return knowledgeSpotlight; }
    public void setKnowledgeSpotlight(KnowledgeSpotlight knowledgeSpotlight) { this.knowledgeSpotlight = knowledgeSpotlight; }

    public static class Overview {
        private long completedLessons;
        private long completedLessonsThisWeek;
        private long totalStudySeconds;
        private long studySecondsThisWeek;
        private double averageScore;
        private double averageScoreChange;

        public long getCompletedLessons() { return completedLessons; }
        public void setCompletedLessons(long completedLessons) { this.completedLessons = completedLessons; }
        public long getCompletedLessonsThisWeek() { return completedLessonsThisWeek; }
        public void setCompletedLessonsThisWeek(long completedLessonsThisWeek) { this.completedLessonsThisWeek = completedLessonsThisWeek; }
        public long getTotalStudySeconds() { return totalStudySeconds; }
        public void setTotalStudySeconds(long totalStudySeconds) { this.totalStudySeconds = totalStudySeconds; }
        public long getStudySecondsThisWeek() { return studySecondsThisWeek; }
        public void setStudySecondsThisWeek(long studySecondsThisWeek) { this.studySecondsThisWeek = studySecondsThisWeek; }
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
        public double getAverageScoreChange() { return averageScoreChange; }
        public void setAverageScoreChange(double averageScoreChange) { this.averageScoreChange = averageScoreChange; }
    }

    public static class Learner {
        private int level;
        private long xp;
        private String rank;

        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public long getXp() { return xp; }
        public void setXp(long xp) { this.xp = xp; }
        public String getRank() { return rank; }
        public void setRank(String rank) { this.rank = rank; }
    }

    public static class ContinueLearning {
        private long lessonId;
        private String title;
        private String subjectName;
        private short grade;
        private short currentUnit;
        private short totalUnits;
        private int progressPercent;

        public long getLessonId() { return lessonId; }
        public void setLessonId(long lessonId) { this.lessonId = lessonId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        public short getGrade() { return grade; }
        public void setGrade(short grade) { this.grade = grade; }
        public short getCurrentUnit() { return currentUnit; }
        public void setCurrentUnit(short currentUnit) { this.currentUnit = currentUnit; }
        public short getTotalUnits() { return totalUnits; }
        public void setTotalUnits(short totalUnits) { this.totalUnits = totalUnits; }
        public int getProgressPercent() { return progressPercent; }
        public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    }

    public static class WeeklyActivity {
        private LocalDate weekStart;
        private long totalSeconds;
        private int changePercent;
        private List<Day> days;

        public LocalDate getWeekStart() { return weekStart; }
        public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }
        public long getTotalSeconds() { return totalSeconds; }
        public void setTotalSeconds(long totalSeconds) { this.totalSeconds = totalSeconds; }
        public int getChangePercent() { return changePercent; }
        public void setChangePercent(int changePercent) { this.changePercent = changePercent; }
        public List<Day> getDays() { return days; }
        public void setDays(List<Day> days) { this.days = days; }
    }

    public static class Day {
        private LocalDate date;
        private String label;
        private long studySeconds;
        private boolean today;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public long getStudySeconds() { return studySeconds; }
        public void setStudySeconds(long studySeconds) { this.studySeconds = studySeconds; }
        public boolean isToday() { return today; }
        public void setToday(boolean today) { this.today = today; }
    }

    public static class SubjectProgress {
        private String subjectCode;
        private long totalLessons;
        private int progressPercent;

        public String getSubjectCode() { return subjectCode; }
        public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
        public long getTotalLessons() { return totalLessons; }
        public void setTotalLessons(long totalLessons) { this.totalLessons = totalLessons; }
        public int getProgressPercent() { return progressPercent; }
        public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    }

    public static class DailyChallenge {
        private long lessonId;
        private long sectionId;
        private long modeId;
        private String lessonTitle;
        private String subjectName;
        private int questionCount;
        private int durationMinutes;
        private long potentialXp;

        public long getLessonId() { return lessonId; }
        public void setLessonId(long lessonId) { this.lessonId = lessonId; }
        public long getSectionId() { return sectionId; }
        public void setSectionId(long sectionId) { this.sectionId = sectionId; }
        public long getModeId() { return modeId; }
        public void setModeId(long modeId) { this.modeId = modeId; }
        public String getLessonTitle() { return lessonTitle; }
        public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        public int getQuestionCount() { return questionCount; }
        public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        public long getPotentialXp() { return potentialXp; }
        public void setPotentialXp(long potentialXp) { this.potentialXp = potentialXp; }
    }

    public static class KnowledgeSpotlight {
        private long lessonId;
        private String title;
        private String description;

        public long getLessonId() { return lessonId; }
        public void setLessonId(long lessonId) { this.lessonId = lessonId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
