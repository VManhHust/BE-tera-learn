CREATE TABLE lesson_details (
    lesson_id          BIGINT       PRIMARY KEY REFERENCES lessons(id) ON DELETE CASCADE,
    badge              VARCHAR(100) NOT NULL,
    introduction       TEXT         NOT NULL,
    author_name        VARCHAR(120) NOT NULL,
    estimated_minutes  SMALLINT     NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_lesson_details_estimated_minutes CHECK (estimated_minutes > 0)
);

CREATE TABLE lesson_objectives (
    id          BIGSERIAL    PRIMARY KEY,
    lesson_id   BIGINT       NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    content     VARCHAR(500) NOT NULL,
    sort_order  SMALLINT     NOT NULL,
    CONSTRAINT uq_lesson_objective_order UNIQUE (lesson_id, sort_order)
);

CREATE TABLE lesson_sections (
    id                BIGSERIAL    PRIMARY KEY,
    lesson_id         BIGINT       NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    title             VARCHAR(220) NOT NULL,
    summary           VARCHAR(700) NOT NULL,
    duration_minutes  SMALLINT     NOT NULL,
    sort_order        SMALLINT     NOT NULL,
    CONSTRAINT uq_lesson_section_order UNIQUE (lesson_id, sort_order),
    CONSTRAINT chk_lesson_section_duration CHECK (duration_minutes > 0)
);

CREATE TABLE lesson_resources (
    id             BIGSERIAL    PRIMARY KEY,
    lesson_id      BIGINT       NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    title          VARCHAR(220) NOT NULL,
    description    VARCHAR(700),
    resource_type  VARCHAR(20)  NOT NULL,
    file_name      VARCHAR(255),
    mime_type      VARCHAR(100),
    content_text   TEXT,
    file_url       VARCHAR(1000),
    downloadable   BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order     SMALLINT     NOT NULL,
    CONSTRAINT uq_lesson_resource_order UNIQUE (lesson_id, sort_order),
    CONSTRAINT chk_lesson_resource_type CHECK (resource_type IN ('DOCUMENT', 'LINK', 'VIDEO'))
);

CREATE INDEX idx_lesson_objectives_lesson ON lesson_objectives(lesson_id, sort_order);
CREATE INDEX idx_lesson_sections_lesson ON lesson_sections(lesson_id, sort_order);
CREATE INDEX idx_lesson_resources_lesson ON lesson_resources(lesson_id, sort_order);

INSERT INTO lesson_details (lesson_id, badge, introduction, author_name, estimated_minutes, updated_at)
SELECT
    lesson.id,
    CASE subject.code
        WHEN 'LITERATURE' THEN 'BÀI HỌC NGỮ VĂN TRỌNG TÂM'
        WHEN 'HISTORY' THEN 'BÀI HỌC LỊCH SỬ TRỌNG TÂM'
        WHEN 'GEOGRAPHY' THEN 'BÀI HỌC ĐỊA LÝ TRỌNG TÂM'
        ELSE 'BÀI HỌC KINH TẾ & PHÁP LUẬT'
    END,
    lesson.description || ' Nội dung được Tera hệ thống thành các phần ngắn, có trọng tâm và gắn với câu hỏi vận dụng để người học dễ theo dõi, ghi nhớ và tự đánh giá tiến độ.',
    'Đội ngũ học thuật Tera',
    lesson.total_units * 12,
    lesson.published_at
FROM lessons lesson
JOIN subjects subject ON subject.id = lesson.subject_id
WHERE lesson.published = TRUE;

INSERT INTO lesson_objectives (lesson_id, content, sort_order)
SELECT lesson.id, objective.content, objective.sort_order
FROM lessons lesson
CROSS JOIN LATERAL (
    VALUES
        (1::SMALLINT, 'Nắm vững hệ thống kiến thức cốt lõi của “' || lesson.title || '”.'),
        (2::SMALLINT, 'Nhận biết mối liên hệ giữa khái niệm, dữ kiện và các tình huống thực tiễn.'),
        (3::SMALLINT, 'Vận dụng kiến thức để giải quyết câu hỏi từ cơ bản đến nâng cao.')
) AS objective(sort_order, content)
WHERE lesson.published = TRUE;

INSERT INTO lesson_sections (lesson_id, title, summary, duration_minutes, sort_order)
SELECT
    lesson.id,
    CASE unit_number
        WHEN 1 THEN 'Khởi động và định hướng bài học'
        WHEN 2 THEN 'Kiến thức nền tảng'
        WHEN 3 THEN 'Nội dung trọng tâm'
        WHEN 4 THEN 'Phân tích và liên hệ'
        ELSE 'Luyện tập chuyên đề ' || (unit_number - 4)
    END,
    CASE unit_number
        WHEN 1 THEN 'Làm quen với mục tiêu, câu hỏi dẫn nhập và bối cảnh của “' || lesson.title || '”.'
        WHEN 2 THEN 'Hệ thống các khái niệm, dữ kiện và kiến thức nền cần ghi nhớ.'
        WHEN 3 THEN 'Đi sâu vào nội dung chính bằng sơ đồ, ví dụ và các điểm dễ nhầm lẫn.'
        WHEN 4 THEN 'Kết nối kiến thức với thực tiễn và rèn luyện cách lập luận, phân tích.'
        ELSE 'Củng cố kiến thức bằng câu hỏi vận dụng và phần tự đánh giá ngắn.'
    END,
    (9 + (unit_number % 4) * 3)::SMALLINT,
    unit_number::SMALLINT
FROM lessons lesson
CROSS JOIN LATERAL generate_series(1, lesson.total_units) AS generated(unit_number)
WHERE lesson.published = TRUE;

INSERT INTO lesson_resources (
    lesson_id, title, description, resource_type, file_name, mime_type,
    content_text, downloadable, sort_order
)
SELECT
    lesson.id,
    'Tài liệu tóm tắt: ' || lesson.title,
    'Bản ghi chú nội dung trọng tâm và mục tiêu ôn tập của bài học.',
    'DOCUMENT',
    lesson.slug || '-tai-lieu.txt',
    'text/plain;charset=UTF-8',
    lesson.title || E'\n' || subject.name || ' - Lớp ' || lesson.grade || E'\n\n' ||
        lesson.description || E'\n\nNỘI DUNG TRỌNG TÂM\n' ||
        '- Kiến thức nền tảng và các khái niệm cần ghi nhớ.' || E'\n' ||
        '- Hệ thống ví dụ, dữ kiện và mối liên hệ thực tiễn.' || E'\n' ||
        '- Câu hỏi vận dụng và hướng dẫn tự đánh giá.' || E'\n\n' ||
        'Tài liệu học tập được cung cấp bởi Tera Learning.',
    TRUE,
    1
FROM lessons lesson
JOIN subjects subject ON subject.id = lesson.subject_id
WHERE lesson.published = TRUE;
