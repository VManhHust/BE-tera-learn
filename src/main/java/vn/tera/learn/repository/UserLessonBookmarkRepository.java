package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.UserLessonBookmark;
import vn.tera.learn.entity.UserLessonId;

import java.util.List;

public interface UserLessonBookmarkRepository extends JpaRepository<UserLessonBookmark, UserLessonId> {

    List<UserLessonBookmark> findAllByIdUserId(Long userId);
}
