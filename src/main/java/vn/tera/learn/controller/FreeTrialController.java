package vn.tera.learn.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tera.learn.dto.FreeTrialAnswerRequest;
import vn.tera.learn.dto.FreeTrialAnswerResponse;
import vn.tera.learn.dto.FreeTrialResponse;
import vn.tera.learn.service.FreeTrialService;

@RestController
@RequestMapping("/api/public/free-trial")
public class FreeTrialController {

    private final FreeTrialService freeTrialService;

    public FreeTrialController(FreeTrialService freeTrialService) {
        this.freeTrialService = freeTrialService;
    }

    @GetMapping
    public ResponseEntity<FreeTrialResponse> getFreeTrial() {
        return ResponseEntity.ok(freeTrialService.getFreeTrial());
    }

    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<FreeTrialAnswerResponse> submitAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody FreeTrialAnswerRequest request
    ) {
        return ResponseEntity.ok(freeTrialService.submitAnswer(questionId, request));
    }
}
