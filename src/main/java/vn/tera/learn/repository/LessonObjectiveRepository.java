package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.LessonObjective;

import java.util.List;

public interface LessonObjectiveRepository extends JpaRepository<LessonObjective, Long> {

    List<LessonObjective> findAllByLessonIdOrderBySortOrderAsc(Long lessonId);
}
