package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.tera.learn.entity.enums.LessonDifficulty;
import vn.tera.learn.entity.enums.LessonProgram;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(nullable = false)
    private short grade;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 700)
    private String description;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LessonDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LessonProgram program;

    @Column(nullable = false)
    private short totalUnits;

    @Column(nullable = false)
    private int focusedTimeLimitMinutes;

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal averageRating;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "free_trial", nullable = false)
    private boolean freeTrial;

    @Column(name = "plus_required", nullable = false)
    private boolean plusRequired;

    @Column(nullable = false)
    private Instant publishedAt = Instant.now();

}
