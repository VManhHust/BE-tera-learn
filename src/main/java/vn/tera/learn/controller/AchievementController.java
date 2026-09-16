package vn.tera.learn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tera.learn.dto.AchievementResponse;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.AchievementService;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {
    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping
    public ResponseEntity<AchievementResponse> getAchievements(
            @AuthenticationPrincipal JwtClaims claims
    ) {
        return ResponseEntity.ok(achievementService.getAchievements(claims.getUserId()));
    }
}

