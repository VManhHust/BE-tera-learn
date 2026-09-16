package vn.tera.learn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.StreakResponse;
import vn.tera.learn.repository.UserRepository;
import vn.tera.learn.repository.UserStreakCheckInRepository;
import vn.tera.learn.service.StreakService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StreakServiceImpl implements StreakService {
    private static final ZoneId LEARNING_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final UserStreakCheckInRepository streakRepository;
    private final UserRepository userRepository;

    public StreakServiceImpl(UserStreakCheckInRepository streakRepository, UserRepository userRepository) {
        this.streakRepository = streakRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StreakResponse getStatus(Long userId) {
        ensureUserExists(userId);
        return buildResponse(userId, LocalDate.now(LEARNING_ZONE));
    }

    @Override
    @Transactional
    public StreakResponse checkIn(Long userId) {
        ensureUserExists(userId);
        LocalDate today = LocalDate.now(LEARNING_ZONE);
        streakRepository.insertIfAbsent(userId, today);
        return buildResponse(userId, today);
    }

    private StreakResponse buildResponse(Long userId, LocalDate today) {
        List<LocalDate> dates = streakRepository.findCheckInDatesByUserId(userId);
        Set<LocalDate> dateSet = new HashSet<>(dates);
        boolean checkedInToday = dateSet.contains(today);

        LocalDate cursor = checkedInToday ? today : today.minusDays(1);
        int currentStreak = 0;
        while (dateSet.contains(cursor)) {
            currentStreak++;
            cursor = cursor.minusDays(1);
        }

        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<LocalDate> checkedInDates = dates.stream()
                .filter(date -> !date.isBefore(weekStart) && !date.isAfter(weekEnd))
                .sorted()
                .toList();

        return new StreakResponse(
                currentStreak,
                checkedInToday,
                streakRepository.countByUserId(userId),
                today,
                checkedInDates
        );
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
    }
}
