package vn.tera.learn.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class FocusedChoiceRepository {
    private final JdbcTemplate jdbc;
    public FocusedChoiceRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Map<Long, Set<Long>> findBySession(Long sessionId) {
        Map<Long, Set<Long>> result = new HashMap<>();
        jdbc.query("SELECT question_id, option_id FROM focused_session_choices WHERE session_id = ?",
                (org.springframework.jdbc.core.RowCallbackHandler) row -> result
                        .computeIfAbsent(row.getLong("question_id"), key -> new LinkedHashSet<>())
                        .add(row.getLong("option_id")), sessionId);
        return result;
    }
    public void replace(Long sessionId, Long questionId, Set<Long> ids) {
        jdbc.update("DELETE FROM focused_session_choices WHERE session_id = ? AND question_id = ?", sessionId, questionId);
        for (Long id : ids) jdbc.update(
                "INSERT INTO focused_session_choices(session_id, question_id, option_id) VALUES (?, ?, ?)",
                sessionId, questionId, id);
    }
    public void deleteBySession(Long sessionId) {
        jdbc.update("DELETE FROM focused_session_choices WHERE session_id = ?", sessionId);
    }
}
