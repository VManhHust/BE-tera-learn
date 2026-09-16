ALTER TABLE lessons
    ADD COLUMN free_trial BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX uq_lessons_single_free_trial
    ON lessons (free_trial)
    WHERE free_trial = TRUE;

UPDATE lessons
SET free_trial = TRUE
WHERE slug = 'doc-hieu-van-ban-thong-tin'
  AND published = TRUE;
