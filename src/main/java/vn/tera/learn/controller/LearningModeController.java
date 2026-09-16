package vn.tera.learn.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tera.learn.dto.LearningModeSelectionResponse;
import vn.tera.learn.dto.LearningSessionResponse;
import vn.tera.learn.dto.StartLearningSessionRequest;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.LearningModeService;

@RestController
@RequestMapping("/api/learning/lessons/{lessonId}/sections/{sectionId}")
public class LearningModeController {

    private final LearningModeService learningModeService;

    public LearningModeController(LearningModeService learningModeService) {
        this.learningModeService = learningModeService;
    }

    @GetMapping("/learning-modes")
    public ResponseEntity<LearningModeSelectionResponse> getLearningModes(
            @PathVariable Long lessonId,
            @PathVariable Long sectionId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(learningModeService.getLearningModes(
                jwtClaims.getUserId(),
                lessonId,
                sectionId
        ));
    }

    @PostMapping("/sessions")
    public ResponseEntity<LearningSessionResponse> startSession(
            @PathVariable Long lessonId,
            @PathVariable Long sectionId,
            @Valid @RequestBody StartLearningSessionRequest request,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(learningModeService.startSession(
                jwtClaims.getUserId(),
                lessonId,
                sectionId,
                request
        ));
    }
}
