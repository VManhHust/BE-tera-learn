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
import vn.tera.learn.entity.enums.LessonProgressStatus;

import java.time.Instant;

@Entity
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

    public UserLessonId getId() {
        return id;
    }

    public void setId(UserLessonId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public short getCurrentUnit() {
        return currentUnit;
    }

    public void setCurrentUnit(short currentUnit) {
        this.currentUnit = currentUnit;
    }

    public LessonProgressStatus getStatus() {
        return status;
    }

    public void setStatus(LessonProgressStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
