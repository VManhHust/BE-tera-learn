ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_status;

ALTER TABLE users
    ADD CONSTRAINT chk_users_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'LOCK', 'DELETE'));

CREATE TABLE IF NOT EXISTS email_verifications (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    code_hash   VARCHAR(64)  NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    attempts    SMALLINT     NOT NULL DEFAULT 0,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_email_verification_attempts CHECK (attempts >= 0)
);

CREATE INDEX IF NOT EXISTS idx_email_verifications_email_created
    ON email_verifications(email, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_email_verifications_active
    ON email_verifications(email, used, expires_at);
