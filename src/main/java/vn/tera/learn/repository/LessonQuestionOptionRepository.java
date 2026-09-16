package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.LessonQuestionOption;

import java.util.List;

public interface LessonQuestionOptionRepository extends JpaRepository<LessonQuestionOption, Long> {

    List<LessonQuestionOption> findAllByQuestionIdOrderBySortOrderAsc(Long questionId);
}
