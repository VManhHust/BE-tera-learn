package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import vn.tera.learn.entity.UserLessonId;
import vn.tera.learn.entity.UserLessonProgress;
import vn.tera.learn.entity.enums.LessonProgressStatus;

import java.time.Instant;
import java.util.List;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, UserLessonId> {

    List<UserLessonProgress> findAllByIdUserId(Long userId);

    @Query("""
            SELECT progress
            FROM UserLessonProgress progress
            JOIN FETCH progress.lesson lesson
            JOIN FETCH lesson.subject
            WHERE progress.user.id = :userId
              AND progress.status = :status
              AND progress.currentUnit > 0
              AND lesson.published = TRUE
              AND progress.updatedAt >= :cutoff
            ORDER BY progress.updatedAt DESC, lesson.id DESC
            """)
    List<UserLessonProgress> findRecentLearningProgress(
            @Param("userId") Long userId,
            @Param("status") LessonProgressStatus status,
            @Param("cutoff") Instant cutoff,
            Pageable pageable
    );
}
