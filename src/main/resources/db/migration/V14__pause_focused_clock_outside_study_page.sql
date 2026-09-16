ALTER TABLE learning_sessions ADD COLUMN focused_remaining_seconds INTEGER;
ALTER TABLE learning_sessions ADD COLUMN focused_resumed_at TIMESTAMPTZ;
ALTER TABLE learning_sessions ADD COLUMN focused_timed_out BOOLEAN NOT NULL DEFAULT FALSE;

-- Freeze existing focused sessions at the time this migration is applied. From
-- now on, their countdown only runs while the focused study page is active.
UPDATE learning_sessions session
SET focused_duration_seconds = COALESCE(
        session.focused_duration_seconds,
        lesson.focused_time_limit_minutes * 60
    ),
    focused_remaining_seconds = CASE
        WHEN session.status = 'IN_PROGRESS' THEN GREATEST(
            0,
            LEAST(
                COALESCE(session.focused_duration_seconds, lesson.focused_time_limit_minutes * 60),
                CASE
                    WHEN session.expires_at IS NULL THEN
                        COALESCE(session.focused_duration_seconds, lesson.focused_time_limit_minutes * 60)
                    ELSE CEIL(EXTRACT(EPOCH FROM (session.expires_at - NOW())))::INTEGER
                END
            )
        )
        ELSE 0
    END,
    focused_resumed_at = NULL,
    focused_timed_out = CASE
        WHEN session.status = 'COMPLETED'
            AND session.completed_at IS NOT NULL
            AND session.expires_at IS NOT NULL
            AND session.completed_at >= session.expires_at
        THEN TRUE
        ELSE FALSE
    END,
    expires_at = NULL
FROM learning_modes mode, lessons lesson
WHERE session.mode_id = mode.id
  AND session.lesson_id = lesson.id
  AND mode.code = 'FOCUSED';

ALTER TABLE learning_sessions ADD CONSTRAINT chk_focused_remaining_seconds
    CHECK (focused_remaining_seconds IS NULL OR focused_remaining_seconds >= 0);
