CREATE TABLE user_daily_learning_activity (
    user_id          BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_date    DATE        NOT NULL,
    study_seconds    BIGINT      NOT NULL DEFAULT 0,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, activity_date),
    CONSTRAINT chk_user_daily_activity_seconds CHECK (study_seconds >= 0)
);

-- Only one active tracker is kept per learner. This prevents two browser tabs
-- from counting the same wall-clock time twice.
CREATE TABLE user_learning_activity_trackers (
    user_id       BIGINT      PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    session_id    BIGINT      NOT NULL REFERENCES learning_sessions(id) ON DELETE CASCADE,
    last_seen_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_user_daily_learning_activity_date
    ON user_daily_learning_activity(activity_date, user_id);
