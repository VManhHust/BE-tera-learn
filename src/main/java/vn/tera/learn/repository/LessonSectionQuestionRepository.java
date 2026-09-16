package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.LessonSectionQuestion;

import java.util.List;
import java.util.Optional;

public interface LessonSectionQuestionRepository extends JpaRepository<LessonSectionQuestion, Long> {

    List<LessonSectionQuestion> findAllBySectionLessonIdAndActiveTrueOrderBySectionSortOrderAscSortOrderAsc(Long lessonId);

    List<LessonSectionQuestion> findAllBySectionIdAndActiveTrueOrderBySortOrderAsc(Long sectionId);

    Optional<LessonSectionQuestion> findByIdAndSectionIdAndActiveTrue(Long id, Long sectionId);

    long countBySectionIdAndActiveTrue(Long sectionId);
}
