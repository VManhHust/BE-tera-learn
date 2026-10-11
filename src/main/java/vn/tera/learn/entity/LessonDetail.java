package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "lesson_details")
public class LessonDetail {

    @Id
    private Long lessonId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(nullable = false, length = 100)
    private String badge;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String introduction;

    @Column(nullable = false, length = 120)
    private String authorName;

    @Column(nullable = false)
    private short estimatedMinutes;

    @Column(nullable = false)
    private Instant updatedAt;

}
