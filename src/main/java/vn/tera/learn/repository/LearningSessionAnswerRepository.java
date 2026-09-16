package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tera.learn.entity.LearningSessionAnswer;

import java.util.List;
import java.util.Optional;

public interface LearningSessionAnswerRepository extends JpaRepository<LearningSessionAnswer, Long> {

    List<LearningSessionAnswer> findAllBySessionId(Long sessionId);

    Optional<LearningSessionAnswer> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    long countBySessionId(Long sessionId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM LearningSessionAnswer answer WHERE answer.session.id = :sessionId")
    void deleteAllBySessionId(@Param("sessionId") Long sessionId);
}
