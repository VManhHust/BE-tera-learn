package vn.tera.learn.dto;

public class LessonBookmarkResponse {

    private Long lessonId;
    private boolean bookmarked;

    public LessonBookmarkResponse() {
    }

    public LessonBookmarkResponse(Long lessonId, boolean bookmarked) {
        this.lessonId = lessonId;
        this.bookmarked = bookmarked;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
}
