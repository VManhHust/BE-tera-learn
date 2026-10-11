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
import vn.tera.learn.entity.enums.LearningSessionStatus;

import java.time.Instant;

@Entity
@Getter
@Setter
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

}
