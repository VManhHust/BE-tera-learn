package vn.tera.learn.service;

import vn.tera.learn.dto.LessonDetailResponse;
import vn.tera.learn.dto.LessonResourceDownload;

public interface LessonDetailService {

    LessonDetailResponse getLessonDetail(Long userId, Long lessonId);

    LessonResourceDownload downloadResource(Long userId, Long lessonId, Long resourceId);
}
