CREATE TEMP TABLE learning_session_merge_map ON COMMIT DROP AS
SELECT
    learning_session.id AS source_session_id,
    FIRST_VALUE(learning_session.id) OVER (
        PARTITION BY learning_session.user_id, learning_session.lesson_id, learning_session.section_id, learning_session.mode_id
        ORDER BY learning_session.started_at ASC, learning_session.id ASC
    ) AS canonical_session_id
FROM learning_sessions learning_session;

CREATE TEMP TABLE learning_answer_merge_map ON COMMIT DROP AS
SELECT
    answer.id AS source_answer_id,
    session_map.canonical_session_id,
    FIRST_VALUE(answer.id) OVER (
        PARTITION BY session_map.canonical_session_id, answer.question_id
        ORDER BY answer.submitted_at DESC, answer.id DESC
    ) AS kept_answer_id
FROM learning_session_answers answer
JOIN learning_session_merge_map session_map
    ON session_map.source_session_id = answer.session_id;

DELETE FROM learning_session_answers answer
USING learning_answer_merge_map answer_map
WHERE answer.id = answer_map.source_answer_id
  AND answer_map.source_answer_id <> answer_map.kept_answer_id;

UPDATE learning_session_answers answer
SET session_id = answer_map.canonical_session_id
FROM learning_answer_merge_map answer_map
WHERE answer.id = answer_map.kept_answer_id
  AND answer.session_id <> answer_map.canonical_session_id;

UPDATE learning_sessions canonical
SET
    status = aggregate.status,
    completed_at = aggregate.completed_at,
    expires_at = aggregate.expires_at
FROM (
    SELECT
        session_map.canonical_session_id,
        CASE
            WHEN BOOL_OR(learning_session.status = 'COMPLETED') THEN 'COMPLETED'
            WHEN BOOL_OR(learning_session.status = 'IN_PROGRESS') THEN 'IN_PROGRESS'
            ELSE 'ABANDONED'
        END AS status,
        MAX(learning_session.completed_at) AS completed_at,
        MAX(learning_session.expires_at) AS expires_at
    FROM learning_sessions learning_session
    JOIN learning_session_merge_map session_map
        ON session_map.source_session_id = learning_session.id
    GROUP BY session_map.canonical_session_id
) aggregate
WHERE canonical.id = aggregate.canonical_session_id;

DELETE FROM learning_sessions learning_session
USING learning_session_merge_map session_map
WHERE learning_session.id = session_map.source_session_id
  AND session_map.source_session_id <> session_map.canonical_session_id;

ALTER TABLE learning_sessions
    ADD CONSTRAINT uq_learning_session_user_lesson_section_mode
        UNIQUE (user_id, lesson_id, section_id, mode_id);
