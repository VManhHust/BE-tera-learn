package vn.tera.learn.service;

import vn.tera.learn.dto.StreakResponse;

public interface StreakService {
    StreakResponse getStatus(Long userId);
    StreakResponse checkIn(Long userId);
}
