CREATE TABLE user_achievement_stats (
    user_id               BIGINT       PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    total_xp              BIGINT       NOT NULL DEFAULT 0,
    current_level         INTEGER      NOT NULL DEFAULT 1,
    rank_name             VARCHAR(80)  NOT NULL DEFAULT 'Người khám phá',
    completed_lessons     INTEGER      NOT NULL DEFAULT 0,
    completed_sections    INTEGER      NOT NULL DEFAULT 0,
    answered_questions    INTEGER      NOT NULL DEFAULT 0,
    correct_answers       INTEGER      NOT NULL DEFAULT 0,
    accuracy_percent      NUMERIC(5,2) NOT NULL DEFAULT 0,
    total_study_seconds   BIGINT       NOT NULL DEFAULT 0,
    active_days           INTEGER      NOT NULL DEFAULT 0,
    current_streak_days   INTEGER      NOT NULL DEFAULT 0,
    longest_streak_days   INTEGER      NOT NULL DEFAULT 0,
    last_activity_date    DATE,
    calculated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_user_achievement_non_negative CHECK (
        total_xp >= 0
        AND current_level >= 1
        AND completed_lessons >= 0
        AND completed_sections >= 0
        AND answered_questions >= 0
        AND correct_answers >= 0
        AND total_study_seconds >= 0
        AND active_days >= 0
        AND current_streak_days >= 0
        AND longest_streak_days >= 0
    ),
    CONSTRAINT chk_user_achievement_accuracy CHECK (accuracy_percent BETWEEN 0 AND 100)
);

COMMENT ON TABLE user_achievement_stats IS 'Latest calculated achievement snapshot; one row per learner';

