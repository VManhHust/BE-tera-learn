package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FreeTrialResponse {
    private Long lessonId;
    private String lessonSlug;
    private String lessonTitle;
    private String lessonDescription;
    private String thumbnailUrl;
    private short grade;
    private String difficulty;
    private String program;
    private boolean plusRequired;
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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextSection {
        private Long id;
        private String title;
        private short sectionNumber;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question {
        private Long id;
        private String questionType;
        private String prompt;
        private short sortOrder;
        private List<Option> options;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private Long id;
        private String label;
        private String content;
    }
}
