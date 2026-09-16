package vn.tera.learn.service;

import vn.tera.learn.dto.FocusedSessionResponse;
import vn.tera.learn.dto.SaveFocusedChoiceRequest;

public interface FocusedLearningService {
    FocusedSessionResponse getSession(Long userId, Long sessionId);
    FocusedSessionResponse pause(Long userId, Long sessionId);
    FocusedSessionResponse saveChoice(Long userId, Long sessionId, Long questionId, SaveFocusedChoiceRequest request);
    FocusedSessionResponse finish(Long userId, Long sessionId);
    FocusedSessionResponse restart(Long userId, Long sessionId);
}
