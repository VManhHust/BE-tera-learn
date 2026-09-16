ALTER TABLE users
    ADD COLUMN plus_starts_at TIMESTAMPTZ,
    ADD COLUMN plus_expires_at TIMESTAMPTZ;

CREATE TABLE plus_plan_configs (
    id               BIGSERIAL    PRIMARY KEY,
    code             VARCHAR(20)  NOT NULL UNIQUE,
    name             VARCHAR(120) NOT NULL,
    description      TEXT,
    amount           BIGINT       NOT NULL,
    duration_days    INTEGER,
    benefits         TEXT,
    special_benefit  TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    featured         BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order       INTEGER      NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_plus_plan_amount CHECK (amount > 0),
    CONSTRAINT chk_plus_plan_duration CHECK (duration_days IS NULL OR duration_days > 0),
    CONSTRAINT chk_plus_plan_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE payment_orders (
    id                    UUID         PRIMARY KEY,
    user_id               BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_code             VARCHAR(20)  NOT NULL REFERENCES plus_plan_configs(code),
    payment_code          VARCHAR(20)  NOT NULL UNIQUE,
    amount                BIGINT       NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    expires_at            TIMESTAMPTZ  NOT NULL,
    paid_at               TIMESTAMPTZ,
    plus_starts_at        TIMESTAMPTZ,
    plus_expires_at       TIMESTAMPTZ,
    sepay_transaction_id  BIGINT UNIQUE,
    bank_reference_code   VARCHAR(255),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_payment_order_amount CHECK (amount > 0),
    CONSTRAINT chk_payment_order_status CHECK (status IN ('PENDING', 'PAID', 'EXPIRED'))
);

CREATE INDEX idx_payment_orders_user_status
    ON payment_orders(user_id, status, created_at DESC);
CREATE INDEX idx_payment_orders_user_plan_status
    ON payment_orders(user_id, plan_code, status, expires_at DESC);

CREATE TABLE payment_webhook_events (
    transaction_id  BIGINT       PRIMARY KEY,
    raw_payload     TEXT         NOT NULL,
    received_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO plus_plan_configs (
    code, name, description, amount, duration_days, benefits,
    special_benefit, status, featured, sort_order
) VALUES
    ('MONTHLY', 'Tera Plus 1 tháng', 'Linh hoạt để trải nghiệm toàn bộ kho học', 69000, 30,
     'Học không giới hạn tất cả môn
Mở toàn bộ chế độ học và video dẫn chứng
Theo dõi thành tích chuyên sâu',
     'Phù hợp để bắt đầu', 'ACTIVE', FALSE, 1),
    ('QUARTERLY', 'Tera Plus 3 tháng', 'Duy trì nhịp học trọn một học kỳ', 169000, 90,
     'Học không giới hạn tất cả môn
Mở toàn bộ chế độ học và video dẫn chứng
Theo dõi thành tích chuyên sâu',
     'Tiết kiệm hơn gói tháng', 'ACTIVE', FALSE, 2),
    ('YEARLY', 'Tera Plus 1 năm', 'Đồng hành xuyên suốt năm học và kỳ thi', 499000, 365,
     'Học không giới hạn tất cả môn
Mở toàn bộ chế độ học và video dẫn chứng
Theo dõi thành tích chuyên sâu',
     'Lựa chọn tốt nhất cho học sinh nghiêm túc', 'ACTIVE', TRUE, 3),
    ('LIFETIME', 'Tera Plus trọn đời', 'Một lần thanh toán, học tập dài lâu', 1849000, NULL,
     'Học không giới hạn tất cả môn
Mở toàn bộ chế độ học và video dẫn chứng
Theo dõi thành tích chuyên sâu',
     'Quyền truy cập trọn đời', 'ACTIVE', FALSE, 4);

