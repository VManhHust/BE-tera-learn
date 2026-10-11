package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Question {
        private Long id;
        private String questionType;
        private String prompt;
        private short sortOrder;
        private List<Option> options;
        private ChillAnswerResponse answer;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private Long id;
        private String label;
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SectionReview {
        private Long sectionId;
        private short sectionNumber;
        private String sectionTitle;
        private int correctCount;
        private int totalQuestions;
        private List<Question> questions;
    }
}
