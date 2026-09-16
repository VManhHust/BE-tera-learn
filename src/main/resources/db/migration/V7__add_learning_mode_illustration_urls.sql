ALTER TABLE learning_modes
    ADD COLUMN illustration_url VARCHAR(255);

UPDATE learning_modes
SET illustration_url = CASE code
    WHEN 'SELF_PACED' THEN '/images/learning-modes/self-paced-tera-mascot-v2.png'
    WHEN 'FOCUSED' THEN '/images/learning-modes/focused-tera-mascot-v4.png'
    ELSE '/images/learning-modes/self-paced-tera-mascot-v2.png'
END;

ALTER TABLE learning_modes
    ALTER COLUMN illustration_url SET NOT NULL;
