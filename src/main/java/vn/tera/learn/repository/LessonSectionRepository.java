package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.LessonSection;

import java.util.List;
import java.util.Optional;

public interface LessonSectionRepository extends JpaRepository<LessonSection, Long> {

    List<LessonSection> findAllByLessonIdOrderBySortOrderAsc(Long lessonId);

    Optional<LessonSection> findByIdAndLessonId(Long id, Long lessonId);

    Optional<LessonSection> findFirstByLessonIdOrderBySortOrderAsc(Long lessonId);

    Optional<LessonSection> findFirstByLessonIdAndSortOrderGreaterThanOrderBySortOrderAsc(
            Long lessonId,
            short sortOrder
    );

    long countByLessonId(Long lessonId);
}
