package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LessonExploreResponse {
    private Summary summary;
    private List<SubjectFilter> subjects;
    private List<LessonCard> lessons;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalLessons;
        private int completedLessons;
        private int subjectCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectFilter {
        private String code;
        private String name;
        private String shortName;
        private String accent;
        private long lessonCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LessonCard {
        private Long id;
        private String slug;
        private String subjectCode;
        private String subjectName;
        private String subjectShortName;
        private String accent;
        private short grade;
        private String title;
        private String description;
        private String thumbnailUrl;
        private String difficulty;
        private String program;
        private boolean plusRequired;
        private short totalUnits;
        private short currentUnit;
        private int progressPercent;
        private String status;
        private boolean bookmarked;
        private int viewCount;
        private int likeCount;
        private double averageRating;
        private Instant publishedAt;
    }
}
