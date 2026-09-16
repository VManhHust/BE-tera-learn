CREATE TABLE subjects (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    short_name  VARCHAR(50)  NOT NULL,
    accent      VARCHAR(20)  NOT NULL,
    sort_order  SMALLINT     NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE lessons (
    id              BIGSERIAL PRIMARY KEY,
    subject_id      BIGINT        NOT NULL REFERENCES subjects(id),
    slug            VARCHAR(180)  NOT NULL UNIQUE,
    grade           SMALLINT      NOT NULL,
    title           VARCHAR(180)  NOT NULL,
    description     VARCHAR(700)  NOT NULL,
    difficulty      VARCHAR(20)   NOT NULL,
    program         VARCHAR(20)   NOT NULL,
    total_units     SMALLINT      NOT NULL,
    view_count      INTEGER       NOT NULL DEFAULT 0,
    like_count      INTEGER       NOT NULL DEFAULT 0,
    average_rating  NUMERIC(3, 1) NOT NULL DEFAULT 0,
    published       BOOLEAN       NOT NULL DEFAULT TRUE,
    published_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_lessons_grade CHECK (grade BETWEEN 10 AND 12),
    CONSTRAINT chk_lessons_difficulty CHECK (difficulty IN ('BASIC', 'APPLICATION', 'ADVANCED')),
    CONSTRAINT chk_lessons_program CHECK (program IN ('GDPT_2018', 'THPT_EXAM')),
    CONSTRAINT chk_lessons_total_units CHECK (total_units > 0),
    CONSTRAINT chk_lessons_metrics CHECK (view_count >= 0 AND like_count >= 0 AND average_rating BETWEEN 0 AND 10)
);

CREATE TABLE user_lesson_progress (
    user_id       BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id     BIGINT      NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    current_unit  SMALLINT    NOT NULL DEFAULT 0,
    status        VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    started_at    TIMESTAMPTZ,
    completed_at  TIMESTAMPTZ,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, lesson_id),
    CONSTRAINT chk_user_lesson_progress_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT chk_user_lesson_current_unit CHECK (current_unit >= 0)
);

CREATE TABLE user_lesson_bookmarks (
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id   BIGINT      NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, lesson_id)
);

CREATE INDEX idx_lessons_subject_grade ON lessons(subject_id, grade);
CREATE INDEX idx_lessons_published_at ON lessons(published, published_at DESC);
CREATE INDEX idx_user_lesson_progress_user_status ON user_lesson_progress(user_id, status);
CREATE INDEX idx_user_lesson_bookmarks_user ON user_lesson_bookmarks(user_id);

INSERT INTO subjects (code, name, short_name, accent, sort_order) VALUES
    ('LITERATURE', 'Ngữ văn', 'Ngữ văn', 'PINK', 1),
    ('HISTORY', 'Lịch sử', 'Lịch sử', 'ORANGE', 2),
    ('GEOGRAPHY', 'Địa lý', 'Địa lý', 'TEAL', 3),
    ('ECONOMICS_LAW', 'Giáo dục Kinh tế & Pháp luật', 'GDKT & PL', 'INDIGO', 4);

INSERT INTO lessons (
    subject_id, slug, grade, title, description, difficulty, program,
    total_units, view_count, like_count, average_rating, published_at
) VALUES
    ((SELECT id FROM subjects WHERE code = 'HISTORY'), 'khang-chien-chong-thuc-dan-phap', 12,
     'Kháng chiến chống thực dân Pháp', 'Đường lối kháng chiến, các chiến dịch lớn và ý nghĩa lịch sử giai đoạn 1945–1954.',
     'APPLICATION', 'GDPT_2018', 10, 28900, 502, 8.6, NOW() - INTERVAL '2 days'),
    ((SELECT id FROM subjects WHERE code = 'LITERATURE'), 'nghi-luan-ve-mot-van-de-xa-hoi', 12,
     'Nghị luận về một vấn đề xã hội', 'Cách xác định luận điểm, dựng dàn ý và triển khai bài văn nghị luận xã hội thuyết phục.',
     'APPLICATION', 'THPT_EXAM', 8, 19400, 312, 9.1, NOW() - INTERVAL '4 days'),
    ((SELECT id FROM subjects WHERE code = 'GEOGRAPHY'), 'cac-vung-kinh-te-trong-diem', 12,
     'Các vùng kinh tế trọng điểm', 'Đặc điểm tự nhiên, dân cư và thế mạnh phát triển của bốn vùng kinh tế trọng điểm.',
     'APPLICATION', 'GDPT_2018', 9, 10700, 113, 8.2, NOW() - INTERVAL '1 day'),
    ((SELECT id FROM subjects WHERE code = 'ECONOMICS_LAW'), 'quyen-va-nghia-vu-cong-dan', 12,
     'Quyền và nghĩa vụ công dân', 'Các quyền cơ bản, trách nhiệm pháp lý và tình huống thực tiễn trong đời sống.',
     'BASIC', 'GDPT_2018', 7, 14200, 268, 8.8, NOW() - INTERVAL '5 days'),
    ((SELECT id FROM subjects WHERE code = 'HISTORY'), 'viet-nam-1954-1975', 12,
     'Việt Nam 1954–1975', 'Hai miền Nam – Bắc và con đường đi đến thống nhất đất nước.',
     'ADVANCED', 'THPT_EXAM', 12, 22100, 441, 8.9, NOW() - INTERVAL '8 days'),
    ((SELECT id FROM subjects WHERE code = 'LITERATURE'), 'chi-pheo-nam-cao', 11,
     'Chí Phèo – Nam Cao', 'Bi kịch bị tha hóa và giá trị nhân đạo sâu sắc qua hình tượng nhân vật.',
     'APPLICATION', 'GDPT_2018', 6, 31500, 689, 9.3, NOW() - INTERVAL '12 days'),
    ((SELECT id FROM subjects WHERE code = 'GEOGRAPHY'), 'bien-dong-va-vi-tri-chien-luoc', 12,
     'Biển Đông và vị trí chiến lược', 'Mối liên hệ giữa vị trí địa lý, kinh tế biển và chủ quyền quốc gia.',
     'ADVANCED', 'THPT_EXAM', 8, 9800, 207, 8.4, NOW() - INTERVAL '3 days'),
    ((SELECT id FROM subjects WHERE code = 'ECONOMICS_LAW'), 'nen-kinh-te-thi-truong', 11,
     'Nền kinh tế thị trường', 'Cơ chế thị trường, quy luật cung – cầu và vai trò của Nhà nước.',
     'BASIC', 'GDPT_2018', 10, 7600, 96, 8.0, NOW() - INTERVAL '9 days'),
    ((SELECT id FROM subjects WHERE code = 'HISTORY'), 'cach-mang-cong-nghiep-the-gioi', 11,
     'Cách mạng công nghiệp thế giới', 'Các cuộc cách mạng công nghiệp và tác động đến xã hội loài người.',
     'BASIC', 'GDPT_2018', 9, 12300, 158, 8.5, NOW() - INTERVAL '7 days'),
    ((SELECT id FROM subjects WHERE code = 'LITERATURE'), 'doc-hieu-van-ban-thong-tin', 10,
     'Đọc hiểu văn bản thông tin', 'Nhận biết cấu trúc, luận đề và cách khai thác dữ kiện trong văn bản thông tin.',
     'BASIC', 'GDPT_2018', 7, 8300, 124, 8.7, NOW() - INTERVAL '6 hours'),
    ((SELECT id FROM subjects WHERE code = 'GEOGRAPHY'), 'ky-nang-su-dung-atlat', 10,
     'Kỹ năng sử dụng Atlat Địa lý', 'Đọc bản đồ, biểu đồ và phối hợp các trang Atlat để giải quyết câu hỏi thực hành.',
     'APPLICATION', 'THPT_EXAM', 11, 16800, 305, 9.0, NOW() - INTERVAL '10 hours'),
    ((SELECT id FROM subjects WHERE code = 'ECONOMICS_LAW'), 'phap-luat-va-doi-song', 10,
     'Pháp luật và đời sống', 'Vai trò của pháp luật, các hình thức thực hiện và trách nhiệm của công dân.',
     'BASIC', 'GDPT_2018', 8, 6900, 88, 8.1, NOW() - INTERVAL '11 days');
