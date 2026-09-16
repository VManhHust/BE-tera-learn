UPDATE user_lesson_progress progress
SET
    current_unit = 0,
    status = 'NOT_STARTED',
    started_at = NULL,
    completed_at = NULL,
    updated_at = NOW()
WHERE progress.status = 'IN_PROGRESS'
  AND NOT EXISTS (
      SELECT 1
      FROM learning_sessions session
      JOIN learning_session_answers answer ON answer.session_id = session.id
      WHERE session.user_id = progress.user_id
        AND session.lesson_id = progress.lesson_id
  );
