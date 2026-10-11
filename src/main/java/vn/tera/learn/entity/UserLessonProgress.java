package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.tera.learn.entity.enums.LessonProgressStatus;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "user_lesson_progress")
public class UserLessonProgress {

    @EmbeddedId
    private UserLessonId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("lessonId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private short currentUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LessonProgressStatus status = LessonProgressStatus.NOT_STARTED;

    private Instant startedAt;

    private Instant completedAt;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

}
