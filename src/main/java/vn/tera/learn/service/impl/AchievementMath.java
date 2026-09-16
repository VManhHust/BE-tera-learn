package vn.tera.learn.service.impl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class AchievementMath {
    private static final long XP_PER_ANSWER = 10;
    private static final long XP_PER_CORRECT_ANSWER = 10;
    private static final long XP_PER_COMPLETED_LESSON = 100;

    private AchievementMath() {}

    static long totalXp(long answeredQuestions, long correctAnswers, long completedLessons) {
        return answeredQuestions * XP_PER_ANSWER
                + correctAnswers * XP_PER_CORRECT_ANSWER
                + completedLessons * XP_PER_COMPLETED_LESSON;
    }

    static StreakStats streak(List<LocalDate> dates, LocalDate today) {
        if (dates.isEmpty()) return new StreakStats(0, 0);
        Set<LocalDate> dateSet = new HashSet<>(dates);
        LocalDate cursor = dateSet.contains(today) ? today : today.minusDays(1);
        int current = 0;
        while (dateSet.contains(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }

        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate date : dates) {
            run = previous != null && date.equals(previous.plusDays(1)) ? run + 1 : 1;
            longest = Math.max(longest, run);
            previous = date;
        }
        return new StreakStats(current, longest);
    }

    record StreakStats(int current, int longest) {}
}

