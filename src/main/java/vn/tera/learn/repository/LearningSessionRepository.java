package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.LearningSession;

import java.util.Optional;
import java.util.List;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT s FROM LearningSession s WHERE s.id = :id AND s.user.id = :userId")
    Optional<LearningSession> findOwnedForUpdate(
            @org.springframework.data.repository.query.Param("id") Long id,
            @org.springframework.data.repository.query.Param("userId") Long userId);

    Optional<LearningSession> findByIdAndUserId(Long id, Long userId);

    Optional<LearningSession> findByUserIdAndLessonIdAndSectionIdAndModeId(
            Long userId,
            Long lessonId,
            Long sectionId,
            Long modeId
    );

    List<LearningSession> findAllByUserIdAndLessonIdAndModeIdOrderBySectionSortOrderAsc(
            Long userId,
            Long lessonId,
            Long modeId
    );

    List<LearningSession> findAllByUserIdAndLessonIdOrderBySectionSortOrderAsc(
            Long userId,
            Long lessonId
    );
}
