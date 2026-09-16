package vn.tera.learn.dto;

import java.time.Instant;
import java.util.List;

public class ChillAnswerResponse {

    private Long questionId;
    private boolean correct;
    private List<Long> selectedOptionIds;
    private List<Long> correctOptionIds;
    private String explanation;
    private String youtubeTitle;
    private String youtubeUrl;
    private Instant submittedAt;
    private int answeredCount;
    private int totalQuestions;
    private boolean sectionCompleted;

    public ChillAnswerResponse() {
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public List<Long> getSelectedOptionIds() { return selectedOptionIds; }
    public void setSelectedOptionIds(List<Long> selectedOptionIds) { this.selectedOptionIds = selectedOptionIds; }
    public List<Long> getCorrectOptionIds() { return correctOptionIds; }
    public void setCorrectOptionIds(List<Long> correctOptionIds) { this.correctOptionIds = correctOptionIds; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getYoutubeTitle() { return youtubeTitle; }
    public void setYoutubeTitle(String youtubeTitle) { this.youtubeTitle = youtubeTitle; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public int getAnsweredCount() { return answeredCount; }
    public void setAnsweredCount(int answeredCount) { this.answeredCount = answeredCount; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public boolean isSectionCompleted() { return sectionCompleted; }
    public void setSectionCompleted(boolean sectionCompleted) { this.sectionCompleted = sectionCompleted; }
}
