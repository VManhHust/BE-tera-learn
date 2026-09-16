package vn.tera.learn.dto;

import java.time.Instant;
import java.util.List;

public class LessonDetailResponse {

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
    private int viewCount;
    private int likeCount;
    private double averageRating;
    private Instant publishedAt;
    private String badge;
    private String introduction;
    private String authorName;
    private short estimatedMinutes;
    private Instant updatedAt;
    private List<String> objectives;
    private List<Section> sections;
    private List<Resource> resources;
    private Progress progress;

    public LessonDetailResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectShortName() { return subjectShortName; }
    public void setSubjectShortName(String subjectShortName) { this.subjectShortName = subjectShortName; }
    public String getAccent() { return accent; }
    public void setAccent(String accent) { this.accent = accent; }
    public short getGrade() { return grade; }
    public void setGrade(short grade) { this.grade = grade; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public short getTotalUnits() { return totalUnits; }
    public void setTotalUnits(short totalUnits) { this.totalUnits = totalUnits; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public short getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(short estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<String> getObjectives() { return objectives; }
    public void setObjectives(List<String> objectives) { this.objectives = objectives; }
    public List<Section> getSections() { return sections; }
    public void setSections(List<Section> sections) { this.sections = sections; }
    public List<Resource> getResources() { return resources; }
    public void setResources(List<Resource> resources) { this.resources = resources; }
    public Progress getProgress() { return progress; }
    public void setProgress(Progress progress) { this.progress = progress; }

    public static class Section {
        private Long id;
        private String title;
        private String summary;
        private short durationMinutes;
        private short sortOrder;

        public Section() {
        }

        public Section(Long id, String title, String summary, short durationMinutes, short sortOrder) {
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.durationMinutes = durationMinutes;
            this.sortOrder = sortOrder;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public short getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(short durationMinutes) { this.durationMinutes = durationMinutes; }
        public short getSortOrder() { return sortOrder; }
        public void setSortOrder(short sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class Resource {
        private Long id;
        private String title;
        private String description;
        private String resourceType;
        private String fileName;
        private boolean downloadable;

        public Resource() {
        }

        public Resource(Long id, String title, String description, String resourceType, String fileName, boolean downloadable) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.resourceType = resourceType;
            this.fileName = fileName;
            this.downloadable = downloadable;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public boolean isDownloadable() { return downloadable; }
        public void setDownloadable(boolean downloadable) { this.downloadable = downloadable; }
    }

    public static class Progress {
        private String status;
        private short currentUnit;
        private int progressPercent;
        private boolean bookmarked;

        public Progress() {
        }

        public Progress(String status, short currentUnit, int progressPercent, boolean bookmarked) {
            this.status = status;
            this.currentUnit = currentUnit;
            this.progressPercent = progressPercent;
            this.bookmarked = bookmarked;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public short getCurrentUnit() { return currentUnit; }
        public void setCurrentUnit(short currentUnit) { this.currentUnit = currentUnit; }
        public int getProgressPercent() { return progressPercent; }
        public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
        public boolean isBookmarked() { return bookmarked; }
        public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }
    }
}
