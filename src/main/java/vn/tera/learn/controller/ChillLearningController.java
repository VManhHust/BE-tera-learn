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
import vn.tera.learn.dto.ChillAnswerResponse;
import vn.tera.learn.dto.ChillLearningSessionResponse;
import vn.tera.learn.dto.SubmitChillAnswerRequest;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.ChillLearningService;

@RestController
@RequestMapping("/api/learning/sessions/{sessionId}/chill")
public class ChillLearningController {

    private final ChillLearningService chillLearningService;

    public ChillLearningController(ChillLearningService chillLearningService) {
        this.chillLearningService = chillLearningService;
    }

    @GetMapping
    public ResponseEntity<ChillLearningSessionResponse> getSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(chillLearningService.getSession(jwtClaims.getUserId(), sessionId));
    }

    @PostMapping("/restart")
    public ResponseEntity<ChillLearningSessionResponse> restartSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(chillLearningService.restartSession(jwtClaims.getUserId(), sessionId));
    }

    @PostMapping("/restart-lesson")
    public ResponseEntity<ChillLearningSessionResponse> restartLesson(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(chillLearningService.restartLesson(jwtClaims.getUserId(), sessionId));
    }

    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<ChillAnswerResponse> submitAnswer(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @Valid @RequestBody SubmitChillAnswerRequest request,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(chillLearningService.submitAnswer(
                jwtClaims.getUserId(),
                sessionId,
                questionId,
                request
        ));
    }
}
