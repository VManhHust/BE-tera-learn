package vn.tera.learn.dto;

import java.time.LocalDate;
import java.util.List;

public class StreakResponse {
    private int currentStreak;
    private boolean checkedInToday;
    private long totalCheckIns;
    private LocalDate today;
    private List<LocalDate> checkedInDates;

    public StreakResponse() {
    }

    public StreakResponse(int currentStreak, boolean checkedInToday, long totalCheckIns,
            LocalDate today, List<LocalDate> checkedInDates) {
        this.currentStreak = currentStreak;
        this.checkedInToday = checkedInToday;
        this.totalCheckIns = totalCheckIns;
        this.today = today;
        this.checkedInDates = checkedInDates;
    }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public boolean isCheckedInToday() { return checkedInToday; }
    public void setCheckedInToday(boolean checkedInToday) { this.checkedInToday = checkedInToday; }
    public long getTotalCheckIns() { return totalCheckIns; }
    public void setTotalCheckIns(long totalCheckIns) { this.totalCheckIns = totalCheckIns; }
    public LocalDate getToday() { return today; }
    public void setToday(LocalDate today) { this.today = today; }
    public List<LocalDate> getCheckedInDates() { return checkedInDates; }
    public void setCheckedInDates(List<LocalDate> checkedInDates) { this.checkedInDates = checkedInDates; }
}
