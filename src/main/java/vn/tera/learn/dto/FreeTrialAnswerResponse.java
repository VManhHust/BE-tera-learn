package vn.tera.learn.dto;

import java.util.List;

public class FreeTrialAnswerResponse {

    private Long questionId;
    private boolean correct;
    private List<Long> selectedOptionIds;
    private List<Long> correctOptionIds;
    private String explanation;
    private String youtubeTitle;
    private String youtubeUrl;

    public FreeTrialAnswerResponse() {
    }

    public FreeTrialAnswerResponse(
            Long questionId,
            boolean correct,
            List<Long> selectedOptionIds,
            List<Long> correctOptionIds,
            String explanation,
            String youtubeTitle,
            String youtubeUrl
    ) {
        this.questionId = questionId;
        this.correct = correct;
        this.selectedOptionIds = selectedOptionIds;
        this.correctOptionIds = correctOptionIds;
        this.explanation = explanation;
        this.youtubeTitle = youtubeTitle;
        this.youtubeUrl = youtubeUrl;
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
}
