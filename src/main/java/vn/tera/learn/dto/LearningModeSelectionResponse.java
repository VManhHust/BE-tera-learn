package vn.tera.learn.dto;

import java.util.List;

public class LearningModeSelectionResponse {

    private Long lessonId;
    private String lessonTitle;
    private String subjectName;
    private String subjectAccent;
    private Long sectionId;
    private String sectionTitle;
    private String sectionSummary;
    private short sectionNumber;
    private long totalSections;
    private List<Mode> modes;

    public LearningModeSelectionResponse() {
    }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectAccent() { return subjectAccent; }
    public void setSubjectAccent(String subjectAccent) { this.subjectAccent = subjectAccent; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }
    public String getSectionSummary() { return sectionSummary; }
    public void setSectionSummary(String sectionSummary) { this.sectionSummary = sectionSummary; }
    public short getSectionNumber() { return sectionNumber; }
    public void setSectionNumber(short sectionNumber) { this.sectionNumber = sectionNumber; }
    public long getTotalSections() { return totalSections; }
    public void setTotalSections(long totalSections) { this.totalSections = totalSections; }
    public List<Mode> getModes() { return modes; }
    public void setModes(List<Mode> modes) { this.modes = modes; }

    public static class Mode {
        private Long id;
        private String code;
        private String name;
        private String description;
        private String timeCaption;
        private String feedbackCaption;
        private Short timeLimitMinutes;
        private boolean immediateFeedback;
        private String accent;
        private String illustrationKey;
        private String illustrationUrl;

        public Mode() {
        }

        public Mode(
                Long id,
                String code,
                String name,
                String description,
                String timeCaption,
                String feedbackCaption,
                Short timeLimitMinutes,
                boolean immediateFeedback,
                String accent,
                String illustrationKey,
                String illustrationUrl
        ) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.description = description;
            this.timeCaption = timeCaption;
            this.feedbackCaption = feedbackCaption;
            this.timeLimitMinutes = timeLimitMinutes;
            this.immediateFeedback = immediateFeedback;
            this.accent = accent;
            this.illustrationKey = illustrationKey;
            this.illustrationUrl = illustrationUrl;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTimeCaption() { return timeCaption; }
        public void setTimeCaption(String timeCaption) { this.timeCaption = timeCaption; }
        public String getFeedbackCaption() { return feedbackCaption; }
        public void setFeedbackCaption(String feedbackCaption) { this.feedbackCaption = feedbackCaption; }
        public Short getTimeLimitMinutes() { return timeLimitMinutes; }
        public void setTimeLimitMinutes(Short timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
        public boolean isImmediateFeedback() { return immediateFeedback; }
        public void setImmediateFeedback(boolean immediateFeedback) { this.immediateFeedback = immediateFeedback; }
        public String getAccent() { return accent; }
        public void setAccent(String accent) { this.accent = accent; }
        public String getIllustrationKey() { return illustrationKey; }
        public void setIllustrationKey(String illustrationKey) { this.illustrationKey = illustrationKey; }
        public String getIllustrationUrl() { return illustrationUrl; }
        public void setIllustrationUrl(String illustrationUrl) { this.illustrationUrl = illustrationUrl; }
    }
}
