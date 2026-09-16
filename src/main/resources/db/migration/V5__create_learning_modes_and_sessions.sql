CREATE TABLE learning_modes (
    id                       BIGSERIAL    PRIMARY KEY,
    code                     VARCHAR(30)  NOT NULL UNIQUE,
    name                     VARCHAR(120) NOT NULL,
    description              VARCHAR(500) NOT NULL,
    time_caption             VARCHAR(180) NOT NULL,
    feedback_caption         VARCHAR(180) NOT NULL,
    time_limit_minutes       SMALLINT,
    immediate_feedback       BOOLEAN      NOT NULL,
    accent                   VARCHAR(20)  NOT NULL,
    illustration_key         VARCHAR(30)  NOT NULL,
    active                   BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order               SMALLINT     NOT NULL,
    CONSTRAINT chk_learning_mode_time_limit CHECK (time_limit_minutes IS NULL OR time_limit_minutes > 0),
    CONSTRAINT chk_learning_mode_accent CHECK (accent IN ('VIOLET', 'BLUE'))
);

CREATE TABLE lesson_section_learning_modes (
    id          BIGSERIAL PRIMARY KEY,
    section_id  BIGINT    NOT NULL REFERENCES lesson_sections(id) ON DELETE CASCADE,
    mode_id     BIGINT    NOT NULL REFERENCES learning_modes(id),
    enabled     BOOLEAN   NOT NULL DEFAULT TRUE,
    sort_order  SMALLINT  NOT NULL,
    CONSTRAINT uq_lesson_section_learning_mode UNIQUE (section_id, mode_id),
    CONSTRAINT uq_lesson_section_learning_mode_order UNIQUE (section_id, sort_order)
);

CREATE TABLE learning_sessions (
    id            BIGSERIAL   PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id     BIGINT      NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    section_id    BIGINT      NOT NULL REFERENCES lesson_sections(id) ON DELETE CASCADE,
    mode_id       BIGINT      NOT NULL REFERENCES learning_modes(id),
    status        VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at    TIMESTAMPTZ,
    completed_at  TIMESTAMPTZ,
    CONSTRAINT chk_learning_session_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ABANDONED'))
);

CREATE INDEX idx_section_learning_modes_section ON lesson_section_learning_modes(section_id, enabled, sort_order);
CREATE INDEX idx_learning_sessions_user_status ON learning_sessions(user_id, status, started_at DESC);
CREATE INDEX idx_learning_sessions_lesson_section ON learning_sessions(lesson_id, section_id);

INSERT INTO learning_modes (
    code, name, description, time_caption, feedback_caption, time_limit_minutes,
    immediate_feedback, accent, illustration_key, sort_order
) VALUES
    (
        'SELF_PACED',
        'Học theo nhịp riêng',
        'Thoải mái khám phá từng nội dung và chủ động điều chỉnh tốc độ học.',
        'Không giới hạn thời gian',
        'Học từng phần và xem hướng dẫn ngay',
        NULL,
        TRUE,
        'VIOLET',
        'ORBIT_BOOK',
        1
    ),
    (
        'FOCUSED',
        'Thử thách tập trung',
        'Rèn khả năng tập trung trong một phiên học có thời gian rõ ràng.',
        'Hoàn thành trong 25 phút',
        'Hoàn thành phiên học rồi xem tổng kết',
        25,
        FALSE,
        'BLUE',
        'FOCUS_TARGET',
        2
    );

INSERT INTO lesson_section_learning_modes (section_id, mode_id, enabled, sort_order)
SELECT section.id, mode.id, TRUE, mode.sort_order
FROM lesson_sections section
CROSS JOIN learning_modes mode
WHERE mode.active = TRUE;
