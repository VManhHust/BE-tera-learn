-- Temporary CMS-style configuration used to verify the focused learning flow.
UPDATE lessons
SET focused_time_limit_minutes = 10
WHERE slug = 'doc-hieu-van-ban-thong-tin';

-- Reinitialize unfinished test sessions with the seeded duration on their next entry.
-- Draft choices remain untouched.
UPDATE learning_sessions session
SET
    focused_duration_seconds = NULL,
    expires_at = NULL
FROM lessons lesson, learning_modes mode
WHERE session.lesson_id = lesson.id
  AND session.mode_id = mode.id
  AND lesson.slug = 'doc-hieu-van-ban-thong-tin'
  AND mode.code = 'FOCUSED'
  AND session.status = 'IN_PROGRESS';
