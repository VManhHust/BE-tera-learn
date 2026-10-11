package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {
    private int currentStreak;
    private boolean checkedInToday;
    private long totalCheckIns;
    private LocalDate today;
    private List<LocalDate> checkedInDates;
}
