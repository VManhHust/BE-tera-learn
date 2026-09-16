CREATE TABLE admin_audit_logs (
    id               BIGSERIAL    PRIMARY KEY,
    actor_user_id    BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    actor_email      VARCHAR(255),
    action           VARCHAR(20)  NOT NULL,
    resource         VARCHAR(80)  NOT NULL,
    resource_id      VARCHAR(100),
    http_method      VARCHAR(10)  NOT NULL,
    request_path     VARCHAR(500) NOT NULL,
    query_string     TEXT,
    response_status  INTEGER      NOT NULL,
    success          BOOLEAN      NOT NULL,
    ip_address       VARCHAR(100),
    user_agent       VARCHAR(500),
    details          TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_admin_audit_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE'))
);

CREATE INDEX idx_admin_audit_logs_created_at ON admin_audit_logs(created_at DESC);
CREATE INDEX idx_admin_audit_logs_actor ON admin_audit_logs(actor_user_id, created_at DESC);
CREATE INDEX idx_admin_audit_logs_resource ON admin_audit_logs(resource, resource_id, created_at DESC);
