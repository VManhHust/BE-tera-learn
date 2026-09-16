package vn.tera.learn.dto;

import java.time.Instant;
import java.util.List;

public class FocusedSessionResponse {
    public Long sessionId;
    public Long lessonId;
    public String lessonTitle;
    public String status;
    public Instant serverNow;
    public Instant expiresAt;
    public int durationSeconds;
    public long remainingSeconds;
    public boolean timedOut;
    public int correctCount;
    public int answeredCount;
    public List<Question> questions;

    public static class Question {
        public Long id;
        public Long sectionId;
        public short sectionNumber;
        public String sectionTitle;
        public String prompt;
        public String questionType;
        public List<ChillLearningSessionResponse.Option> options;
        public List<Long> selectedOptionIds;
        public ChillAnswerResponse answer;
    }
}
