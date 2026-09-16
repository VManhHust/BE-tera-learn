package vn.tera.learn.dto;

import java.util.List;

public class FreeTrialResponse {

    private Long lessonId;
    private String lessonSlug;
    private String lessonTitle;
    private String lessonDescription;
    private String thumbnailUrl;
    private short grade;
    private String difficulty;
    private String program;
    private String subjectCode;
    private String subjectName;
    private String subjectShortName;
    private String subjectAccent;
    private long totalSections;
    private Long sectionId;
    private String sectionTitle;
    private String sectionSummary;
    private short sectionNumber;
    private short durationMinutes;
    private NextSection nextSection;
    private int totalQuestions;
    private List<Question> questions;

    public FreeTrialResponse() {
    }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public String getLessonSlug() { return lessonSlug; }
    public void setLessonSlug(String lessonSlug) { this.lessonSlug = lessonSlug; }
    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
    public String getLessonDescription() { return lessonDescription; }
    public void setLessonDescription(String lessonDescription) { this.lessonDescription = lessonDescription; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public short getGrade() { return grade; }
    public void setGrade(short grade) { this.grade = grade; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectShortName() { return subjectShortName; }
    public void setSubjectShortName(String subjectShortName) { this.subjectShortName = subjectShortName; }
    public String getSubjectAccent() { return subjectAccent; }
    public void setSubjectAccent(String subjectAccent) { this.subjectAccent = subjectAccent; }
    public long getTotalSections() { return totalSections; }
    public void setTotalSections(long totalSections) { this.totalSections = totalSections; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }
    public String getSectionSummary() { return sectionSummary; }
    public void setSectionSummary(String sectionSummary) { this.sectionSummary = sectionSummary; }
    public short getSectionNumber() { return sectionNumber; }
    public void setSectionNumber(short sectionNumber) { this.sectionNumber = sectionNumber; }
    public short getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(short durationMinutes) { this.durationMinutes = durationMinutes; }
    public NextSection getNextSection() { return nextSection; }
    public void setNextSection(NextSection nextSection) { this.nextSection = nextSection; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public static class NextSection {
        private Long id;
        private String title;
        private short sectionNumber;

        public NextSection() {
        }

        public NextSection(Long id, String title, short sectionNumber) {
            this.id = id;
            this.title = title;
            this.sectionNumber = sectionNumber;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public short getSectionNumber() { return sectionNumber; }
        public void setSectionNumber(short sectionNumber) { this.sectionNumber = sectionNumber; }
    }

    public static class Question {
        private Long id;
        private String questionType;
        private String prompt;
        private short sortOrder;
        private List<Option> options;

        public Question() {
        }

        public Question(Long id, String questionType, String prompt, short sortOrder, List<Option> options) {
            this.id = id;
            this.questionType = questionType;
            this.prompt = prompt;
            this.sortOrder = sortOrder;
            this.options = options;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public short getSortOrder() { return sortOrder; }
        public void setSortOrder(short sortOrder) { this.sortOrder = sortOrder; }
        public List<Option> getOptions() { return options; }
        public void setOptions(List<Option> options) { this.options = options; }
    }

    public static class Option {
        private Long id;
        private String label;
        private String content;

        public Option() {
        }

        public Option(Long id, String label, String content) {
            this.id = id;
            this.label = label;
            this.content = content;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
