package vn.tera.learn.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tera.learn.dto.DashboardResponse;
import vn.tera.learn.dto.StudyActivityRequest;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.DashboardService;
import vn.tera.learn.service.StudyActivityService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    private final StudyActivityService studyActivityService;

    public DashboardController(
            DashboardService dashboardService,
            StudyActivityService studyActivityService
    ) {
        this.dashboardService = dashboardService;
        this.studyActivityService = studyActivityService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(dashboardService.getDashboard(claims.getUserId()));
    }

    @PostMapping("/activity/heartbeat")
    public ResponseEntity<Void> heartbeat(
            @AuthenticationPrincipal JwtClaims claims,
            @Valid @RequestBody StudyActivityRequest request
    ) {
        studyActivityService.heartbeat(claims.getUserId(), request.getSessionId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/activity/pause")
    public ResponseEntity<Void> pause(
            @AuthenticationPrincipal JwtClaims claims,
            @Valid @RequestBody StudyActivityRequest request
    ) {
        studyActivityService.pause(claims.getUserId(), request.getSessionId());
        return ResponseEntity.noContent().build();
    }
}
