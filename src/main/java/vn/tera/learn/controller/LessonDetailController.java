package vn.tera.learn.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tera.learn.dto.LessonDetailResponse;
import vn.tera.learn.dto.LessonResourceDownload;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.LessonDetailService;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/learning/lessons")
public class LessonDetailController {

    private final LessonDetailService lessonDetailService;

    public LessonDetailController(LessonDetailService lessonDetailService) {
        this.lessonDetailService = lessonDetailService;
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonDetailResponse> getLessonDetail(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        return ResponseEntity.ok(lessonDetailService.getLessonDetail(jwtClaims.getUserId(), lessonId));
    }

    @GetMapping("/{lessonId}/resources/{resourceId}/download")
    public ResponseEntity<byte[]> downloadResource(
            @PathVariable Long lessonId,
            @PathVariable Long resourceId,
            @AuthenticationPrincipal JwtClaims jwtClaims
    ) {
        LessonResourceDownload download = lessonDetailService.downloadResource(
                jwtClaims.getUserId(),
                lessonId,
                resourceId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(download.getMimeType()));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(download.getFileName(), StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(download.getContent().length)
                .body(download.getContent());
    }
}
