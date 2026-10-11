package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
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
    }
}
