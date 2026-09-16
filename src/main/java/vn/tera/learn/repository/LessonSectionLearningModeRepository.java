package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.LessonSectionLearningMode;

import java.util.List;
import java.util.Optional;

public interface LessonSectionLearningModeRepository extends JpaRepository<LessonSectionLearningMode, Long> {

    List<LessonSectionLearningMode> findAllBySectionIdAndEnabledTrueOrderBySortOrderAsc(Long sectionId);

    Optional<LessonSectionLearningMode> findBySectionIdAndModeIdAndEnabledTrue(Long sectionId, Long modeId);
}
