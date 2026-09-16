package vn.tera.learn.dto;

import java.time.Instant;
import java.util.List;

public class LessonExploreResponse {

    private Summary summary;
    private List<SubjectFilter> subjects;
    private List<LessonCard> lessons;

    public LessonExploreResponse() {
    }

    public LessonExploreResponse(Summary summary, List<SubjectFilter> subjects, List<LessonCard> lessons) {
        this.summary = summary;
        this.subjects = subjects;
        this.lessons = lessons;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public List<SubjectFilter> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectFilter> subjects) {
        this.subjects = subjects;
    }

    public List<LessonCard> getLessons() {
        return lessons;
    }

    public void setLessons(List<LessonCard> lessons) {
        this.lessons = lessons;
    }

    public static class Summary {

        private int totalLessons;
        private int completedLessons;
        private int subjectCount;

        public Summary() {
        }

        public Summary(int totalLessons, int completedLessons, int subjectCount) {
            this.totalLessons = totalLessons;
            this.completedLessons = completedLessons;
            this.subjectCount = subjectCount;
        }

        public int getTotalLessons() {
            return totalLessons;
        }

        public void setTotalLessons(int totalLessons) {
            this.totalLessons = totalLessons;
        }

        public int getCompletedLessons() {
            return completedLessons;
        }

        public void setCompletedLessons(int completedLessons) {
            this.completedLessons = completedLessons;
        }

        public int getSubjectCount() {
            return subjectCount;
        }

        public void setSubjectCount(int subjectCount) {
            this.subjectCount = subjectCount;
        }
    }

    public static class SubjectFilter {

        private String code;
        private String name;
        private String shortName;
        private String accent;
        private long lessonCount;

        public SubjectFilter() {
        }

        public SubjectFilter(String code, String name, String shortName, String accent, long lessonCount) {
            this.code = code;
            this.name = name;
            this.shortName = shortName;
            this.accent = accent;
            this.lessonCount = lessonCount;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public String getAccent() {
            return accent;
        }

        public void setAccent(String accent) {
            this.accent = accent;
        }

        public long getLessonCount() {
            return lessonCount;
        }

        public void setLessonCount(long lessonCount) {
            this.lessonCount = lessonCount;
        }
    }

    public static class LessonCard {

        private Long id;
        private String slug;
        private String subjectCode;
        private String subjectName;
        private String subjectShortName;
        private String accent;
        private short grade;
        private String title;
        private String description;
        private String thumbnailUrl;
        private String difficulty;
        private String program;
        private short totalUnits;
        private short currentUnit;
        private int progressPercent;
        private String status;
        private boolean bookmarked;
        private int viewCount;
        private int likeCount;
        private double averageRating;
        private Instant publishedAt;

        public LessonCard() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getSubjectCode() {
            return subjectCode;
        }

        public void setSubjectCode(String subjectCode) {
            this.subjectCode = subjectCode;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public String getSubjectShortName() {
            return subjectShortName;
        }

        public void setSubjectShortName(String subjectShortName) {
            this.subjectShortName = subjectShortName;
        }

        public String getAccent() {
            return accent;
        }

        public void setAccent(String accent) {
            this.accent = accent;
        }

        public short getGrade() {
            return grade;
        }

        public void setGrade(short grade) {
            this.grade = grade;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public String getProgram() {
            return program;
        }

        public void setProgram(String program) {
            this.program = program;
        }

        public short getTotalUnits() {
            return totalUnits;
        }

        public void setTotalUnits(short totalUnits) {
            this.totalUnits = totalUnits;
        }

        public short getCurrentUnit() {
            return currentUnit;
        }

        public void setCurrentUnit(short currentUnit) {
            this.currentUnit = currentUnit;
        }

        public int getProgressPercent() {
            return progressPercent;
        }

        public void setProgressPercent(int progressPercent) {
            this.progressPercent = progressPercent;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isBookmarked() {
            return bookmarked;
        }

        public void setBookmarked(boolean bookmarked) {
            this.bookmarked = bookmarked;
        }

        public int getViewCount() {
            return viewCount;
        }

        public void setViewCount(int viewCount) {
            this.viewCount = viewCount;
        }

        public int getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(double averageRating) {
            this.averageRating = averageRating;
        }

        public Instant getPublishedAt() {
            return publishedAt;
        }

        public void setPublishedAt(Instant publishedAt) {
            this.publishedAt = publishedAt;
        }
    }
}
