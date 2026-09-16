package vn.tera.learn.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class DashboardRepository {
    private final JdbcTemplate jdbc;

    public DashboardRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public CompletionStats findCompletionStats(Long userId, Instant weekStart, Instant weekEnd) {
        return jdbc.queryForObject("""
                SELECT
                    COUNT(*) FILTER (WHERE status = 'COMPLETED') AS completed_lessons,
                    COUNT(*) FILTER (
                        WHERE status = 'COMPLETED'
                          AND completed_at >= ?
                          AND completed_at < ?
                    ) AS completed_this_week
                FROM user_lesson_progress
                WHERE user_id = ?
                """, (result, row) -> new CompletionStats(
                result.getLong("completed_lessons"),
                result.getLong("completed_this_week")
        ), Timestamp.from(weekStart), Timestamp.from(weekEnd), userId);
    }

    public AnswerStats findAnswerStats(
            Long userId,
            Instant previousWeekStart,
            Instant weekStart,
            Instant weekEnd
    ) {
        return jdbc.queryForObject("""
                SELECT
                    COUNT(*) AS graded_answers,
                    COUNT(*) FILTER (WHERE answer.correct) AS correct_answers,
                    COUNT(*) FILTER (
                        WHERE EXISTS (
                            SELECT 1
                            FROM learning_session_answer_options selected
                            WHERE selected.answer_id = answer.id
                        )
                    ) AS submitted_answers,
                    COUNT(*) FILTER (
                        WHERE answer.submitted_at >= ? AND answer.submitted_at < ?
                    ) AS current_week_answers,
                    COUNT(*) FILTER (
                        WHERE answer.correct
                          AND answer.submitted_at >= ? AND answer.submitted_at < ?
                    ) AS current_week_correct,
                    COUNT(*) FILTER (
                        WHERE answer.submitted_at >= ? AND answer.submitted_at < ?
                    ) AS previous_week_answers,
                    COUNT(*) FILTER (
                        WHERE answer.correct
                          AND answer.submitted_at >= ? AND answer.submitted_at < ?
                    ) AS previous_week_correct
                FROM learning_session_answers answer
                JOIN learning_sessions session ON session.id = answer.session_id
                WHERE session.user_id = ?
                """, (result, row) -> new AnswerStats(
                result.getLong("graded_answers"),
                result.getLong("correct_answers"),
                result.getLong("submitted_answers"),
                result.getLong("current_week_answers"),
                result.getLong("current_week_correct"),
                result.getLong("previous_week_answers"),
                result.getLong("previous_week_correct")
        ),
                Timestamp.from(weekStart), Timestamp.from(weekEnd),
                Timestamp.from(weekStart), Timestamp.from(weekEnd),
                Timestamp.from(previousWeekStart), Timestamp.from(weekStart),
                Timestamp.from(previousWeekStart), Timestamp.from(weekStart),
                userId
        );
    }

    public Optional<ContinueLearningProjection> findContinueLearning(Long userId) {
        return jdbc.query("""
                SELECT
                    lesson.id AS lesson_id,
                    lesson.title,
                    subject.short_name AS subject_name,
                    lesson.grade,
                    progress.current_unit,
                    lesson.total_units
                FROM user_lesson_progress progress
                JOIN lessons lesson ON lesson.id = progress.lesson_id
                JOIN subjects subject ON subject.id = lesson.subject_id
                WHERE progress.user_id = ?
                  AND progress.status = 'IN_PROGRESS'
                  AND progress.current_unit > 0
                  AND lesson.published = TRUE
                ORDER BY progress.updated_at DESC, lesson.id DESC
                LIMIT 1
                """, (result, row) -> new ContinueLearningProjection(
                result.getLong("lesson_id"),
                result.getString("title"),
                result.getString("subject_name"),
                result.getShort("grade"),
                result.getShort("current_unit"),
                result.getShort("total_units")
        ), userId).stream().findFirst();
    }

    public long findTotalStudySeconds(Long userId) {
        Long result = jdbc.queryForObject("""
                SELECT COALESCE(SUM(study_seconds), 0)
                FROM user_daily_learning_activity
                WHERE user_id = ?
                """, Long.class, userId);
        return result == null ? 0 : result;
    }

    public Map<LocalDate, Long> findActivityByDate(Long userId, LocalDate start, LocalDate endExclusive) {
        Map<LocalDate, Long> activity = new LinkedHashMap<>();
        jdbc.query("""
                SELECT activity_date, study_seconds
                FROM user_daily_learning_activity
                WHERE user_id = ?
                  AND activity_date >= ?
                  AND activity_date < ?
                ORDER BY activity_date
                """, result -> {
            activity.put(
                    result.getObject("activity_date", LocalDate.class),
                    result.getLong("study_seconds")
            );
        }, userId, start, endExclusive);
        return activity;
    }

    public java.util.List<SubjectProgressProjection> findSubjectProgress(Long userId) {
        return jdbc.query("""
                SELECT
                    subject.code AS subject_code,
                    COUNT(lesson.id) AS total_lessons,
                    COALESCE(
                        ROUND(
                            100.0 * SUM(
                                LEAST(COALESCE(progress.current_unit, 0), lesson.total_units)
                            ) / NULLIF(SUM(lesson.total_units), 0)
                        ),
                        0
                    ) AS progress_percent
                FROM subjects subject
                LEFT JOIN lessons lesson
                    ON lesson.subject_id = subject.id
                   AND lesson.published = TRUE
                LEFT JOIN user_lesson_progress progress
                    ON progress.lesson_id = lesson.id
                   AND progress.user_id = ?
                WHERE subject.active = TRUE
                GROUP BY subject.id, subject.code, subject.sort_order
                ORDER BY subject.sort_order
                """, (result, row) -> new SubjectProgressProjection(
                result.getString("subject_code"),
                result.getLong("total_lessons"),
                result.getInt("progress_percent")
        ), userId);
    }

    public java.util.List<DailyChallengeProjection> findDailyChallengeCandidates(Long userId) {
        return jdbc.query("""
                SELECT
                    lesson.id AS lesson_id,
                    entry.section_id,
                    entry.mode_id,
                    lesson.title AS lesson_title,
                    subject.short_name AS subject_name,
                    lesson.focused_time_limit_minutes AS duration_minutes,
                    (
                        SELECT COUNT(*)
                        FROM lesson_section_questions question
                        JOIN lesson_sections question_section ON question_section.id = question.section_id
                        WHERE question_section.lesson_id = lesson.id
                          AND question.active = TRUE
                    ) AS question_count,
                    COALESCE(progress.status = 'COMPLETED', FALSE) AS completed,
                    COALESCE(progress.status = 'IN_PROGRESS', FALSE) AS in_progress
                FROM lessons lesson
                JOIN subjects subject ON subject.id = lesson.subject_id
                JOIN LATERAL (
                    SELECT section.id AS section_id, mode.id AS mode_id
                    FROM lesson_sections section
                    JOIN lesson_section_learning_modes section_mode
                        ON section_mode.section_id = section.id
                       AND section_mode.enabled = TRUE
                    JOIN learning_modes mode
                        ON mode.id = section_mode.mode_id
                       AND mode.active = TRUE
                       AND mode.code = 'FOCUSED'
                    WHERE section.lesson_id = lesson.id
                    ORDER BY section.sort_order
                    LIMIT 1
                ) entry ON TRUE
                LEFT JOIN user_lesson_progress progress
                    ON progress.lesson_id = lesson.id
                   AND progress.user_id = ?
                WHERE lesson.published = TRUE
                  AND lesson.focused_time_limit_minutes > 0
                  AND EXISTS (
                      SELECT 1
                      FROM lesson_section_questions question
                      JOIN lesson_sections question_section ON question_section.id = question.section_id
                      WHERE question_section.lesson_id = lesson.id
                        AND question.active = TRUE
                  )
                ORDER BY lesson.id
                """, (result, row) -> new DailyChallengeProjection(
                result.getLong("lesson_id"),
                result.getLong("section_id"),
                result.getLong("mode_id"),
                result.getString("lesson_title"),
                result.getString("subject_name"),
                result.getInt("question_count"),
                result.getInt("duration_minutes"),
                result.getBoolean("completed"),
                result.getBoolean("in_progress")
        ), userId);
    }

    public java.util.List<KnowledgeSpotlightProjection> findKnowledgeSpotlightCandidates() {
        return jdbc.query("""
                SELECT lesson.id, lesson.title, lesson.description
                FROM lessons lesson
                JOIN subjects subject ON subject.id = lesson.subject_id
                WHERE lesson.published = TRUE
                  AND subject.active = TRUE
                ORDER BY lesson.id
                """, (result, row) -> new KnowledgeSpotlightProjection(
                result.getLong("id"),
                result.getString("title"),
                result.getString("description")
        ));
    }

    public static class CompletionStats {
        public final long completedLessons;
        public final long completedThisWeek;

        public CompletionStats(long completedLessons, long completedThisWeek) {
            this.completedLessons = completedLessons;
            this.completedThisWeek = completedThisWeek;
        }
    }

    public static class AnswerStats {
        public final long gradedAnswers;
        public final long correctAnswers;
        public final long submittedAnswers;
        public final long currentWeekAnswers;
        public final long currentWeekCorrect;
        public final long previousWeekAnswers;
        public final long previousWeekCorrect;

        public AnswerStats(
                long gradedAnswers,
                long correctAnswers,
                long submittedAnswers,
                long currentWeekAnswers,
                long currentWeekCorrect,
                long previousWeekAnswers,
                long previousWeekCorrect
        ) {
            this.gradedAnswers = gradedAnswers;
            this.correctAnswers = correctAnswers;
            this.submittedAnswers = submittedAnswers;
            this.currentWeekAnswers = currentWeekAnswers;
            this.currentWeekCorrect = currentWeekCorrect;
            this.previousWeekAnswers = previousWeekAnswers;
            this.previousWeekCorrect = previousWeekCorrect;
        }
    }

    public static class ContinueLearningProjection {
        public final long lessonId;
        public final String title;
        public final String subjectName;
        public final short grade;
        public final short currentUnit;
        public final short totalUnits;

        public ContinueLearningProjection(
                long lessonId,
                String title,
                String subjectName,
                short grade,
                short currentUnit,
                short totalUnits
        ) {
            this.lessonId = lessonId;
            this.title = title;
            this.subjectName = subjectName;
            this.grade = grade;
            this.currentUnit = currentUnit;
            this.totalUnits = totalUnits;
        }
    }

    public static class SubjectProgressProjection {
        public final String subjectCode;
        public final long totalLessons;
        public final int progressPercent;

        public SubjectProgressProjection(String subjectCode, long totalLessons, int progressPercent) {
            this.subjectCode = subjectCode;
            this.totalLessons = totalLessons;
            this.progressPercent = progressPercent;
        }
    }

    public static class DailyChallengeProjection {
        public final long lessonId;
        public final long sectionId;
        public final long modeId;
        public final String lessonTitle;
        public final String subjectName;
        public final int questionCount;
        public final int durationMinutes;
        public final boolean completed;
        public final boolean inProgress;

        public DailyChallengeProjection(
                long lessonId,
                long sectionId,
                long modeId,
                String lessonTitle,
                String subjectName,
                int questionCount,
                int durationMinutes,
                boolean completed,
                boolean inProgress
        ) {
            this.lessonId = lessonId;
            this.sectionId = sectionId;
            this.modeId = modeId;
            this.lessonTitle = lessonTitle;
            this.subjectName = subjectName;
            this.questionCount = questionCount;
            this.durationMinutes = durationMinutes;
            this.completed = completed;
            this.inProgress = inProgress;
        }
    }

    public static class KnowledgeSpotlightProjection {
        public final long lessonId;
        public final String title;
        public final String description;

        public KnowledgeSpotlightProjection(long lessonId, String title, String description) {
            this.lessonId = lessonId;
            this.title = title;
            this.description = description;
        }
    }
}
