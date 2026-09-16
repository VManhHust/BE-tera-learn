package vn.tera.learn.service;

import vn.tera.learn.dto.LessonBookmarkResponse;
import vn.tera.learn.dto.LessonExploreResponse;

public interface LessonExploreService {

    LessonExploreResponse getExploreData(Long userId);

    LessonExploreResponse getSubjectExploreData(Long userId, String subjectCode);

    LessonBookmarkResponse saveBookmark(Long userId, Long lessonId);

    LessonBookmarkResponse removeBookmark(Long userId, Long lessonId);

    LessonExploreResponse.LessonCard startLesson(Long userId, Long lessonId);

    LessonExploreResponse.LessonCard restartLesson(Long userId, Long lessonId);

    LessonExploreResponse.LessonCard updateProgress(Long userId, Long lessonId, short currentUnit);
}
