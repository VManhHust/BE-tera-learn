package vn.tera.learn.service;

import vn.tera.learn.dto.AchievementResponse;

public interface AchievementService {
    AchievementResponse getAchievements(Long userId);
}

