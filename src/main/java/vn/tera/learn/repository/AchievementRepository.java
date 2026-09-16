package vn.tera.learn.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AchievementRepository {
    private final JdbcTemplate jdbc;

    public AchievementRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SummarySource findSummary(Long userId) {
        return jdbc.queryForObject("""
                SELECT
                    (SELECT COUNT(*)
                     FROM user_lesson_progress progress
                     WHERE progress.user_id = ? AND progress.status = 'COMPLETED') AS completed_lessons,
                    (SELECT COUNT(DISTINCT session.section_id)
                     FROM learning_sessions session
                     WHERE session.user_id = ? AND session.status = 'COMPLETED') AS completed_sections,
                    (SELECT COUNT(*)
                     FROM learning_session_answers answer
                     JOIN learning_sessions session ON session.id = answer.session_id
                     WHERE session.user_id = ?) AS answered_questions,
                    (SELECT COUNT(*)
                     FROM learning_session_answers answer
                     JOIN learning_sessions session ON session.id = answer.session_id
                     WHERE session.user_id = ? AND answer.correct = TRUE) AS correct_answers,
                    (SELECT COALESCE(SUM(activity.study_seconds), 0)
                     FROM user_daily_learning_activity activity
                     WHERE activity.user_id = ?) AS total_study_seconds
                """, (result, row) -> new SummarySource(
                result.getLong("completed_lessons"),
                result.getLong("completed_sections"),
                result.getLong("answered_questions"),
                result.getLong("correct_answers"),
                result.getLong("total_study_seconds")
        ), userId, userId, userId, userId, userId);
    }

    public List<LocalDate> findLearningDates(Long userId) {
        return jdbc.query("""
                SELECT learning_date
                FROM (
                    SELECT activity_date AS learning_date
                    FROM user_daily_learning_activity
                    WHERE user_id = ? AND study_seconds > 0
                    UNION
                    SELECT (answer.submitted_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS learning_date
                    FROM learning_session_answers answer
                    JOIN learning_sessions session ON session.id = answer.session_id
                    WHERE session.user_id = ?
                    UNION
                    SELECT (progress.completed_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS learning_date
                    FROM user_lesson_progress progress
                    WHERE progress.user_id = ? AND progress.completed_at IS NOT NULL
                ) learning_days
                ORDER BY learning_date
                """, (result, row) -> result.getObject("learning_date", LocalDate.class), userId, userId, userId);
    }

    public List<LocalDate> findStreakDates(Long userId) {
        return jdbc.query("""
                SELECT check_in_date
                FROM user_streak_checkins
                WHERE user_id = ?
                ORDER BY check_in_date
                """, (result, row) -> result.getObject("check_in_date", LocalDate.class), userId);
    }

    public List<WeeklyGrowthSource> findWeeklyGrowth(Long userId, LocalDate start, LocalDate end) {
        return jdbc.query("""
                WITH weeks AS (
                    SELECT generate_series(CAST(? AS date), CAST(? AS date), INTERVAL '1 week')::date AS week_start
                ),
                weekly_study AS (
                    SELECT date_trunc('week', activity.activity_date::timestamp)::date AS week_start,
                           SUM(activity.study_seconds) AS study_seconds
                    FROM user_daily_learning_activity activity
                    WHERE activity.user_id = ?
                      AND activity.activity_date >= ?
                      AND activity.activity_date < ?
                    GROUP BY 1
                ),
                weekly_answers AS (
                    SELECT date_trunc('week', answer.submitted_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS week_start,
                           COUNT(*) AS answered_questions,
                           COUNT(*) FILTER (WHERE answer.correct = TRUE) AS correct_answers
                    FROM learning_session_answers answer
                    JOIN learning_sessions session ON session.id = answer.session_id
                    WHERE session.user_id = ?
                      AND answer.submitted_at >= (?::date AT TIME ZONE 'Asia/Ho_Chi_Minh')
                      AND answer.submitted_at < (?::date AT TIME ZONE 'Asia/Ho_Chi_Minh')
                    GROUP BY 1
                ),
                weekly_lessons AS (
                    SELECT date_trunc('week', progress.completed_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS week_start,
                           COUNT(*) AS completed_lessons
                    FROM user_lesson_progress progress
                    WHERE progress.user_id = ?
                      AND progress.status = 'COMPLETED'
                      AND progress.completed_at >= (?::date AT TIME ZONE 'Asia/Ho_Chi_Minh')
                      AND progress.completed_at < (?::date AT TIME ZONE 'Asia/Ho_Chi_Minh')
                    GROUP BY 1
                )
                SELECT weeks.week_start,
                       COALESCE(weekly_study.study_seconds, 0) AS study_seconds,
                       COALESCE(weekly_answers.answered_questions, 0) AS answered_questions,
                       COALESCE(weekly_answers.correct_answers, 0) AS correct_answers,
                       COALESCE(weekly_lessons.completed_lessons, 0) AS completed_lessons
                FROM weeks
                LEFT JOIN weekly_study USING (week_start)
                LEFT JOIN weekly_answers USING (week_start)
                LEFT JOIN weekly_lessons USING (week_start)
                ORDER BY weeks.week_start
                """, (result, row) -> new WeeklyGrowthSource(
                result.getObject("week_start", LocalDate.class),
                result.getLong("study_seconds"),
                result.getLong("answered_questions"),
                result.getLong("correct_answers"),
                result.getLong("completed_lessons")
        ), Date.valueOf(start), Date.valueOf(end.minusWeeks(1)), userId, Date.valueOf(start), Date.valueOf(end),
                userId, Date.valueOf(start), Date.valueOf(end), userId, Date.valueOf(start), Date.valueOf(end));
    }

    public List<SubjectMasterySource> findSubjectMastery(Long userId) {
        return jdbc.query("""
                WITH lesson_stats AS (
                    SELECT lesson.subject_id,
                           COUNT(lesson.id) AS total_lessons,
                           COUNT(lesson.id) FILTER (WHERE progress.status = 'COMPLETED') AS completed_lessons,
                           COALESCE(SUM(lesson.total_units), 0) AS total_units,
                           COALESCE(SUM(LEAST(COALESCE(progress.current_unit, 0), lesson.total_units)), 0) AS completed_units
                    FROM lessons lesson
                    LEFT JOIN user_lesson_progress progress
                      ON progress.lesson_id = lesson.id AND progress.user_id = ?
                    WHERE lesson.published = TRUE
                    GROUP BY lesson.subject_id
                ),
                answer_stats AS (
                    SELECT lesson.subject_id,
                           COUNT(answer.id) AS answered_questions,
                           COUNT(answer.id) FILTER (WHERE answer.correct = TRUE) AS correct_answers
                    FROM learning_session_answers answer
                    JOIN learning_sessions session ON session.id = answer.session_id
                    JOIN lessons lesson ON lesson.id = session.lesson_id
                    WHERE session.user_id = ?
                    GROUP BY lesson.subject_id
                )
                SELECT subject.code AS subject_code,
                       subject.name AS subject_name,
                       subject.accent,
                       COALESCE(lesson_stats.completed_lessons, 0) AS completed_lessons,
                       COALESCE(lesson_stats.total_lessons, 0) AS total_lessons,
                       COALESCE(lesson_stats.completed_units, 0) AS completed_units,
                       COALESCE(lesson_stats.total_units, 0) AS total_units,
                       COALESCE(answer_stats.answered_questions, 0) AS answered_questions,
                       COALESCE(answer_stats.correct_answers, 0) AS correct_answers
                FROM subjects subject
                LEFT JOIN lesson_stats ON lesson_stats.subject_id = subject.id
                LEFT JOIN answer_stats ON answer_stats.subject_id = subject.id
                WHERE subject.active = TRUE
                ORDER BY subject.sort_order
                """, (result, row) -> new SubjectMasterySource(
                result.getString("subject_code"),
                result.getString("subject_name"),
                result.getString("accent"),
                result.getLong("completed_lessons"),
                result.getLong("total_lessons"),
                result.getLong("completed_units"),
                result.getLong("total_units"),
                result.getLong("answered_questions"),
                result.getLong("correct_answers")
        ), userId, userId);
    }

    public Optional<LessonSuggestionSource> findInProgressLesson(Long userId) {
        return jdbc.query("""
                SELECT lesson.id, lesson.title, subject.code AS subject_code,
                       subject.name AS subject_name, progress.current_unit, lesson.total_units
                FROM user_lesson_progress progress
                JOIN lessons lesson ON lesson.id = progress.lesson_id
                JOIN subjects subject ON subject.id = lesson.subject_id
                WHERE progress.user_id = ?
                  AND progress.status = 'IN_PROGRESS'
                  AND progress.current_unit > 0
                  AND lesson.published = TRUE
                ORDER BY progress.updated_at DESC
                LIMIT 1
                """, (result, row) -> new LessonSuggestionSource(
                result.getLong("id"),
                result.getString("title"),
                result.getString("subject_code"),
                result.getString("subject_name"),
                result.getShort("current_unit"),
                result.getShort("total_units")
        ), userId).stream().findFirst();
    }

    public Optional<LessonSuggestionSource> findRecommendedLesson(Long userId, String subjectCode) {
        return jdbc.query("""
                SELECT lesson.id, lesson.title, subject.code AS subject_code,
                       subject.name AS subject_name, COALESCE(progress.current_unit, 0) AS current_unit,
                       lesson.total_units
                FROM lessons lesson
                JOIN subjects subject ON subject.id = lesson.subject_id
                LEFT JOIN user_lesson_progress progress
                  ON progress.lesson_id = lesson.id AND progress.user_id = ?
                WHERE subject.code = ?
                  AND lesson.published = TRUE
                  AND COALESCE(progress.status, 'NOT_STARTED') = 'NOT_STARTED'
                ORDER BY lesson.difficulty, lesson.published_at DESC, lesson.id
                LIMIT 1
                """, (result, row) -> new LessonSuggestionSource(
                result.getLong("id"),
                result.getString("title"),
                result.getString("subject_code"),
                result.getString("subject_name"),
                result.getShort("current_unit"),
                result.getShort("total_units")
        ), userId, subjectCode).stream().findFirst();
    }

    public void saveSnapshot(Long userId, AchievementSnapshot snapshot) {
        jdbc.update("""
                INSERT INTO user_achievement_stats(
                    user_id, total_xp, current_level, rank_name, completed_lessons,
                    completed_sections, answered_questions, correct_answers, accuracy_percent,
                    total_study_seconds, active_days, current_streak_days, longest_streak_days,
                    last_activity_date, calculated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET
                    total_xp = EXCLUDED.total_xp,
                    current_level = EXCLUDED.current_level,
                    rank_name = EXCLUDED.rank_name,
                    completed_lessons = EXCLUDED.completed_lessons,
                    completed_sections = EXCLUDED.completed_sections,
                    answered_questions = EXCLUDED.answered_questions,
                    correct_answers = EXCLUDED.correct_answers,
                    accuracy_percent = EXCLUDED.accuracy_percent,
                    total_study_seconds = EXCLUDED.total_study_seconds,
                    active_days = EXCLUDED.active_days,
                    current_streak_days = EXCLUDED.current_streak_days,
                    longest_streak_days = EXCLUDED.longest_streak_days,
                    last_activity_date = EXCLUDED.last_activity_date,
                    calculated_at = EXCLUDED.calculated_at
                """, userId, snapshot.totalXp(), snapshot.currentLevel(), snapshot.rankName(),
                snapshot.completedLessons(), snapshot.completedSections(), snapshot.answeredQuestions(),
                snapshot.correctAnswers(), snapshot.accuracyPercent(), snapshot.totalStudySeconds(),
                snapshot.activeDays(), snapshot.currentStreakDays(), snapshot.longestStreakDays(),
                snapshot.lastActivityDate() == null ? null : Date.valueOf(snapshot.lastActivityDate()),
                Timestamp.from(snapshot.calculatedAt()));
    }

    public record SummarySource(
            long completedLessons,
            long completedSections,
            long answeredQuestions,
            long correctAnswers,
            long totalStudySeconds
    ) {}

    public record WeeklyGrowthSource(
            LocalDate weekStart,
            long studySeconds,
            long answeredQuestions,
            long correctAnswers,
            long completedLessons
    ) {}

    public record SubjectMasterySource(
            String subjectCode,
            String subjectName,
            String accent,
            long completedLessons,
            long totalLessons,
            long completedUnits,
            long totalUnits,
            long answeredQuestions,
            long correctAnswers
    ) {}

    public record LessonSuggestionSource(
            long lessonId,
            String title,
            String subjectCode,
            String subjectName,
            short currentUnit,
            short totalUnits
    ) {}

    public record AchievementSnapshot(
            long totalXp,
            int currentLevel,
            String rankName,
            long completedLessons,
            long completedSections,
            long answeredQuestions,
            long correctAnswers,
            double accuracyPercent,
            long totalStudySeconds,
            int activeDays,
            int currentStreakDays,
            int longestStreakDays,
            LocalDate lastActivityDate,
            Instant calculatedAt
    ) {}
}

