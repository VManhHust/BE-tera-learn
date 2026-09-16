package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.LessonDetail;

public interface LessonDetailRepository extends JpaRepository<LessonDetail, Long> {
}
