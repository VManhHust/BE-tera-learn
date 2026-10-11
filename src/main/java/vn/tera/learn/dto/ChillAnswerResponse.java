package vn.tera.learn.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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
}
