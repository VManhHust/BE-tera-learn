package vn.tera.learn.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FocusedSessionResponse {
    private Long sessionId;
    private Long lessonId;
    private String lessonTitle;
    private String status;
    private Instant serverNow;
    private Instant expiresAt;
    private int durationSeconds;
    private long remainingSeconds;
    private boolean timedOut;
    private int correctCount;
    private int answeredCount;
    private List<Question> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Question {
        private Long id;
        private Long sectionId;
        private short sectionNumber;
        private String sectionTitle;
        private String prompt;
        private String questionType;
        private List<ChillLearningSessionResponse.Option> options;
        private List<Long> selectedOptionIds;
        private ChillAnswerResponse answer;
    }
}
