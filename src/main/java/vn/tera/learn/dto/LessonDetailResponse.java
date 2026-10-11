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
public class LessonDetailResponse {
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
    private int viewCount;
    private int likeCount;
    private double averageRating;
    private Instant publishedAt;
    private String badge;
    private String introduction;
    private String authorName;
    private short estimatedMinutes;
    private Instant updatedAt;
    private List<String> objectives;
    private List<Section> sections;
    private List<Resource> resources;
    private Progress progress;

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class Section {
        private Long id;
        private String title;
        private String summary;
        private short durationMinutes;
        private short sortOrder;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class Resource {
        private Long id;
        private String title;
        private String description;
        private String resourceType;
        private String fileName;
        private boolean downloadable;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class Progress {
        private String status;
        private short currentUnit;
        private int progressPercent;
        private boolean bookmarked;
    }
}
