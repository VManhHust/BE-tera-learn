package vn.tera.learn.service.impl;

import org.junit.jupiter.api.Test;
import vn.tera.learn.dto.StreakStats;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AchievementServiceImplTest {
    private final AchievementMath achievementMath = new AchievementMath();

    @Test
    void calculatesXpFromAnswersAccuracyAndCompletedLessons() {
        assertThat(achievementMath.totalXp(10, 7, 2)).isEqualTo(370);
    }

    @Test
    void calculatesCurrentAndLongestStreakWithoutAFrameworkMock() {
        LocalDate today = LocalDate.of(2026, 9, 11);
        List<LocalDate> dates = List.of(
                today.minusDays(6),
                today.minusDays(5),
                today.minusDays(2),
                today.minusDays(1),
                today
        );

        StreakStats streak = achievementMath.streak(dates, today);

        assertThat(streak.getCurrent()).isEqualTo(3);
        assertThat(streak.getLongest()).isEqualTo(3);
    }
}
