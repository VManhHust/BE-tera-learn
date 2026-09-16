package vn.tera.learn.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tera.learn.dto.LessonBookmarkResponse;
import vn.tera.learn.dto.LessonExploreResponse;
import vn.tera.learn.dto.UpdateLessonProgressRequest;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.LessonExploreService;

@RestController
@RequestMapping("/api/learning")
public class LessonExploreController {

    private final LessonExploreService lessonExploreService;

    public LessonExploreController(LessonExploreService lessonExploreService) {
        this.lessonExploreService = lessonExploreService;
    }

    @GetMapping("/explore")
    public ResponseEntity<LessonExploreResponse> getExploreData(
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(lessonExploreService.getExploreData(jwtClaims.getUserId()));
    }

    @GetMapping("/subjects/literature/lessons")
    public ResponseEntity<LessonExploreResponse> getLiteratureLessons(
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return getSubjectLessons(jwtClaims, "LITERATURE");
    }

    @GetMapping("/subjects/history/lessons")
    public ResponseEntity<LessonExploreResponse> getHistoryLessons(
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return getSubjectLessons(jwtClaims, "HISTORY");
    }

    @GetMapping("/subjects/geography/lessons")
    public ResponseEntity<LessonExploreResponse> getGeographyLessons(
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return getSubjectLessons(jwtClaims, "GEOGRAPHY");
    }

    @GetMapping("/subjects/economics-law/lessons")
    public ResponseEntity<LessonExploreResponse> getEconomicsLawLessons(
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return getSubjectLessons(jwtClaims, "ECONOMICS_LAW");
    }

    private ResponseEntity<LessonExploreResponse> getSubjectLessons(JwtClaims jwtClaims, String subjectCode) {
        return ResponseEntity.ok(lessonExploreService.getSubjectExploreData(
                jwtClaims.getUserId(),
                subjectCode
        ));
    }

    @PutMapping("/lessons/{lessonId}/bookmark")
    public ResponseEntity<LessonBookmarkResponse> saveBookmark(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(lessonExploreService.saveBookmark(jwtClaims.getUserId(), lessonId));
    }

    @DeleteMapping("/lessons/{lessonId}/bookmark")
    public ResponseEntity<LessonBookmarkResponse> removeBookmark(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(lessonExploreService.removeBookmark(jwtClaims.getUserId(), lessonId));
    }

    @PostMapping("/lessons/{lessonId}/start")
    public ResponseEntity<LessonExploreResponse.LessonCard> startLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(lessonExploreService.startLesson(jwtClaims.getUserId(), lessonId));
    }

    @PostMapping("/lessons/{lessonId}/restart")
    public ResponseEntity<LessonExploreResponse.LessonCard> restartLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(lessonExploreService.restartLesson(jwtClaims.getUserId(), lessonId));
    }

    @PutMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<LessonExploreResponse.LessonCard> updateProgress(
            @PathVariable Long lessonId,
            @Valid @RequestBody UpdateLessonProgressRequest request,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(lessonExploreService.updateProgress(
                jwtClaims.getUserId(),
                lessonId,
                request.getCurrentUnit()
        ));
    }
}
