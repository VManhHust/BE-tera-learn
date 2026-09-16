CREATE TABLE lesson_section_questions (
    id             BIGSERIAL    PRIMARY KEY,
    section_id     BIGINT       NOT NULL REFERENCES lesson_sections(id) ON DELETE CASCADE,
    question_type  VARCHAR(30)  NOT NULL,
    prompt         TEXT         NOT NULL,
    explanation    TEXT         NOT NULL,
    youtube_title  VARCHAR(255),
    youtube_url    VARCHAR(1000),
    sort_order     SMALLINT     NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_lesson_section_question_order UNIQUE (section_id, sort_order),
    CONSTRAINT chk_lesson_question_type CHECK (question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE'))
);

CREATE TABLE lesson_question_options (
    id           BIGSERIAL   PRIMARY KEY,
    question_id  BIGINT      NOT NULL REFERENCES lesson_section_questions(id) ON DELETE CASCADE,
    label        VARCHAR(5)  NOT NULL,
    content      TEXT        NOT NULL,
    correct      BOOLEAN     NOT NULL DEFAULT FALSE,
    sort_order   SMALLINT    NOT NULL,
    CONSTRAINT uq_lesson_question_option_label UNIQUE (question_id, label),
    CONSTRAINT uq_lesson_question_option_order UNIQUE (question_id, sort_order)
);

CREATE TABLE learning_session_answers (
    id            BIGSERIAL   PRIMARY KEY,
    session_id    BIGINT      NOT NULL REFERENCES learning_sessions(id) ON DELETE CASCADE,
    question_id   BIGINT      NOT NULL REFERENCES lesson_section_questions(id) ON DELETE CASCADE,
    correct       BOOLEAN     NOT NULL,
    submitted_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_learning_session_question_answer UNIQUE (session_id, question_id)
);

CREATE TABLE learning_session_answer_options (
    answer_id  BIGINT NOT NULL REFERENCES learning_session_answers(id) ON DELETE CASCADE,
    option_id  BIGINT NOT NULL REFERENCES lesson_question_options(id) ON DELETE CASCADE,
    PRIMARY KEY (answer_id, option_id)
);

CREATE INDEX idx_lesson_questions_section ON lesson_section_questions(section_id, active, sort_order);
CREATE INDEX idx_lesson_question_options_question ON lesson_question_options(question_id, sort_order);
CREATE INDEX idx_learning_session_answers_session ON learning_session_answers(session_id, submitted_at);

INSERT INTO lesson_section_questions (
    section_id, question_type, prompt, explanation, youtube_title, youtube_url, sort_order
)
SELECT
    section.id,
    'SINGLE_CHOICE',
    question.prompt,
    question.explanation,
    CASE subject.code
        WHEN 'HISTORY' THEN 'Video tổng ôn kiến thức Lịch sử'
        WHEN 'GEOGRAPHY' THEN 'Video bài giảng Địa lí'
        WHEN 'ECONOMICS_LAW' THEN 'Video bài giảng Giáo dục Kinh tế và Pháp luật'
        ELSE 'Video bài giảng Ngữ văn'
    END,
    CASE subject.code
        WHEN 'HISTORY' THEN 'https://www.youtube.com/watch?v=QowLk9S3sUo'
        WHEN 'GEOGRAPHY' THEN 'https://www.youtube.com/watch?v=oExvETZ0Zj8'
        WHEN 'ECONOMICS_LAW' THEN 'https://www.youtube.com/watch?v=jsw3ZYMzvaA'
        ELSE 'https://www.youtube.com/results?search_query=ngu+van+10+van+ban+thong+tin'
    END,
    question.sort_order
FROM lesson_sections section
JOIN lessons lesson ON lesson.id = section.lesson_id
JOIN subjects subject ON subject.id = lesson.subject_id
CROSS JOIN LATERAL (
    VALUES
        (
            1::SMALLINT,
            'Nội dung nào phản ánh đúng nhất trọng tâm của phần “' || section.title || '”?',
            'Phương án đúng diễn đạt trực tiếp nội dung trọng tâm đã được xác định cho phần học “' || section.title || '”. Các phương án còn lại chỉ mô tả cách học máy móc hoặc tách kiến thức khỏi bối cảnh.'
        ),
        (
            2::SMALLINT,
            'Cách tiếp cận nào phù hợp nhất để học hiệu quả phần “' || section.title || '”?',
            'Một tiến trình học hiệu quả cần bắt đầu bằng việc xác định mục tiêu, đọc nội dung trọng tâm, đối chiếu ví dụ rồi tự kiểm tra mức độ hiểu.'
        ),
        (
            3::SMALLINT,
            'Thông tin nào cần được ưu tiên khi liên hệ phần học này với toàn bộ bài “' || lesson.title || '”?',
            'Phần học cần được đặt trong mục tiêu chung của bài. Vì vậy, mô tả tổng quan của bài là căn cứ phù hợp nhất để tạo mối liên hệ kiến thức.'
        ),
        (
            4::SMALLINT,
            'Sau khi hoàn thành phần học, hoạt động nào giúp bạn tự đánh giá mức độ hiểu bài tốt nhất?',
            'Tự trình bày lại ý chính và giải thích bằng ví dụ là cách kiểm tra khả năng hiểu, ghi nhớ và vận dụng; hiệu quả hơn việc chỉ đọc lại hoặc học thuộc từ khóa.'
        )
) AS question(sort_order, prompt, explanation)
WHERE lesson.published = TRUE;

INSERT INTO lesson_question_options (question_id, label, content, correct, sort_order)
SELECT question.id, option.label, option.content, option.correct, option.sort_order
FROM lesson_section_questions question
JOIN lesson_sections section ON section.id = question.section_id
JOIN lessons lesson ON lesson.id = section.lesson_id
CROSS JOIN LATERAL (
    VALUES
        (
            'A',
            CASE question.sort_order
                WHEN 1 THEN section.summary
                WHEN 2 THEN 'Đọc thật nhanh toàn bộ nội dung mà không cần xác định mục tiêu.'
                WHEN 3 THEN 'Chỉ ghi nhớ tên của phần học, không cần liên hệ với bài.'
                ELSE 'Đọc lại nguyên văn nhiều lần mà không tự diễn đạt.'
            END,
            question.sort_order = 1,
            1::SMALLINT
        ),
        (
            'B',
            CASE question.sort_order
                WHEN 1 THEN 'Chỉ ghi nhớ tên phần học và bỏ qua các mối liên hệ.'
                WHEN 2 THEN 'Xác định mục tiêu, đọc trọng tâm, đối chiếu ví dụ và tự kiểm tra.'
                WHEN 3 THEN 'Chỉ tập trung vào số thứ tự của phần trong lộ trình.'
                ELSE 'Ghi nhớ một vài từ khóa nhưng không giải thích ý nghĩa.'
            END,
            question.sort_order = 2,
            2::SMALLINT
        ),
        (
            'C',
            CASE question.sort_order
                WHEN 1 THEN 'Tách nội dung khỏi bối cảnh của toàn bộ bài học.'
                WHEN 2 THEN 'Học thuộc từng câu trước khi đọc phần giải thích.'
                WHEN 3 THEN lesson.description
                ELSE 'Bỏ qua câu hỏi vận dụng vì đã đọc xong nội dung.'
            END,
            question.sort_order = 3,
            3::SMALLINT
        ),
        (
            'D',
            CASE question.sort_order
                WHEN 1 THEN 'Bỏ qua nội dung chính và chỉ làm câu hỏi nâng cao.'
                WHEN 2 THEN 'Chỉ xem đáp án mà không tự lựa chọn phương án.'
                WHEN 3 THEN 'Xem phần học như một nội dung hoàn toàn độc lập.'
                ELSE 'Tự trình bày ý chính, giải thích bằng ví dụ và đối chiếu mục tiêu.'
            END,
            question.sort_order = 4,
            4::SMALLINT
        )
) AS option(label, content, correct, sort_order);
