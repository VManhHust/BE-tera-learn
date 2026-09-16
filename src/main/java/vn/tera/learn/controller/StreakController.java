package vn.tera.learn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tera.learn.dto.StreakResponse;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.StreakService;

@RestController
@RequestMapping("/api/streak")
public class StreakController {
    private final StreakService streakService;

    public StreakController(StreakService streakService) {
        this.streakService = streakService;
    }

    @GetMapping
    public ResponseEntity<StreakResponse> getStatus(@AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(streakService.getStatus(claims.getUserId()));
    }

    @PostMapping("/check-in")
    public ResponseEntity<StreakResponse> checkIn(@AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(streakService.checkIn(claims.getUserId()));
    }
}
