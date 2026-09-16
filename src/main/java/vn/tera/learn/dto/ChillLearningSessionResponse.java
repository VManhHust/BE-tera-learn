package vn.tera.learn.dto;

import java.time.Instant;
import java.util.List;

public class ChillLearningSessionResponse {

    private Long sessionId;
    private Long lessonId;
    private String lessonTitle;
    private String subjectName;
    private String subjectAccent;
    private Long sectionId;
    private String sectionTitle;
    private short sectionNumber;
    private Long modeId;
    private String modeCode;
    private String modeName;
    private String status;
    private Instant startedAt;
    private Instant expiresAt;
    private int answeredCount;
    private int totalQuestions;
    private Long nextSectionId;
    private Short nextSectionNumber;
    private String nextSectionTitle;
    private List<SectionReview> lessonReviewSections;
    private List<Question> questions;

    public ChillLearningSessionResponse() {
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
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
    public short getSectionNumber() { return sectionNumber; }
    public void setSectionNumber(short sectionNumber) { this.sectionNumber = sectionNumber; }
    public Long getModeId() { return modeId; }
    public void setModeId(Long modeId) { this.modeId = modeId; }
    public String getModeCode() { return modeCode; }
    public void setModeCode(String modeCode) { this.modeCode = modeCode; }
    public String getModeName() { return modeName; }
    public void setModeName(String modeName) { this.modeName = modeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public int getAnsweredCount() { return answeredCount; }
    public void setAnsweredCount(int answeredCount) { this.answeredCount = answeredCount; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public Long getNextSectionId() { return nextSectionId; }
    public void setNextSectionId(Long nextSectionId) { this.nextSectionId = nextSectionId; }
    public Short getNextSectionNumber() { return nextSectionNumber; }
    public void setNextSectionNumber(Short nextSectionNumber) { this.nextSectionNumber = nextSectionNumber; }
    public String getNextSectionTitle() { return nextSectionTitle; }
    public void setNextSectionTitle(String nextSectionTitle) { this.nextSectionTitle = nextSectionTitle; }
    public List<SectionReview> getLessonReviewSections() { return lessonReviewSections; }
    public void setLessonReviewSections(List<SectionReview> lessonReviewSections) { this.lessonReviewSections = lessonReviewSections; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public static class Question {
        private Long id;
        private String questionType;
        private String prompt;
        private short sortOrder;
        private List<Option> options;
        private ChillAnswerResponse answer;

        public Question() {
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
        public ChillAnswerResponse getAnswer() { return answer; }
        public void setAnswer(ChillAnswerResponse answer) { this.answer = answer; }
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

    public static class SectionReview {
        private Long sectionId;
        private short sectionNumber;
        private String sectionTitle;
        private int correctCount;
        private int totalQuestions;
        private List<Question> questions;

        public SectionReview() {
        }

        public Long getSectionId() { return sectionId; }
        public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
        public short getSectionNumber() { return sectionNumber; }
        public void setSectionNumber(short sectionNumber) { this.sectionNumber = sectionNumber; }
        public String getSectionTitle() { return sectionTitle; }
        public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }
        public int getCorrectCount() { return correctCount; }
        public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
        public int getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
        public List<Question> getQuestions() { return questions; }
        public void setQuestions(List<Question> questions) { this.questions = questions; }
    }
}
