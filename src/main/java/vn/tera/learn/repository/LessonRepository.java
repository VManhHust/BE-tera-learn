package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.Lesson;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @EntityGraph(attributePaths = "subject")
    List<Lesson> findAllByPublishedTrueOrderByPublishedAtDesc();

    @EntityGraph(attributePaths = "subject")
    Optional<Lesson> findByIdAndPublishedTrue(Long id);

    @EntityGraph(attributePaths = "subject")
    Optional<Lesson> findByFreeTrialTrueAndPublishedTrue();

    @EntityGraph(attributePaths = "subject")
    List<Lesson> findAllBySubjectCodeAndPublishedTrueOrderByPublishedAtDesc(String subjectCode);

}
