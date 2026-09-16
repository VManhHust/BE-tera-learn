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
import vn.tera.learn.entity.enums.LearningSessionStatus;

import java.time.Instant;

@Entity
@Table(name = "learning_sessions")
public class LearningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private LessonSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mode_id", nullable = false)
    private LearningMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LearningSessionStatus status = LearningSessionStatus.IN_PROGRESS;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant expiresAt;

    private Instant completedAt;

    private Integer focusedDurationSeconds;

    private Integer focusedRemainingSeconds;

    private Instant focusedResumedAt;

    @Column(nullable = false)
    private boolean focusedTimedOut;

    public Integer getFocusedDurationSeconds() { return focusedDurationSeconds; }
    public void setFocusedDurationSeconds(Integer seconds) { this.focusedDurationSeconds = seconds; }
    public Integer getFocusedRemainingSeconds() { return focusedRemainingSeconds; }
    public void setFocusedRemainingSeconds(Integer seconds) { this.focusedRemainingSeconds = seconds; }
    public Instant getFocusedResumedAt() { return focusedResumedAt; }
    public void setFocusedResumedAt(Instant focusedResumedAt) { this.focusedResumedAt = focusedResumedAt; }
    public boolean isFocusedTimedOut() { return focusedTimedOut; }
    public void setFocusedTimedOut(boolean focusedTimedOut) { this.focusedTimedOut = focusedTimedOut; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
    public LessonSection getSection() { return section; }
    public void setSection(LessonSection section) { this.section = section; }
    public LearningMode getMode() { return mode; }
    public void setMode(LearningMode mode) { this.mode = mode; }
    public LearningSessionStatus getStatus() { return status; }
    public void setStatus(LearningSessionStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
