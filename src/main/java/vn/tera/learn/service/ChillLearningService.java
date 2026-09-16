package vn.tera.learn.service;

import vn.tera.learn.dto.ChillAnswerResponse;
import vn.tera.learn.dto.ChillLearningSessionResponse;
import vn.tera.learn.dto.SubmitChillAnswerRequest;

public interface ChillLearningService {

    ChillLearningSessionResponse getSession(Long userId, Long sessionId);

    ChillLearningSessionResponse restartSession(Long userId, Long sessionId);

    ChillLearningSessionResponse restartLesson(Long userId, Long sessionId);

    ChillAnswerResponse submitAnswer(
            Long userId,
            Long sessionId,
            Long questionId,
            SubmitChillAnswerRequest request
    );
}
