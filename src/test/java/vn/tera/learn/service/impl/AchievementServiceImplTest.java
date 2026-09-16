package vn.tera.learn.service.impl;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AchievementServiceImplTest {
    @Test
    void calculatesXpFromAnswersAccuracyAndCompletedLessons() {
        assertThat(AchievementMath.totalXp(10, 7, 2)).isEqualTo(370);
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

        AchievementMath.StreakStats streak = AchievementMath.streak(dates, today);

        assertThat(streak.current()).isEqualTo(3);
        assertThat(streak.longest()).isEqualTo(3);
    }
}
