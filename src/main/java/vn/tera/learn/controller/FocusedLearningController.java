package vn.tera.learn.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.tera.learn.dto.FocusedSessionResponse;
import vn.tera.learn.dto.SaveFocusedChoiceRequest;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.FocusedLearningService;

@RestController
@RequestMapping("/api/learning/sessions/{sessionId}/focused")
public class FocusedLearningController {
    private final FocusedLearningService service;
    public FocusedLearningController(FocusedLearningService service) { this.service = service; }

    @GetMapping
    public FocusedSessionResponse get(@AuthenticationPrincipal JwtClaims user, @PathVariable Long sessionId) {
        return service.getSession(user.getUserId(), sessionId);
    }
    @PostMapping("/pause")
    public FocusedSessionResponse pause(@AuthenticationPrincipal JwtClaims user, @PathVariable Long sessionId) {
        return service.pause(user.getUserId(), sessionId);
    }
    @PutMapping("/questions/{questionId}/choice")
    public FocusedSessionResponse save(@AuthenticationPrincipal JwtClaims user, @PathVariable Long sessionId,
            @PathVariable Long questionId, @Valid @RequestBody SaveFocusedChoiceRequest request) {
        return service.saveChoice(user.getUserId(), sessionId, questionId, request);
    }
    @PostMapping("/finish")
    public FocusedSessionResponse finish(@AuthenticationPrincipal JwtClaims user, @PathVariable Long sessionId) {
        return service.finish(user.getUserId(), sessionId);
    }
    @PostMapping("/restart")
    public FocusedSessionResponse restart(@AuthenticationPrincipal JwtClaims user, @PathVariable Long sessionId) {
        return service.restart(user.getUserId(), sessionId);
    }
}
