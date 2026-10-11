ALTER TABLE lessons
    ADD COLUMN plus_required BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE lessons
    ADD CONSTRAINT ck_lessons_free_trial_not_plus_required
        CHECK (NOT (free_trial AND plus_required));

CREATE INDEX idx_lessons_plus_required_published
    ON lessons (published, plus_required)
    WHERE plus_required = TRUE;
