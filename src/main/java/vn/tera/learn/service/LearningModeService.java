package vn.tera.learn.service;

import vn.tera.learn.dto.LearningModeSelectionResponse;
import vn.tera.learn.dto.LearningSessionResponse;
import vn.tera.learn.dto.StartLearningSessionRequest;

public interface LearningModeService {

    LearningModeSelectionResponse getLearningModes(Long userId, Long lessonId, Long sectionId);

    LearningSessionResponse startSession(
            Long userId,
            Long lessonId,
            Long sectionId,
            StartLearningSessionRequest request
    );
}
