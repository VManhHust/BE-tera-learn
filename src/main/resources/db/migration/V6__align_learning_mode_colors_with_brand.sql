ALTER TABLE learning_modes
    DROP CONSTRAINT chk_learning_mode_accent;

UPDATE learning_modes
SET accent = CASE code
    WHEN 'SELF_PACED' THEN 'RED'
    WHEN 'FOCUSED' THEN 'CORAL'
    ELSE accent
END;

ALTER TABLE learning_modes
    ADD CONSTRAINT chk_learning_mode_accent CHECK (accent IN ('RED', 'CORAL'));
