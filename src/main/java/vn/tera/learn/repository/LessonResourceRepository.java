package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.LessonResource;

import java.util.List;
import java.util.Optional;

public interface LessonResourceRepository extends JpaRepository<LessonResource, Long> {

    List<LessonResource> findAllByLessonIdOrderBySortOrderAsc(Long lessonId);

    Optional<LessonResource> findByIdAndLessonId(Long id, Long lessonId);
}
