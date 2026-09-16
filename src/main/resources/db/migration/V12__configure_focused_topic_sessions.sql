ALTER TABLE lessons ADD COLUMN focused_time_limit_minutes INTEGER;
-- Temporary editable seed: estimate the duration from the topic's question count.
UPDATE lessons lesson
SET focused_time_limit_minutes = GREATEST(5, (
    SELECT COUNT(*) * 2 FROM lesson_section_questions question
    JOIN lesson_sections section ON section.id = question.section_id
    WHERE section.lesson_id = lesson.id AND question.active
));
ALTER TABLE lessons ALTER COLUMN focused_time_limit_minutes SET NOT NULL;
ALTER TABLE lessons ADD CONSTRAINT chk_lesson_focused_minutes
    CHECK (focused_time_limit_minutes BETWEEN 1 AND 1440);

ALTER TABLE learning_sessions ADD COLUMN focused_duration_seconds INTEGER;
ALTER TABLE learning_sessions ADD CONSTRAINT chk_focused_duration
    CHECK (focused_duration_seconds IS NULL OR focused_duration_seconds > 0);

CREATE TABLE focused_session_choices (
    session_id BIGINT NOT NULL REFERENCES learning_sessions(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES lesson_section_questions(id) ON DELETE CASCADE,
    option_id BIGINT NOT NULL REFERENCES lesson_question_options(id) ON DELETE CASCADE,
    PRIMARY KEY (session_id, question_id, option_id)
);

UPDATE learning_modes SET time_caption = 'Đếm ngược theo thời lượng chủ đề',
    time_limit_minutes = NULL WHERE code = 'FOCUSED';
-- Earlier focused sessions only opened a placeholder. Initialize their clocks on entry.
-- No answers or sessions are deleted.
UPDATE learning_sessions session SET expires_at = NULL
FROM learning_modes mode
WHERE session.mode_id = mode.id AND mode.code = 'FOCUSED'
    AND session.status = 'IN_PROGRESS';
