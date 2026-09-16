package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.UserLessonId;
import vn.tera.learn.entity.UserLessonProgress;

import java.util.List;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, UserLessonId> {

    List<UserLessonProgress> findAllByIdUserId(Long userId);
}
