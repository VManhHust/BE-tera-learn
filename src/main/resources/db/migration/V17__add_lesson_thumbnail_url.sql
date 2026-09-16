ALTER TABLE lessons
    ADD COLUMN thumbnail_url VARCHAR(1000);

COMMENT ON COLUMN lessons.thumbnail_url IS
    'Public image URL used as the lesson card thumbnail; NULL uses the frontend fallback illustration.';
